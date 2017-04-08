/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 08.04.2017.
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
package cz.kovar.petr.homevoice.zwave.network.auth;

import android.content.Context;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.AuthEvent;
import cz.kovar.petr.homevoice.bus.events.CancelConnectionEvent;
import cz.kovar.petr.homevoice.zwave.services.AuthService;

public class ZWayAuthQueue {

    private static final String LOG_TAG = "ZWayAuthQueue";

    private enum State {
        DISCONNECTED,
        CONNECTING_LOCAL,
        CONNECTING_CLOUD,
        CONNECTED_LOCAL,
        CONNECTED_CLOUD
    }

    @Inject UserData userData;
    @Inject MainThreadBus bus;

    private Context m_context;

    private List<String> m_ipQueue = new ArrayList<>();

    private State m_state = State.DISCONNECTED;

    public ZWayAuthQueue(Context aContext) {
        m_context = aContext;
        ((ZWayApplication) aContext.getApplicationContext()).getComponent().inject(this);
        bus.register(this);
    }

    public void init() {
        m_ipQueue.clear();
        loginCloud();
    }

    public void addToQueue(String aIP) {
        Log.d(LOG_TAG, "ADD TO QUEUE: " + aIP);
        m_ipQueue.add(aIP);
        if(m_state == State.CONNECTING_CLOUD) {
            bus.post(new CancelConnectionEvent());
            loginLocal(aIP);
        }
        if(m_state == State.DISCONNECTED || m_state == State.CONNECTED_CLOUD) {
            loginLocal(aIP);
        }
    }

    @Subscribe
    public void onAuthSuccess(AuthEvent.Success event) {
        if(event.profile.useRemote()) {
            Log.d(LOG_TAG, "CLOUD SUCCESS");
            m_state = State.CONNECTED_CLOUD;
            if(!m_ipQueue.isEmpty()) loginLocal(m_ipQueue.get(0));
        } else {
            Log.d(LOG_TAG, "LOCAL SUCCESS");
            m_state = State.CONNECTED_LOCAL;
        }
    }

    @Subscribe
    public void onAuthFailed(AuthEvent.Fail event) {
        m_state = State.DISCONNECTED;
        Log.d(LOG_TAG, "FAILED");
        if(!m_ipQueue.isEmpty()) m_ipQueue.remove(0);
        if(m_ipQueue.isEmpty()) {
            loginCloud();
        } else {
            loginLocal(m_ipQueue.get(0));
        }
    }

    private void loginLocal(String aIP) {
        Log.d(LOG_TAG, "LOGIN LOCAL: " + aIP);
        m_state = State.CONNECTING_LOCAL;
        userData.getProfile().setLocalIP(aIP);
        userData.getProfile().useRemote(false);
        AuthService.login(m_context, userData.getProfile());
    }

    private void loginCloud() {
        Log.d(LOG_TAG, "LOGIN CLOUD");
        m_state = State.CONNECTING_CLOUD;
        userData.getProfile().useRemote(true);
        AuthService.login(m_context, userData.getProfile());
    }

}
