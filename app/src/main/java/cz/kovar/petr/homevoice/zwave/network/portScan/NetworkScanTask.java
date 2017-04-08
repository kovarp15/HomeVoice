/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 08.04.2017.
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

import android.os.AsyncTask;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.zwave.utils.NetUtils;

public class NetworkScanTask extends AsyncTask<Void, String, Void> {

    private static final String LOG_TAG = "NetworkScanTask";

    private final static int TIMEOUT_SCAN = 3600; // seconds
    private final static int TIMEOUT_SHUTDOWN = 10; // seconds
    private final static int THREADS = 10;

    private final OnNetworkScanListener m_listener;

    private int hosts_done = 0;
    private ExecutorService m_pool;
    private int pt_move = 2;
    private int mPort;

    private long ip;
    private long start = 0;
    private long end = 0;
    private long size = 0;

    private long m_scanStart = 0;

    public NetworkScanTask(int port, OnNetworkScanListener aListener) {
        m_listener = aListener;
        mPort = port;
    }

    public void setNetwork(long ip, long start, long end) {
        this.ip = ip;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void onPreExecute() {
        size = (int) (end - start + 1);
    }

    @Override
    protected void onProgressUpdate(String... host) {
        if (!isCancelled()) {
            if (host[0] != null) {
                Log.i(LOG_TAG, "FOUND: " + host[0]);
                if(m_listener != null) m_listener.onDeviceFound(host[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(Void unused) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "SCANNING DONE (" + (System.currentTimeMillis() - m_scanStart)/1000.0 + " sec)");
    }

    @Override
    protected void onCancelled() {
        if (m_pool != null) {
            synchronized (m_pool) {
                m_pool.shutdownNow();
            }
        }
        super.onCancelled();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(AppConfig.DEBUG) {
            Log.d(LOG_TAG, "SCANNING START\n|\tstart: " + NetUtils.getIpFromLongUnsigned(start) +
            "\n|\tend: " + NetUtils.getIpFromLongUnsigned(end) + "\n|\tcount: " + size);
        }
        m_scanStart = System.currentTimeMillis();
        m_pool = Executors.newFixedThreadPool(THREADS);
        if (ip <= end && ip >= start) {
            // gateway
            launch(start);

            // hosts
            long pt_backward = ip;
            long pt_forward = ip + 1;
            long size_hosts = size - 1;

            for (int i = 0; i < size_hosts; i++) {
                // Set pointer if of limits
                if (pt_backward <= start) {
                    pt_move = 2;
                } else if (pt_forward > end) {
                    pt_move = 1;
                }
                // Move back and forth
                if (pt_move == 1) {
                    launch(pt_backward);
                    pt_backward--;
                    pt_move = 2;
                } else if (pt_move == 2) {
                    launch(pt_forward);
                    pt_forward++;
                    pt_move = 1;
                }
            }
        } else {
            for (long i = start; i <= end; i++) {
                launch(i);
            }
        }
        m_pool.shutdown();
        try {
            if(!m_pool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)){
                m_pool.shutdownNow();
                Log.e(LOG_TAG, "Shutting down pool");
                if(!m_pool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)){
                    Log.e(LOG_TAG, "Pool did not terminate");
                }
            }
        } catch (InterruptedException e){
            Log.e(LOG_TAG, e.getMessage());
            m_pool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return null;
    }

    private void launch(long i) {
        if(!m_pool.isShutdown()) {
            m_pool.execute(new CheckRunnable(NetUtils.getIpFromLongUnsigned(i)));
        }
    }

    private class CheckRunnable implements Runnable {
        private String addr;

        CheckRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            if(isCancelled()) {
                publish(null);
            }
            final String host = addr;

            try {
                final Socket socket = new Socket();
                socket.connect(new InetSocketAddress(String.valueOf(addr), mPort), 500);
                socket.close();
                publish(host);
            } catch (Exception ex) {
                publish(null);
            }
        }
    }

    private void publish(final String host) {
        hosts_done++;
        publishProgress(host);

    }

    public interface OnNetworkScanListener {

        void onDeviceFound(String aIP);

    }
}
