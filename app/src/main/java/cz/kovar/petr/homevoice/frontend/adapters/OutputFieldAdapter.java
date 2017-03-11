/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 04.03.2017.
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
package cz.kovar.petr.homevoice.frontend.adapters;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.tts.SpeechSynthesizer;

public class OutputFieldAdapter {

    private static final String LOG_TAG = "OutputManager";

    private static final long CLEAR_DELAY = 3000; // 2 seconds

    private TextView m_outputField = null;
    private SpeechSynthesizer m_synthesizer = null;
    private Timer m_clearTimer = null;

    public void init(TextView aOutputField, SpeechSynthesizer aSynthesizer) {
        m_clearTimer = new Timer();
        m_outputField = aOutputField;
        m_synthesizer = aSynthesizer;
    }

    public void printOutput(final String aOutputText, long aDuration) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                m_outputField.setText(aOutputText);
            }
        });
        m_clearTimer.cancel();
        m_clearTimer = new Timer();
        m_clearTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                clear();
                            }
                        });
                    }
                },
                aDuration
        );
    }

    public void printOutput(String aOutputText) {
        printOutput(aOutputText, CLEAR_DELAY);
    }

    public void addOutput(final String aOutputText, final OnProgressListener aListener) {
        m_synthesizer.setUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "On speak start: " + s);
                printOutput(aOutputText);
            }

            @Override
            public void onDone(String s) {
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "On speak done: " + s);
                if(aListener != null) aListener.onDone();
            }

            @Override
            public void onError(String s) {

            }
        });
        m_synthesizer.speak(aOutputText);
    }

    public void addOutput(final List<String> aOutputList, final OnProgressListener aListener) {
        m_synthesizer.setUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "On speak start: " + s);
                printOutput(s);
            }

            @Override
            public void onDone(String s) {
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "On speak done: " + s);
                if(s.equalsIgnoreCase(aOutputList.get(aOutputList.size() - 1)))
                    if(aListener != null) aListener.onDone();
            }

            @Override
            public void onError(String s) {

            }
        });

        for(final String output : aOutputList)
            m_synthesizer.speak(output);
    }

    public void clear() {
        m_outputField.setText("");
    }

    public interface OnProgressListener {
        void onDone();
    }

}
