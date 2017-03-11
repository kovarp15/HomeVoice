/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 04.02.2017.
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

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private List<String> locationsIDs = new ArrayList<>();

    private FragmentHome m_home = FragmentHome.newInstance();
    private FragmentSettings m_settings = FragmentSettings.newInstance();

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        try{
            super.finishUpdate(container);
        } catch (NullPointerException nullPointerException){
            System.out.println("Catch the NullPointerException in FragmentPagerAdapter.finishUpdate");
        }
    }

    @Override
    public android.support.v4.app.Fragment getItem(int pos) {

        if(pos == 0) {
            return m_home;
        }

        if(pos == getCount() - 1) {
            return m_settings;
        }

        return FragmentLocation.newInstance(locationsIDs.get(pos - 1));
    }

    @Override
    public int getItemPosition(Object item) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return locationsIDs.size() + 2;
    }

    public void addLocationIDs(List<String> aLocationIDs) {
        locationsIDs = aLocationIDs;
        notifyDataSetChanged();
    }

    public void clear() {
        locationsIDs.clear();
        notifyDataSetChanged();
    }

}