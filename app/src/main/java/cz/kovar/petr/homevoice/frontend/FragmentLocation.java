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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.zwave.dataModel.Filter;

/**
 * Provides frontend for home summary
 */
public class FragmentLocation extends FragmentBase {

    private static final String LOG_TAG = "FragmentLocation";

    private static final String TAG_LOCATION = "loc";

    private TextView m_title;

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

        m_title = (TextView) v.findViewById(R.id.fragmentTitle);
        m_title.setText(m_locationID);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        String locationIndex = String.valueOf(super.dataContext.getLocationsNames().indexOf(m_locationID));
        fragmentTransaction.replace(R.id.fragmentDevices, FragmentDevices.newInstance(Filter.LOCATION, locationIndex));
        fragmentTransaction.commit();

    }

}
