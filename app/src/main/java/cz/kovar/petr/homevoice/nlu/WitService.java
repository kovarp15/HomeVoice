/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 16.04.2017.
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
package cz.kovar.petr.homevoice.nlu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class WitService extends Service {

    private static final String LOG_TAG = "WitService";

    public static final String COMMAND = "cmd";
    public static final int CMD_START = 1;
    public static final int CMD_NONE = -1;

    public static final int UPDATE_TIME = 30000; // 1 second

    private Timer m_timer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "On create");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "On destroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            int state = intent.getIntExtra(COMMAND, CMD_NONE);
            switch (state) {
                case CMD_START:
                    startDataUpdates();
                    break;
                case CMD_NONE:
                    break;
            }
        }
        return Service.START_STICKY;
    }

    protected void onRestart(){
        if (m_timer != null) m_timer.cancel();
        startDataUpdates();
    }

    protected void startDataUpdates() {
        m_timer = new Timer();
        m_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(LOG_TAG, "UPDATE");
                update();
            }
        }, 0, UPDATE_TIME);
    }

    private void update() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://api.wit.ai";
                try {
                    URLConnection connection = new URL(url).openConnection();
                    InputStream response = connection.getInputStream();
                    Log.e(LOG_TAG, response.toString());
                } catch (UnsupportedEncodingException e) {
                    Log.e(LOG_TAG, "Unable to connect WIT.ai (problem with encoding)", e);
                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, "Unable to connect WIT.ai (problem with URL)", e);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Unable to connect WIT.ai", e);
                }
            }
        });
        thread.start();
    }

}
