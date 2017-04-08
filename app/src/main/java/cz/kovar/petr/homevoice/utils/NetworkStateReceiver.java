/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 08.04.2017.
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
package cz.kovar.petr.homevoice.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.InternetStateEvent;
import cz.kovar.petr.homevoice.zwave.network.portScan.NetInfo;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Inject
    MainThreadBus bus;

    private boolean online = false;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ((ZWayApplication) context.getApplicationContext()).getComponent().inject(this);
        bus.register(this);
        boolean onlineNow = NetInfo.isConnected(context);
        if(onlineNow != online) {
            bus.post(new InternetStateEvent(onlineNow));
            online = onlineNow;
        }
    }

}
