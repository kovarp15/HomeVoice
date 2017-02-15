/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 10.01.2017.
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

package cz.kovar.petr.homevoice.sr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Converts speech to text
 */
public class SpeechRecognition {

    private static final String LOG_TAG = "SpeechRecognition";

    private static final int ERROR_NETWORK = 2;

    private OnSpeechRecognitionListener m_listener;
    private android.speech.SpeechRecognizer m_recognizerSTT;

    public SpeechRecognition(Context aContext) {

        long start = System.currentTimeMillis();
        m_recognizerSTT = android.speech.SpeechRecognizer.createSpeechRecognizer(aContext);
        long duration = System.currentTimeMillis() - start;
        Log.e(LOG_TAG, "duration = " + duration);

        m_recognizerSTT.setRecognitionListener(new android.speech.RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Log.e(LOG_TAG, "error: " + error);
                switch(error) {
                    case ERROR_NETWORK:
                        m_recognizerSTT.cancel();
                       startRecognition();
                        break;
                    default:
                        if(m_listener != null) m_listener.onError();
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList data = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                if(data != null && !data.isEmpty() && m_listener != null)
                    m_listener.onRecognized((String) data.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList data = partialResults.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                if(data != null && !data.isEmpty() && m_listener != null)
                    m_listener.onPartial((String) data.get(0));
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}

        });
    }

    public void setOnSpeechRecognitionListener(OnSpeechRecognitionListener aListener) {
        m_listener = aListener;
    }

    public void startRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
        m_recognizerSTT.startListening(intent);
    }

    public void destroy() {
        if(m_recognizerSTT != null)
            m_recognizerSTT.destroy();
    }

    public interface OnSpeechRecognitionListener {

        void onPartial(String aText);

        void onRecognized(String aText);

        void onError();

    }

}
