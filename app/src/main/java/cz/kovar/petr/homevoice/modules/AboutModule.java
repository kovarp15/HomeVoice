/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 15.02.2017.
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
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class AboutModule extends Module {

    private static final String LOG_TAG = "AboutModule";

    private static final String INTENT_ABOUT = "ABOUT";
    private static final String ENTITY_SCOPE = "scope";

    public AboutModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    Set<String> getSupportedIntents() {
        return new HashSet<String>() {{
            add(INTENT_ABOUT);
        }};
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(moduleContext, aIntent);

        if(!supportsIntent(moduleContext.intent)) return;

        switch(moduleContext.intent) {
            case INTENT_ABOUT:
                if(aIntent.hasEntity(ENTITY_SCOPE))
                    provideAbilitiesResponse();
                else
                    provideAboutResponse();
                break;
        }
    }

    private void provideAboutResponse() {
        notifyIntentHandled(SentenceHelper.randomResponse(m_context, R.array.about_response));
    }

    private void provideAbilitiesResponse() {
        notifyIntentHandled(SentenceHelper.randomResponse(m_context, R.array.abilities_response));
    }

    @Override
    void resetModule() {}

}
