/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 18.02.2017.
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.frontend.widgets.RoomButton;

/**
 * Provides frontend for plan
 */
public class FragmentPlan extends Fragment {

    private Button m_doneButton;
    private RoomButton m_testRoomButton;

    public static FragmentPlan newInstance() {

        return new FragmentPlan();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_plan, container, false);

        m_testRoomButton = (RoomButton) v.findViewById(R.id.roomButton);
        m_testRoomButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                m_doneButton.setVisibility(View.VISIBLE);
                m_testRoomButton.setEditMode(true);
                return true;
            }
        });

        m_doneButton = (Button) v.findViewById(R.id.doneButton);
        m_doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_testRoomButton.setEditMode(false);
                m_doneButton.setVisibility(View.GONE);
            }
        });

        return v;
    }

}
