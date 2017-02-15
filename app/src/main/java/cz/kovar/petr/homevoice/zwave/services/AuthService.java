/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 24.01.2017.
 * Copyright (c) 2017 Petr Kovář
 *
 * All rights reserved
 * kovarp15@fel.cvut.cz
 * HomeVoice for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HomeVoice for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HomeVoice for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.kovar.petr.homevoice.zwave.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.AuthEvent;
import cz.kovar.petr.homevoice.bus.events.CancelConnectionEvent;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.dataModel.Location;
import cz.kovar.petr.homevoice.zwave.network.auth.AuthRequest;
import cz.kovar.petr.homevoice.zwave.network.auth.HttpClientHelper;
import cz.kovar.petr.homevoice.zwave.ZWayProfile;
import cz.kovar.petr.homevoice.zwave.network.auth.LocalAuth;
import cz.kovar.petr.homevoice.zwave.network.auth.LocalAuthRequest;
import cz.kovar.petr.homevoice.zwave.network.auth.ZWayCookieJar;
import cz.kovar.petr.homevoice.zwave.utils.BooleanTypeAdapter;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Handles local and remote login to Z-Way control unit.
 */
public class AuthService extends IntentService {

    private static final String LOG_TAG = "AuthService";

    private static final String ACTION_LOGIN = "cz.kovar.petr.homevoice.zwave.services.action.LOGIN";

    private static final String EXTRA_LOGIN_PROFILE = "cz.kovar.petr.homevoice.zwave.services.extra.PROFILE";
    private static final String EXTRA_LOGIN_REQUEST_DELAY = "cz.kovar.petr.homevoice.zwave.services.extra.REQUEST_DELAY";

    private static final int AUTH_TRIES_COUNT = 3;
    private static final int DEFAULT_AUTH_REQUEST_DELAY = 10000; //10 sec

    @Inject
    ApiClient apiClient;
    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    private OkHttpClient mClient;
    private int mDelay = DEFAULT_AUTH_REQUEST_DELAY;

    private boolean mCancelEvent;

    public AuthService(String name) {
        super(name);
    }

    public AuthService() {
        super("AuthorizationService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        ((ZWayApplication) getApplicationContext()).getComponent().inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    public static void login(Context context, ZWayProfile profile, int requestDelay) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(EXTRA_LOGIN_PROFILE, profile);
        intent.putExtra(EXTRA_LOGIN_REQUEST_DELAY, requestDelay);
        context.startService(intent);
    }

    public static void login(Context context, ZWayProfile profile) {
        Intent intent = new Intent(context, AuthService.class);
        intent.setAction(ACTION_LOGIN);
        intent.putExtra(EXTRA_LOGIN_PROFILE, profile);
        intent.putExtra(EXTRA_LOGIN_REQUEST_DELAY, DEFAULT_AUTH_REQUEST_DELAY);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOGIN.equals(action)) {
                final ZWayProfile profile = (ZWayProfile) intent
                        .getSerializableExtra(EXTRA_LOGIN_PROFILE);
                mDelay = intent.getIntExtra(EXTRA_LOGIN_REQUEST_DELAY,
                        DEFAULT_AUTH_REQUEST_DELAY);
                handleActionAuth(profile);
            }
        }
    }

    @Subscribe
    public void onCancelEvent(CancelConnectionEvent event) {
        mCancelEvent = true;
    }

    private Cookie getCookie(HttpUrl aURL, String aName){
        final List<Cookie> cookies = mClient.cookieJar().loadForRequest(aURL);
        if (cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (cookie.name().compareToIgnoreCase(aName) == 0 &&
                    !TextUtils.isEmpty(cookie.value())) {
                return cookie;
            }
        }
        return null;
    }

    private void handleActionAuth(ZWayProfile profile) {

        final Retrofit adapter = prepareRestAdaptor(profile);
        if (profile.useRemote()) {
            cloudAuth(adapter, profile);
        } else {
            ZWayAuth(adapter, profile);
        }
    }

    public Retrofit prepareRestAdaptor(ZWayProfile profile) {
        if (profile != null) {
            Log.d(LOG_TAG, "init ApiClient");
            mClient = HttpClientHelper.createHttpsClient(mDelay);
            final String url = profile.getURL();
            final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Boolean.class, new BooleanTypeAdapter())
                    .setLenient()
                    .create();

            return new Retrofit.Builder()
                    .baseUrl(url)
                    .client(mClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return null;
    }

    private void cloudAuth (Retrofit adapter, final ZWayProfile profile) {

        for (int i = 0; i < AUTH_TRIES_COUNT; ++i) {

            Log.v(LOG_TAG, "Auth with find.z-wave, try " + i);
            if (mCancelEvent)
                return;

            try {
                adapter.create(AuthRequest.class).auth("login", profile.getLogin(), profile.getPassword()).execute();
            } catch (IOException e) {
                if(e instanceof UnknownHostException) {
                    onAuthFail(profile, true);
                }
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }

            if (getCookie(HttpUrl.parse(profile.getURL()), ZWayCookieJar.CLOUD_COOKIE) != null
                    && getCookie(HttpUrl.parse(profile.getURL()), ZWayCookieJar.ZWAY_COOKIE) != null) {
                onAuthSuccess(profile, adapter);
                //ZWayAuth(adapter, profile);
                return;
            }
        }
        onAuthFail(profile, true);
    }

    private void ZWayAuth(Retrofit adapter, final ZWayProfile profile) {
        for (int i = 0; i < AUTH_TRIES_COUNT; ++i) {

            Log.v(LOG_TAG, "Auth with ZBox, try " + i);
            if (mCancelEvent)
                return;

            try {
                adapter.create(LocalAuthRequest.class).auth(new LocalAuth(true, profile.getLogin(), profile.getPassword(), false, 1)).execute();
            } catch (IOException e) {
                if(e instanceof UnknownHostException) {
                    onAuthFail(profile, true);
                }
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }

            if (getCookie(HttpUrl.parse(profile.getURL()), ZWayCookieJar.ZWAY_COOKIE) != null) {
                onAuthSuccess(profile, adapter);
                return;
            }
        }
        onAuthFail(profile, true);

    }

    private void onAuthSuccess(ZWayProfile aProfile, Retrofit aAdaptor) {
        Log.v(LOG_TAG, "Auth success!");
        if(mCancelEvent) {
            return;
        }

        apiClient.init(aProfile, aAdaptor);

        dataContext.clear();
        //final List<ContactsContract.Profile> serverProfiles = loadProfiles();
        final List<Location> locations = loadLocation();

        dataContext.addLocations(locations);

        /*dataContext.addProfiles(serverProfiles);
        provider.addServerProfiles(serverProfiles, aProfile.id);
        dataContext.addLocations(locations);

        if((aProfile.serverId < 0 || !isServerProfileExist(serverProfiles, aProfile.serverId))
                && serverProfiles.size() > 0) {
            aProfile.serverId = serverProfiles.get(0).id;
        }

        provider.updateLocalProfile(aProfile);*/
        bus.post(new AuthEvent.Success(aProfile, aAdaptor));
        //bus.post(new AccountChangedEvent());
    }

    private void onAuthFail(ZWayProfile profile, boolean isNetworkError) {
        Log.v(LOG_TAG, "Auth fail");
        bus.post(new AuthEvent.Fail(profile, isNetworkError));
    }

    private List<Location> loadLocation(){
        try {
            return apiClient.getLocations().data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
