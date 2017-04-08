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
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.zwave.ZWayProfile;

/**
 * Provides frontend for preference settings
 */
public class FragmentSettings extends Fragment {

    private static final String LOG_TAG = "FragmentSettings";

    private ZWayProfile m_profile = null;

    @Inject
    UserData userData;
    @Inject
    MainThreadBus bus;

    private EditText m_localIPEdit;
    private EditText m_loginEdit;
    private EditText m_passwordEdit;

    public static FragmentSettings newInstance() {

        Log.v(LOG_TAG, "Create new instance of Fragment Settings.");
        return new FragmentSettings();

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
        m_profile = userData.getProfile();
        updateLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    private void initZWayLayout(View aView) {

        m_localIPEdit = (EditText) aView.findViewById(R.id.localIP);
        m_localIPEdit.setOnEditorActionListener(new ClearFocusListener(m_localIPEdit));
        m_localIPEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    m_profile.setLocalIP(m_localIPEdit.getText().toString());
                    notifyZWayProfileChanged(m_profile);
                }
            }
        });

        m_loginEdit = (EditText) aView.findViewById(R.id.login);
        m_loginEdit.setOnEditorActionListener(new ClearFocusListener(m_loginEdit));
        m_loginEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    m_profile.setRemoteLogin(m_loginEdit.getText().toString());
                    notifyZWayProfileChanged(m_profile);
                }
            }
        });

        m_passwordEdit = (EditText) aView.findViewById(R.id.password);
        m_passwordEdit.setOnEditorActionListener(new ClearFocusListener(m_passwordEdit));
        m_passwordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    m_profile.setPassword(m_passwordEdit.getText().toString());
                    notifyZWayProfileChanged(m_profile);
                }
            }
        });

        TextView firebaseAPI = (TextView) aView.findViewById(R.id.firebaseAPI);
        firebaseAPI.setText(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateLayout() {
        m_localIPEdit.setText(m_profile.getLocalIP());
        m_loginEdit.setText(m_profile.getRemoteLogin());
        m_passwordEdit.setText(m_profile.getPassword());
    }

    private void notifyZWayProfileChanged(ZWayProfile aProfile) {
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
