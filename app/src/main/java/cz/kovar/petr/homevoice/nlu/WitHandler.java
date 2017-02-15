/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 13.10.2016.
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
package cz.kovar.petr.homevoice.nlu;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import cz.kovar.petr.homevoice.BuildConfig;

public class WitHandler implements NLUInterface {

    @Override
    public void getIntent(OnNLUListener aListener, String aCommand) {
        new TestAsync(aListener, aCommand).execute();
    }

    private static class TestAsync extends AsyncTask<Void, Integer, UserIntent> {
        String TAG = getClass().getSimpleName();

        OnNLUListener m_listener = null;
        String command = "";

        TestAsync(OnNLUListener aListener, String aCommand) {
            command = aCommand;
            m_listener = aListener;
        }

        protected UserIntent doInBackground(Void...arg0) {
            String url = "https://api.wit.ai/message";
            String key = BuildConfig.WIT_API_KEY;

            String param1 = "20161006";
            String param2 = command;
            String charset = "UTF-8";

            String query;
            InputStream response = null;
            try {
                query = String.format("v=%s&q=%s",
                        URLEncoder.encode(param1, charset),
                        URLEncoder.encode(param2, charset));

                URLConnection connection = new URL(url + "?" + query).openConnection();
                connection.setRequestProperty ("Authorization", "Bearer " + key);
                connection.setRequestProperty("Accept-Charset", charset);
                response = connection.getInputStream();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader(response)); //Convert the input stream to a json element
            return UserIntent.createFromJSON(root);
        }

        protected void onPostExecute(UserIntent result) {
            if(m_listener != null) m_listener.onMsgObjReceived(result);
            Log.d(TAG + " onPostExecute", "" + result);
        }
    }

}
