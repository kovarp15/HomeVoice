/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 31.01.2017.
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

package cz.kovar.petr.homevoice.bus;

import android.os.Handler;
import android.os.Looper;
import com.squareup.otto.Bus;

public class MainThreadBus extends Bus {
    private final Bus mBus;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public MainThreadBus(final Bus bus) {
        if (bus == null) {
            throw new NullPointerException("bus must not be null");
        }
        mBus = bus;
    }

    @Override
    public void register(Object obj) {
        mBus.register(obj);
    }

    @Override
    public void unregister(Object obj) {
        mBus.unregister(obj);
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mBus.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(event);
                }
            });
        }
    }
}