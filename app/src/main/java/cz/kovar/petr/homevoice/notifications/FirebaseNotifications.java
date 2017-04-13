/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 13.04.2017.
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
package cz.kovar.petr.homevoice.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.List;

import cz.kovar.petr.homevoice.BuildConfig;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.dataModel.Instance;

public class FirebaseNotifications {

    private static final String LOG_TAG = "FirebaseNotifications";

    private static final String MODULE_ID = "FirebaseModule";

    private static final String TAG_API_KEY = "api_key";
    private static final String TAG_DEVICE_ID = "device_id";

    public static void init(DataContext aContext, ApiClient aApiClient) {
        Instance firebaseInstance = FirebaseNotifications.getInstance(aContext.getInstances());
        if(firebaseInstance != null) {
            if (!FirebaseNotifications.checkParams(firebaseInstance)) {
                FirebaseNotifications.fillParams(firebaseInstance);
                updateInstance(aApiClient, firebaseInstance);
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "Firebase instance updated.");
            } else {
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "Firebase instance already initiated.");
            }
        } else {
            createInstance(aApiClient);
            if(AppConfig.DEBUG) Log.d(LOG_TAG, "Firebase instance created.");
        }
    }

    private static Instance getInstance(List<Instance> aInstances) {
        for(Instance instance : aInstances) {
            if(instance.moduleId.equals(MODULE_ID)) return instance;
        }
        return null;
    }

    private static boolean checkParams(Instance aInstance) {
        if (aInstance == null) return false;
        if (!aInstance.params.containsKey(TAG_API_KEY)
                || !aInstance.params.containsKey(TAG_DEVICE_ID)) return false;

        String apiKey = (String) aInstance.params.get(TAG_API_KEY);
        String deviceId = (String) aInstance.params.get(TAG_DEVICE_ID);

        return apiKey.equals(BuildConfig.FIREBASE_API_KEY)
                && deviceId.equals(FirebaseInstanceId.getInstance().getToken());

    }

    private static void fillParams(Instance aInstance) {
        aInstance.params.put(TAG_API_KEY, BuildConfig.FIREBASE_API_KEY);
        aInstance.params.put(TAG_DEVICE_ID, FirebaseInstanceId.getInstance().getToken());
    }

    private static void updateInstance(ApiClient aClient, Instance aInstance) {
        try {
            aClient.updateInstance(aInstance);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to update Firebase Instance!", e);
        }
    }

    private static void createInstance(ApiClient aClient) {
        try {
            aClient.createInstance(prepareInstance());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to create Firebase Instance!", e);
        }
    }

    private static Instance prepareInstance() {
        Instance instance = new Instance();
        instance.moduleId = MODULE_ID;
        instance.active = true;
        instance.params = new LinkedTreeMap<>();
        instance.params.put(TAG_API_KEY, BuildConfig.FIREBASE_API_KEY);
        instance.params.put(TAG_DEVICE_ID, FirebaseInstanceId.getInstance().getToken());
        return instance;

    }

}
