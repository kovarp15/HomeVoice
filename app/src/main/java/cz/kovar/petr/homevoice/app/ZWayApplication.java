/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 01.02.2017.
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

package cz.kovar.petr.homevoice.app;

import android.app.Application;

public class ZWayApplication extends Application {

    private ZWayComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerZWayComponent.builder().build();
    }

    public ZWayComponent getComponent() {
        return component;
    }

}
