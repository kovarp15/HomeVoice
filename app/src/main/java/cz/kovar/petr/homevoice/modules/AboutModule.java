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

import java.util.ArrayList;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;

public class AboutModule extends Module {

    private static final String LOG_TAG = "AboutModule";

    private static final String ABOUT_INTENT = "ABOUT";
    private static final String SCOPE_ENTITY = "scope";

    public AboutModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    public void handleIntent(UserIntent aIntent) {
        String intentName = (String) aIntent.getIntent().getValue();

        if(intentName.equals(ABOUT_INTENT)) {
            if(aIntent.hasEntity(SCOPE_ENTITY))
                bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                    add(randomResponse(R.array.abilities_response));
                }}));
            else
                bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                    add(randomResponse(R.array.about_response));
                }}));
        }
    }

}
