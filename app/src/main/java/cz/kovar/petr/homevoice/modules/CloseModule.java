/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.03.2017.
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

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class CloseModule extends Module {

    private static final String LOG_TAG = "CloseModule";
    private static final String INTENT_CLOSE = "EXIT_APP";

    public CloseModule(Context aContext) {
        super(aContext);
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    public void handleIntent(UserIntent aIntent) {
        if(aIntent.hasIntent() && aIntent.getIntent().getValue().equals(INTENT_CLOSE)) {
            closeActivity(2000);
            bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
                add(SentenceHelper.randomResponse(m_context, R.array.goodbye));
            }}));
        }
    }

    private void closeActivity(int aDelay) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "CLOSE ACTIVITY");
                ((Activity) m_context).finish();
            }
        }, aDelay);
    }

}
