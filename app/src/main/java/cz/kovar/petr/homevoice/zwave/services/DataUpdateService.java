/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 25.02.2017.
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
package cz.kovar.petr.homevoice.zwave.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.OnDataUpdatedEvent;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.network.devices.DevicesStateResponse;

public class DataUpdateService extends Service {

    private static final String LOG_TAG = "DataUpdateService";

    public static final int UPDATE_TIME = 1000; // 1 second

    private final IBinder m_binder = new Binder();

    @Inject
    MainThreadBus bus;

    @Inject
    ApiClient apiClient;

    @Inject
    DataContext dataContext;

    private Timer m_timer;
    private int m_updateTime = UPDATE_TIME;
    private long m_lastUpdateTime;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "On create");
        ((ZWayApplication) getApplication()).getComponent().inject(this);
        bus.register(this);
        m_lastUpdateTime = 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "On bind");
        startDataUpdates();
        return m_binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "On unbind");
        if(m_timer != null)
            m_timer.cancel();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "On destroy");
        bus.unregister(this);
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
                update();
            }
        }, 0, m_updateTime);
    }

    private void update() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(apiClient.isPrepared()) {
                    Log.v(LOG_TAG, "Update data");
                    onUpdateData();
                } else {
                    Log.v(LOG_TAG, "apiClient not prepared");
                    m_lastUpdateTime = 0;
                }
            }
        });
        thread.start();
    }

    public void onUpdateData() {
        final DevicesStateResponse devicesStateResponse;
        try {
            Log.v(LOG_TAG, "update (" + m_lastUpdateTime + ")");
            devicesStateResponse = apiClient.getDevices(m_lastUpdateTime);
            m_lastUpdateTime = devicesStateResponse.data.updateTime;
            if(devicesStateResponse.data.devices.size() > 0) {
                dataContext.addDevices(devicesStateResponse.data.devices);
                bus.post(new OnDataUpdatedEvent(devicesStateResponse.data.devices));
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while updating data", e);
            e.printStackTrace();
        }
    }

    protected void setUpdateTime(int time) {
        m_updateTime = time;
    }

    @Subscribe
    public void onSettingsChanged(SettingsEvent.ZWayChanged event) {
        m_lastUpdateTime = 0;
        onRestart();
    }

}
