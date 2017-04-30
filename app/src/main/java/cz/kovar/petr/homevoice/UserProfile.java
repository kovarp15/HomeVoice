/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 29.04.2017.
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

import java.io.Serializable;

/**
 * Holds user data related to HomeVoice application
 */
public class UserProfile implements Serializable {

    public static final String PREF_TAG_USER_NAME  = "user_name";
    public static final String PREF_TAG_USER_LOCATION = "user_location";

    private String m_userName = "";
    private String m_userLocation = "";

    public UserProfile(String aUserName, String aLocation) {

        m_userName = aUserName;
        m_userLocation = aLocation;

    }

    public void setUserName(String aUserName) {
        m_userName = aUserName;
    }
    public String getUserName() {
        return m_userName;
    }

    public void setUserLocation(String aUserLocation) {
        m_userLocation = aUserLocation;
    }
    public String getUserLocation() {
        return m_userLocation;
    }

}
