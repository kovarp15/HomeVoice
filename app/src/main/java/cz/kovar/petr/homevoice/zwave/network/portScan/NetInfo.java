/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 07.04.2017.
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
package cz.kovar.petr.homevoice.zwave.network.portScan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

import cz.kovar.petr.homevoice.zwave.utils.NetUtils;

public class NetInfo {

    public static final String LOG_TAG = "NetInfo";

    private static final String NOIP = "0.0.0.0";

    private String m_intf = "eth0";
    public String m_ip = NOIP;
    private int m_cidr = 24;
    private String m_netIP = NOIP;
    public long m_netStart = 0;
    public long m_netEnd = 0;

    public static boolean isConnected(Context ctxt) {
        NetworkInfo nfo = ((ConnectivityManager) ctxt
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return nfo != null && nfo.isConnected();
    }

    public static void logLocalNetInfo() {
        try {
            Enumeration<NetworkInterface> nwis = NetworkInterface.getNetworkInterfaces();
            while (nwis.hasMoreElements()) {
                NetworkInterface ni = nwis.nextElement();
                NetInfo info = getInterfaceInfo(ni);
                if(info.isValid()) {
                    info.prepareNetworkInfo();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NetInfo getNetInfo() {
        try {
            Enumeration<NetworkInterface> nwis = NetworkInterface.getNetworkInterfaces();
            while (nwis.hasMoreElements()) {
                NetworkInterface ni = nwis.nextElement();
                NetInfo info = getInterfaceInfo(ni);
                if(info.isValid()) {
                    info.prepareNetworkInfo();
                    return info;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new NetInfo();
    }

    private static NetInfo getInterfaceInfo(NetworkInterface ni) {
        if (ni == null) return new NetInfo();
        for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
            if (!ia.getAddress().isLoopbackAddress()) {
                if (ia.getAddress() instanceof Inet6Address) {
                    Log.i(LOG_TAG, "IPv6 detected and not supported yet!");
                    continue;
                }
                NetInfo info = new NetInfo();
                info.m_intf = ni.getDisplayName();
                info.m_ip   = ia.getAddress().getHostAddress();
                info.m_cidr = ia.getNetworkPrefixLength();
                return info;
            }
        }
        return new NetInfo();
    }

    private String getNetIp() {
        int shift = (32 - m_cidr);
        int start = ((int) NetUtils.getUnsignedLongFromIp(m_ip) >> shift << shift);
        return NetUtils.getIpFromLongUnsigned((long) start);
    }

    private void prepareNetworkInfo() {
        m_netIP = getNetIp();

        // Detected IP
        int shift = (32 - m_cidr);
        if (m_cidr < 31) {
            m_netStart = (NetUtils.getUnsignedLongFromIp(m_netIP) >> shift << shift) + 1;
            m_netEnd = (m_netStart | ((1 << shift) - 1)) - 1;
        } else {
            m_netStart = (NetUtils.getUnsignedLongFromIp(m_netIP) >> shift << shift);
            m_netEnd = (m_netStart | ((1 << shift) - 1));
        }
    }

    public boolean isValid() {
        return !m_ip.equals(NOIP);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s: %s/%d", m_intf,  m_ip, m_cidr);
    }

}
