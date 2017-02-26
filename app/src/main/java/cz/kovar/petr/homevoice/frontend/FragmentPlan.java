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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.bus.events.AuthEvent;
import cz.kovar.petr.homevoice.bus.events.ShowEvent;
import cz.kovar.petr.homevoice.frontend.widgets.LocationButton;
import cz.kovar.petr.homevoice.utils.UtilsConverter;

/**
 * Provides frontend for plan
 */
public class FragmentPlan extends FragmentBase {

    private static final String LOG_TAG = "FragmentPlan";
    private static final String PLAN_PREF_NAME = "plan_preferences";
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_WIDTH = "width";
    private static final String TAG_HEIGHT = "height";
    private static final String TAG_LEFT   = "left";
    private static final String TAG_TOP    = "top";

    private static final int LOAD_PLAN = 0;
    private static final int MAP_ROOM  = 1;

    private HashMap<String, LocationButton> m_locationButtons = new HashMap<>();
    private HashMap<String, Parameters> m_paramsBackup = new HashMap<>();
    private SharedPreferences m_preferences;

    private ImageButton m_doneButton;
    private ImageButton m_deleteButton;
    private ImageButton m_cancelButton;
    private ImageButton m_optionsButton;

    public static FragmentPlan newInstance() {

        return new FragmentPlan();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.e(LOG_TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_plan, container, false);

        final List<String> locToDelete = new ArrayList<>();

        m_doneButton = (ImageButton) v.findViewById(R.id.doneButton);
        m_doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_paramsBackup.clear();
                deleteLocations(locToDelete);
                savePlanPreferences();
                clearEditMode();
            }
        });

        m_deleteButton = (ImageButton) v.findViewById(R.id.deleteButton);
        m_deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locToDelete.clear();
                locToDelete.addAll(updateDeleteList());
            }
        });

        m_cancelButton = (ImageButton) v.findViewById(R.id.cancelButton);
        m_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revertDelete(locToDelete);
                revertModifications(m_paramsBackup);
                clearEditMode();
            }
        });

        m_optionsButton = (ImageButton) v.findViewById(R.id.optionsButton);
        m_optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });

        return v;
    }

    private void showOptions() {
        final ListPopupWindow popupWindow = new ListPopupWindow(getContext());
        String[] items = getResources().getStringArray(R.array.plan_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(m_optionsButton);
        popupWindow.setWidth(UtilsConverter.dp2px(getContext(), 125));
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case LOAD_PLAN:
                        // TODO Implement load plan from directory
                        Toast.makeText(getContext(), getString(R.string.not_implemented_yet), Toast.LENGTH_SHORT).show();
                        break;
                    case MAP_ROOM:
                        popupWindow.dismiss();
                        showLocations();
                        break;
                }
            }
        });
        popupWindow.show();
    }

    private void showLocations(){

        final List<String> locations = dataContext.getLocationsNames();

        // remove already mapped locations
        locations.removeAll(m_locationButtons.keySet());

        String[] items = new String[locations.size()];
        items = locations.toArray(items);

        // prepare and show popup menu
        ListPopupWindow popupWindow = new ListPopupWindow(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(m_optionsButton);
        popupWindow.setWidth(UtilsConverter.dp2px(getContext(), 125));
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setEditMode(addLocationButton(locations.get(i),
                                  UtilsConverter.dp2px(getContext(),150),
                                  UtilsConverter.dp2px(getContext(),150),
                                  0, 0));
            }
        });
        popupWindow.show();

    }

    private void initLocationButtons() {
        Set<String> savedLocations = m_preferences.getStringSet(TAG_LOCATIONS, new HashSet<String>());
        Set<String> currentLocations = new HashSet<>(dataContext.getLocationsNames());

        for(String location : currentLocations) {
            if(savedLocations.contains(location) && !m_locationButtons.keySet().contains(location)) {
                int width = m_preferences.getInt(location + TAG_WIDTH, 150);
                int height = m_preferences.getInt(location + TAG_HEIGHT, 150);
                int left = m_preferences.getInt(location + TAG_LEFT, 0);
                int top = m_preferences.getInt(location + TAG_TOP, 0);
                addLocationButton(location, width, height, left, top);
            }
        }

    }

    private LocationButton addLocationButton(final String aID, final int aWidth, final int aHeight, int aLeft, int aTop) {
        final LocationButton button = new LocationButton(getContext());
        button.setText(aID);
        button.setDimensions(aWidth,aHeight);
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                RelativeLayout.LayoutParams lparams = (RelativeLayout.LayoutParams) button.getLayoutParams();
                Parameters params = new Parameters(lparams.width, lparams.height, lparams.leftMargin, lparams.topMargin);
                m_paramsBackup.put(aID, params);
                setEditMode((LocationButton) view);
                return true;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = ((LocationButton) view).getText().toString();
                bus.post(new ShowEvent.Location(id));
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(aWidth, aHeight);
        params.leftMargin = aLeft;
        params.topMargin = aTop;
        button.setLayoutParams(params);
        m_locationButtons.put(aID, button);
        ((RelativeLayout) getActivity().findViewById(R.id.fragment)).addView(button);
        return button;
    }

    private void setEditMode(LocationButton aButton) {
        aButton.setEditMode();
        m_doneButton.setVisibility(View.VISIBLE);
        m_deleteButton.setVisibility(View.VISIBLE);
        m_cancelButton.setVisibility(View.VISIBLE);
    }

    private void clearEditMode() {
        for(LocationButton button : m_locationButtons.values()) {
            button.clearEditMode();
        }
        m_doneButton.setVisibility(View.GONE);
        m_deleteButton.setVisibility(View.GONE);
        m_cancelButton.setVisibility(View.GONE);
    }

    private List<String> updateDeleteList() {
        List<String> toDelete = new ArrayList<>();
        for(String name : m_locationButtons.keySet()) {
            LocationButton button = m_locationButtons.get(name);
            if(button.isEdited()) {
                button.setVisibility(View.INVISIBLE);
                toDelete.add(name);
            }
        }
        return toDelete;
    }

    private void deleteLocations(List<String> aLocations) {
        for(String name : aLocations) {
            LocationButton button = m_locationButtons.get(name);
            m_locationButtons.remove(name);
            RelativeLayout parent = (RelativeLayout) button.getParent();
            parent.removeView(button);
        }
        aLocations.clear();
    }

    private void revertDelete(List<String> aLocations) {
        for(String name : aLocations) {
            LocationButton button = m_locationButtons.get(name);
            button.setVisibility(View.VISIBLE);
        }
        aLocations.clear();
    }

    private void revertModifications(Map<String, Parameters> aBackup) {
        for(String name : aBackup.keySet()) {
            LocationButton button = m_locationButtons.get(name);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
            params.width = aBackup.get(name).width;
            params.height = aBackup.get(name).height;
            params.leftMargin = aBackup.get(name).left;
            params.topMargin = aBackup.get(name).top;
            button.setLayoutParams(params);
        }
        aBackup.clear();
    }

    private void savePlanPreferences() {
        SharedPreferences.Editor editor = m_preferences.edit();
        editor.putStringSet(TAG_LOCATIONS, m_locationButtons.keySet());
        for(String location : m_locationButtons.keySet()) {
            LocationButton button = m_locationButtons.get(location);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
            editor.putInt(location + TAG_WIDTH, button.getWidth());
            editor.putInt(location + TAG_HEIGHT, button.getHeight());
            editor.putInt(location + TAG_LEFT, params.leftMargin);
            editor.putInt(location + TAG_TOP, params.topMargin);
        }
        editor.apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        m_preferences = getContext().getSharedPreferences(PLAN_PREF_NAME, Context.MODE_PRIVATE);
        initLocationButtons();
    }

    @Subscribe
    public void onAuthSuccess(AuthEvent.Success event) {
        Log.v(LOG_TAG, "Auth Success!");
    }

    @Subscribe
    public void onAuthFail(AuthEvent.Fail event) {
        Log.v(LOG_TAG, "Auth Failed!");
    }

    private class Parameters {

        final int width;
        final int height;
        final int left;
        final int top;

        Parameters(int aWidth, int aHeight, int aLeft, int aTop) {
            width = aWidth;
            height = aHeight;
            left = aLeft;
            top = aTop;
        }

    }

}
