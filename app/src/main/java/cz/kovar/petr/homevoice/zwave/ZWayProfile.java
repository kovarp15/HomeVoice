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

package cz.kovar.petr.homevoice.zwave;

import android.text.TextUtils;

import java.io.Serializable;

import cz.kovar.petr.homevoice.app.AppConfig;

/**
 * Holds user data related to Z-Way server
 */
public class ZWayProfile implements Serializable {

    public static final String PREF_TAG_USE_REMOTE = "use_remote";
    public static final String PREF_TAG_REMOTE_URL = "remote_url";
    public static final String PREF_TAG_LOCAL_IP   = "local_ip";
    public static final String PREF_TAG_LOCAL_PORT = "local_port";
    public static final String PREF_TAG_LOGIN      = "login";
    public static final String PREF_TAG_PASSWORD   = "pass";

    private boolean m_useRemote = true;

    private String m_remoteURL = AppConfig.DEFAULT_REMOTE_URL;
    private String m_localIP   = "";
    private int    m_localPort = AppConfig.DEFAULT_LOCAL_PORT;
    private String m_login = "";
    private String m_pass  = "";

    public ZWayProfile(String aRemoteURL, String aLocalIP, int aLocalPort, String aLogin,
                       String aPass, boolean aUseRemote) {

        m_useRemote = aUseRemote;
        m_remoteURL = aRemoteURL;
        m_localIP   = aLocalIP;
        m_localPort = aLocalPort;
        m_login     = aLogin;
        m_pass      = aPass;

    }

    public void useRemote(boolean aUseRemote) {
        m_useRemote = aUseRemote;
    }
    public boolean useRemote() {
        return m_useRemote;
    }

    public void setRemoteURL(String aRemoteURL) {
        m_remoteURL = aRemoteURL;
    }
    public String getRemoteURL() {
        return m_remoteURL;
    }

    private String getLocalURL() {
        return "http://" + m_localIP + ":" + m_localPort;
    }

    public String getURL() {
        String url;
        if (m_useRemote) {
            url = getRemoteURL();
        } else {
            url = TextUtils.isEmpty(getLocalIP())
                    ? getRemoteURL() : getLocalURL();
        }
        return url;
    }

    public void setLocalIP(String aIP) {

        m_localIP = aIP;

    }

    public String getLocalIP() {

        return m_localIP;

    }

    public void setLocalPort(int aPort) {

        m_localPort = aPort;

    }

    public int getLocalPort() {

        return m_localPort;

    }

    public void setRemoteLogin(String aLogin) {

        m_login = aLogin;

    }

    public String getRemoteLogin() {

        return m_login;

    }

    private String getLocalLogin() {

        return m_login.substring(m_login.lastIndexOf("/") + 1);

    }

    public String getLogin() {

        if(m_useRemote) {
            return m_login;
        } else {
            return getLocalLogin();
        }

    }

    public void setPassword(String aPass) {

        m_pass = aPass;

    }

    public String getPassword() {

        return m_pass;

    }

}
