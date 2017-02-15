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

import java.util.Random;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.tts.SpeechSynthesizer;

public class AboutModule implements Module {

    private Context m_context;
    private SpeechSynthesizer m_synthetizer;

    private static final String ABOUT_INTENT = "ABOUT";
    private static final String SCOPE_ENTITY = "scope";

    public AboutModule(Context aContext, SpeechSynthesizer aSynthetizer) {
        m_context = aContext;
        m_synthetizer = aSynthetizer;
    }

    @Override
    public void handleIntent(UserIntent aIntent) {
        String intentName = (String) aIntent.getIntent().getValue();

        if(intentName.equals(ABOUT_INTENT)) {
            if(aIntent.hasEntity(SCOPE_ENTITY))
                m_synthetizer.speak(randomResponse(R.array.abilities_response));
            else
                m_synthetizer.speak(randomResponse(R.array.about_response));
        }
    }

    private String randomResponse(int aStringArrayID) {
        String[] responseArray = m_context.getResources().getStringArray(aStringArrayID);
        return responseArray[new Random().nextInt(responseArray.length)];
    }
}
