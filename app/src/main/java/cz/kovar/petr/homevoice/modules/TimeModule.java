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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.tts.SpeechSynthesizer;

public class TimeModule implements Module {

    private Context m_context;
    private SpeechSynthesizer m_synthetizer;

    private static final String TIME_INTENT = "TIME";

    public TimeModule(Context aContext, SpeechSynthesizer aSynthetizer) {
        m_context = aContext;
        m_synthetizer = aSynthetizer;
    }

    @Override
    public void handleIntent(UserIntent aIntent) {
        if(!aIntent.hasIntent()) return;

        String intentName = (String) aIntent.getIntent().getValue();

        if(intentName.equals(TIME_INTENT)) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa", Locale.US);
            m_synthetizer.speak("It is " + sdf.format(new Date().getTime()));
        }
    }

}
