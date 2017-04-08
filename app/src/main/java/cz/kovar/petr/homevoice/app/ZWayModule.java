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

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.frontend.adapters.OutputFieldAdapter;
import cz.kovar.petr.homevoice.zwave.ApiClient;
import cz.kovar.petr.homevoice.zwave.DataContext;
import dagger.Module;
import dagger.Provides;

@Module
public class ZWayModule {

    @Provides
    @Singleton
    MainThreadBus provideBus() {
        return new MainThreadBus(new Bus());
    }

    @Provides
    @Singleton
    ApiClient provideApiClient() {
        return new ApiClient();
    }

    @Provides
    @Singleton
    DataContext provideDataContext() {
        return new DataContext();
    }

    @Provides
    @Singleton
    OutputFieldAdapter provideOutputAdapter() {
        return new OutputFieldAdapter();
    }

    @Provides
    @Singleton
    UserData provideUserData() {
        return new UserData();
    }

}
