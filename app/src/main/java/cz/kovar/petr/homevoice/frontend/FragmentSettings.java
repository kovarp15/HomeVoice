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

package cz.kovar.petr.homevoice.frontend;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.zwave.ZWayProfile;
import cz.kovar.petr.homevoice.zwave.network.portScan.NetInfo;
import cz.kovar.petr.homevoice.zwave.network.portScan.NetworkScanTask;
import cz.kovar.petr.homevoice.zwave.utils.NetUtils;

/**
 * Provides frontend for preference settings
 */
public class FragmentSettings extends Fragment {

    private static final String LOG_TAG = "FragmentSettings";

    private ZWayProfile m_profile = null;

    @Inject
    MainThreadBus bus;

    public static FragmentSettings newInstance() {

        Log.v(LOG_TAG, "Create new instance of Fragment Settings.");
        return new FragmentSettings();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_profile = UserData.loadZWayProfile(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        initZWayLayout(v);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ZWayApplication) getContext().getApplicationContext()).getComponent().inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    private void initZWayLayout(View aView) {

        final EditText localIPEdit   = (EditText) aView.findViewById(R.id.localIP);
        localIPEdit.setText(m_profile.getLocalIP());
        localIPEdit.setOnEditorActionListener(new ClearFocusListener(localIPEdit));
        localIPEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    m_profile.setLocalIP(localIPEdit.getText().toString());
                    onZWayProfileChanged(m_profile);
                }
            }
        });

        final EditText loginEdit     = (EditText) aView.findViewById(R.id.login);
        loginEdit.setText(m_profile.getRemoteLogin());
        loginEdit.setOnEditorActionListener(new ClearFocusListener(loginEdit));
        loginEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    m_profile.setRemoteLogin(loginEdit.getText().toString());
                    onZWayProfileChanged(m_profile);
                }
            }
        });

        final EditText passwordEdit  = (EditText) aView.findViewById(R.id.password);
        passwordEdit.setText(m_profile.getPassword());
        passwordEdit.setOnEditorActionListener(new ClearFocusListener(passwordEdit));
        passwordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    m_profile.setPassword(passwordEdit.getText().toString());
                    onZWayProfileChanged(m_profile);
                }
            }
        });

        final Switch remoteSwitch = (Switch) aView.findViewById(R.id.remoteSwitch);
        remoteSwitch.setChecked(m_profile.useRemote());
        remoteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_profile.useRemote(b);
                onZWayProfileChanged(m_profile);
            }
        });

        final TextView firebaseAPI = (TextView) aView.findViewById(R.id.firebaseAPI);
        firebaseAPI.setText(FirebaseInstanceId.getInstance().getToken());

    }

    private void onZWayProfileChanged(ZWayProfile aProfile) {
        bus.post(new SettingsEvent.ZWayChanged(aProfile));
    }

    private void hideKeyboard(View editText) {
        InputMethodManager imm= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private class ClearFocusListener implements TextView.OnEditorActionListener {

        private View m_view = null;

        ClearFocusListener(View aView) {

            m_view = aView;

        }

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                hideKeyboard(m_view);
                m_view.clearFocus();

            }

            return false;
        }

    }

}
