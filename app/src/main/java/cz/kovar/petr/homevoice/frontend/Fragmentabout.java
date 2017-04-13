/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 13.04.2017.
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cz.kovar.petr.homevoice.BuildConfig;
import cz.kovar.petr.homevoice.R;

public class FragmentAbout extends Fragment {

    private static final String LOG_TAG = "FragmentAbout";

    public static FragmentAbout newInstance() {
        Log.v(LOG_TAG, "Create new instance of Fragment About.");
        return new FragmentAbout();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        TextView versionView = (TextView) v.findViewById(R.id.version);
        versionView.setText("v" + BuildConfig.VERSION_NAME + " (rev. " + BuildConfig.BUILD_NUMBER + ")");
        return v;
    }

}
