/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.10.2016.
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
package cz.kovar.petr.homevoice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cz.kovar.petr.homevoice.utils.ScreenReceiver;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * Listens for keyword when screen is on
 */
public class KeywordSpotting extends Service {

    public static final String COMMAND = "cmd";
    public static final int CMD_START = 1;
    public static final int CMD_STOP  = 0;
    public static final int CMD_NONE  = -1;

    private static final String LOG_TAG = "KeywordSpotting";
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "okay smart home";

    private SpeechRecognizer m_recognizer;
    private boolean m_running = false;

    @Override
    public void onCreate(){
        // Check if user has given permission to record audio
        /*int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            m_serviceApproved = false;
            return;
        }*/
        // Initialization
        Log.e(LOG_TAG, "onCreate KeywordSpotting Service");
        initService();

        // REGISTER RECEIVER THAT HANDLES SCREEN ON AND SCREEN OFF LOGIC
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        m_recognizer.cancel();
    }

    private boolean initService() {
        Log.e(LOG_TAG, "init KeywordSpotting Service");
        try {
            Assets assets = new Assets(this);
            File assetsDir = assets.syncAssets();

            m_recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                    .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                    //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                    .setKeywordThreshold(1e-45f) // Threshold to tune for keyphrase to balance between false alarms and misses
                    .setBoolean("-allphone_ci", true)  // Use context-independent phonetic search, context-dependent is too slow for mobile
                    .getRecognizer();

            m_recognizer.addListener(new OnRecognitionListener());
            m_recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

            Toast.makeText(this, "KeywordSpotting prepared", Toast.LENGTH_LONG).show();
            if(m_running) {
                m_recognizer.startListening(KWS_SEARCH);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            int state = intent.getIntExtra(COMMAND, CMD_NONE);
            switch (state) {
                case CMD_START:
                    startService();
                    break;
                case CMD_STOP:
                    stopService();
                    break;
                case CMD_NONE:
                    if (isScreenOn()) startService();
                    break;
            }
        } else {
            if (isScreenOn()) startService();
        }
        return Service.START_STICKY;
    }

    public void startService() {
        Log.d(LOG_TAG, "Start service called.");
        if(m_recognizer != null) {
            m_recognizer.startListening(KWS_SEARCH);
        } else {
            m_running = true;
        }
    }

    public void stopService() {
        Log.d(LOG_TAG, "Stop service called.");
        m_recognizer.stop();
        m_running = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class OnRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
                return;

            String text = hypothesis.getHypstr();
            if (text.equals(KEYPHRASE)) {
                stopService();
            }
        }

        @Override
        public void onResult(Hypothesis hypothesis) {
            if (hypothesis != null) {
                runMainActivity();
                Log.d(LOG_TAG, "Keyword spotted");
            }
        }

        @Override
        public void onError(Exception e) {
            Log.e(LOG_TAG, e.getLocalizedMessage());

        }

        @Override
        public void onTimeout() {
            // TODO restart
        }
    }

    private void runMainActivity() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "CHESS");
        wl.acquire();
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.putExtra(MainActivity.TAG_COMMAND, MainActivity.CMD_SPEECH_RECOGNITION);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
        wl.release();
    }

    private boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        return powerManager.isScreenOn();
    }

}
