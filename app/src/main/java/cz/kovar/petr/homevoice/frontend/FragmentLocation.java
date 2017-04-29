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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.HashMap;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.zwave.DataContext;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;

/**
 * Provides frontend for home summary
 */
public class FragmentLocation extends FragmentBase {

    private static final String LOG_TAG = "FragmentLocation";

    private static final String TAG_LOCATION = "loc";

    private Button m_alarmButton;
    private Button m_doorButton;
    private Button m_lightButton;
    private Button m_climateButton;
    private Button m_powerButton;
    private Button m_cameraButton;

    private String m_locationID;

    public static FragmentLocation newInstance(String aLocationID) {

        FragmentLocation f = new FragmentLocation();
        Bundle b = new Bundle();
        b.putString(TAG_LOCATION, aLocationID);

        f.setArguments(b);

        return f;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_locationID = getArguments().getString(TAG_LOCATION);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_location, container, false);

        Button m_title = (Button) v.findViewById(R.id.titleButton);
        m_title.setText(m_locationID);
        m_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices(null);
            }
        });

        m_alarmButton = (Button) v.findViewById(R.id.alarmButton);
        m_alarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices("ALARM");
            }
        });

        m_doorButton = (Button) v.findViewById(R.id.doorButton);
        m_doorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices("DOOR");
            }
        });

        m_lightButton = (Button) v.findViewById(R.id.lightButton);
        m_lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices("LIGHT");
            }
        });

        m_climateButton = (Button) v.findViewById(R.id.climateButton);
        m_climateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices("CLIMATE");
            }
        });

        m_powerButton = (Button) v.findViewById(R.id.powerButton);
        m_powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices("POWER");
            }
        });

        m_cameraButton = (Button) v.findViewById(R.id.cameraButton);
        m_cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDevices("CAMERA");
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        showDevices(null);
    }

    private void showDevices(final String aTag) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        final String locationIndex = String.valueOf(super.dataContext.getLocationsNames().indexOf(m_locationID));
        HashMap<Filter, String> filters = new HashMap<>();
        filters.put(Filter.LOCATION, locationIndex);
        if(aTag != null) filters.put(Filter.TAG, aTag);
        fragmentTransaction.replace(R.id.fragmentDevices, FragmentDevices.newInstance(filters, false));
        fragmentTransaction.commit();
    }

}
