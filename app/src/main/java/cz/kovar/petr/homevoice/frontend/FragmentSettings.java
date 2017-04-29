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

package cz.kovar.petr.homevoice.frontend;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;

/**
 * Provides frontend for preference settings
 */
public class FragmentSettings extends Fragment {

    private static final String LOG_TAG = "FragmentSettings";

    @Inject
    UserData userData;
    @Inject
    MainThreadBus bus;

    public static FragmentSettings newInstance() {

        Log.v(LOG_TAG, "Create new instance of Fragment Settings.");
        return new FragmentSettings();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        Button loginButton = (Button) v.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginFragment();
            }
        });

        Button devicesButton = (Button) v.findViewById(R.id.devicesButton);
        devicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices();
            }
        });

        Button modulesButton = (Button) v.findViewById(R.id.modulesButton);
        modulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO show modules
            }
        });

        Button appsButton = (Button) v.findViewById(R.id.appsButton);
        appsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInstancesFragment();
            }
        });

        Button aboutButton = (Button) v.findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAboutFragment();
            }
        });

        showLoginFragment();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ZWayApplication) getContext().getApplicationContext()).getComponent().inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    private void showLoginFragment() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentSettings, FragmentLogin.newInstance());
        fragmentTransaction.commit();
    }

    private void showDevices() {
        showFragment(FragmentDevices.newInstance(new HashMap<Filter, String>(), true));
    }

    private void showInstancesFragment() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentSettings, FragmentInstances.newInstance());
        fragmentTransaction.commit();
    }

    private void showAboutFragment() {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentSettings, FragmentAbout.newInstance());
        fragmentTransaction.commit();
    }

    private void showFragment(Fragment aFragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentSettings, aFragment);
        fragmentTransaction.commit();
    }

}
