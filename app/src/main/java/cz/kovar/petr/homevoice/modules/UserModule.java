/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 29.04.2017.
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
package cz.kovar.petr.homevoice.modules;

import android.content.Context;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.UserProfile;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class UserModule extends Module {

    private static final String LOG_TAG = "UserModule";

    private static final String INTENT_INTRODUCTION = "INTRODUCTION";
    private static final String ENTITY_NAME = "name";
    private static final String ENTITY_LOCATION = "location";

    public UserModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    Set<String> getSupportedIntents() {
        return new HashSet<String>() {{
            add(INTENT_INTRODUCTION);
        }};
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(moduleContext, aIntent);

        if(!supportsIntent(moduleContext.intent)) return;

        switch(moduleContext.intent) {
            case INTENT_INTRODUCTION:
                if(aIntent.hasEntity(ENTITY_NAME)) {
                    String name = aIntent.getEntity(ENTITY_NAME).getValue().toString();
                    UserProfile userProfile = userData.getUserProfile();
                    userProfile.setUserName(name);
                    bus.post(new SettingsEvent.UserChanged(userProfile));
                    provideUserNameSaved(name);
                }
                if(aIntent.hasEntity(ENTITY_LOCATION)) {
                    String location = aIntent.getEntity(ENTITY_LOCATION).getValue().toString();
                    UserProfile userProfile = userData.getUserProfile();
                    userProfile.setUserLocation(location);
                    bus.post(new SettingsEvent.UserChanged(userProfile));
                    provideUserLocationSaved(location);
                }
        }
    }

    private void provideUserNameSaved(String aName) {
        notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.your_name_is), aName));
    }

    private void provideUserLocationSaved(String aLocation) {
        notifyIntentHandled("You live in " + aLocation);
    }

    @Override
    void resetModule() {}

}
