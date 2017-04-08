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

import javax.inject.Singleton;

import cz.kovar.petr.homevoice.MainActivity;
import cz.kovar.petr.homevoice.frontend.FragmentBase;
import cz.kovar.petr.homevoice.frontend.FragmentHome;
import cz.kovar.petr.homevoice.frontend.FragmentSettings;
import cz.kovar.petr.homevoice.frontend.dialogs.CameraDialog;
import cz.kovar.petr.homevoice.modules.DeviceModule;
import cz.kovar.petr.homevoice.modules.Module;
import cz.kovar.petr.homevoice.modules.RoomModule;
import cz.kovar.petr.homevoice.utils.NetworkStateReceiver;
import cz.kovar.petr.homevoice.zwave.services.AuthService;
import cz.kovar.petr.homevoice.zwave.services.DataUpdateService;
import cz.kovar.petr.homevoice.zwave.services.UpdateDeviceService;
import dagger.Component;

@Singleton
@Component(modules = { ZWayModule.class})
public interface ZWayComponent {

    void inject(AuthService authService);

    void inject(MainActivity activity);

    void inject(FragmentSettings fragment);

    void inject(FragmentHome fragment);

    void inject(FragmentBase fragment);

    void inject(DataUpdateService fragment);

    void inject(UpdateDeviceService service);

    void inject(NetworkStateReceiver receiver);

    void inject(Module module);
    void inject(RoomModule module);
    void inject(DeviceModule module);
    void inject(CameraDialog dialog);

}
