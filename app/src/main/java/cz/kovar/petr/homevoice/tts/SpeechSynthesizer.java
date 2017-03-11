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
package cz.kovar.petr.homevoice.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

import javax.inject.Inject;

public class SpeechSynthesizer {

    private TextToSpeech m_tts;

    private boolean m_initialized = false;

    public SpeechSynthesizer(Context aContext) {
        m_tts = new TextToSpeech(aContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                m_initialized = true;
                m_tts.setLanguage(Locale.US);
            }
        });
    }

    public void speak(String aText) {
        if(m_initialized) m_tts.speak(aText, TextToSpeech.QUEUE_ADD, null, aText);
    }

    public void setUtteranceProgressListener(UtteranceProgressListener aListener) {
        m_tts.setOnUtteranceProgressListener(aListener);
    }

    public void shutdown() {
        if(m_tts != null) {
            m_tts.stop();
            m_tts.shutdown();
        }
    }

}
