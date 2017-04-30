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

package cz.kovar.petr.homevoice;

import android.content.Context;
import android.content.SharedPreferences;

import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.zwave.ZWayProfile;

/**
 * Holds user data related to Z-Way server, NLU service
 */
public class UserData {

    private ZWayProfile m_zwayProfile = null;
    private UserProfile m_userProfile = null;

    public ZWayProfile getZWayProfile() {
        return m_zwayProfile;
    }
    public UserProfile getUserProfile() {
        return m_userProfile;
    }

    /**
     * Initializes User Data
     *
     * @param aContext activity
     */
    public void initUserData(Context aContext) {

        loadZWayProfile(aContext);
        loadUserProfile(aContext);

    }

    private void loadZWayProfile(Context aContext) {

        SharedPreferences sharedPreferences = aContext.getSharedPreferences(AppConfig.ZWAY_PREF_FILE_KEY, Context.MODE_PRIVATE);

        String  remoteURL = sharedPreferences.getString(ZWayProfile.PREF_TAG_REMOTE_URL, AppConfig.DEFAULT_REMOTE_URL);
        String  localIP   = sharedPreferences.getString(ZWayProfile.PREF_TAG_LOCAL_IP, "");
        int     localPort = sharedPreferences.getInt(ZWayProfile.PREF_TAG_LOCAL_PORT, AppConfig.DEFAULT_LOCAL_PORT);
        String  login     = sharedPreferences.getString(ZWayProfile.PREF_TAG_LOGIN, "");
        String  pass      = sharedPreferences.getString(ZWayProfile.PREF_TAG_PASSWORD, "");
        boolean useRemote = sharedPreferences.getBoolean(ZWayProfile.PREF_TAG_USE_REMOTE, true);

        m_zwayProfile = new ZWayProfile(remoteURL, localIP, localPort, login, pass, useRemote);
    }

    private void loadUserProfile(Context aContext) {

        SharedPreferences sharedPreferences = aContext.getSharedPreferences(AppConfig.USER_PREF_FILE_KEY, Context.MODE_PRIVATE);

        String  username  = sharedPreferences.getString(UserProfile.PREF_TAG_USER_NAME, "");
        String  location  = sharedPreferences.getString(UserProfile.PREF_TAG_USER_LOCATION, "");

        m_userProfile = new UserProfile(username, location);
    }

    public static void saveZWayProfile(Context aContext, ZWayProfile aProfile) {

        SharedPreferences sharedPreferences = aContext.getSharedPreferences(AppConfig.ZWAY_PREF_FILE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ZWayProfile.PREF_TAG_REMOTE_URL, aProfile.getRemoteURL());
        editor.putString(ZWayProfile.PREF_TAG_LOCAL_IP, aProfile.getLocalIP());
        editor.putInt(ZWayProfile.PREF_TAG_LOCAL_PORT, aProfile.getLocalPort());
        editor.putString(ZWayProfile.PREF_TAG_LOGIN, aProfile.getRemoteLogin());
        editor.putString(ZWayProfile.PREF_TAG_PASSWORD, aProfile.getPassword());
        editor.putBoolean(ZWayProfile.PREF_TAG_USE_REMOTE, aProfile.useRemote());
        editor.apply();
    }

    public static void saveUserProfile(Context aContext, UserProfile aProfile) {

        SharedPreferences sharedPreferences = aContext.getSharedPreferences(AppConfig.USER_PREF_FILE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UserProfile.PREF_TAG_USER_NAME, aProfile.getUserName());
        editor.putString(UserProfile.PREF_TAG_USER_LOCATION, aProfile.getUserLocation());
        editor.apply();

    }

}
