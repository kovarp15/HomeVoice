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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class TimeModule extends Module {

    private static final String LOG_TAG = "TimeModule";

    private static final String INTENT_TIME = "TIME";
    private static final long REPEATED_THRESHOLD = 10000;

    private long m_lastActivityTime = 0;

    public TimeModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    Set<String> getSupportedIntents() {
        return new HashSet<String>() {{
            add(INTENT_TIME);
        }};
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(moduleContext, aIntent);

        if(!supportsIntent(moduleContext.intent)) return;

        switch(moduleContext.intent) {
            case INTENT_TIME:
                long currentTime = System.currentTimeMillis();

                if(currentTime - m_lastActivityTime < REPEATED_THRESHOLD) {
                    provideEasterEgg();
                } else {
                    provideTimeResponse();
                }

                m_lastActivityTime = currentTime;
                break;
        }

    }

    private void provideEasterEgg() {
        notifyIntentHandled(SentenceHelper.randomResponse(m_context, R.array.repeated_time_query));
    }

    private void provideTimeResponse() {
        final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.US);
        notifyIntentHandled(String.format(SentenceHelper.randomResponse(m_context, R.array.current_time),
                sdf.format(new Date().getTime())));
    }

    @Override
    void resetModule() {}

}
