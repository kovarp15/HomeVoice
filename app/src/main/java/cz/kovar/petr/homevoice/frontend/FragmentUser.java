/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 11.04.2017.
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.UserData;
import cz.kovar.petr.homevoice.UserProfile;
import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.SettingsEvent;
import cz.kovar.petr.homevoice.zwave.ZWayProfile;

public class FragmentUser extends Fragment {

    private static final String LOG_TAG = "FragmentLogin";

    private ZWayProfile m_zwayProfile = null;

    @Inject
    UserData userData;
    @Inject
    MainThreadBus bus;

    private EditText m_loginEdit;
    private EditText m_passwordEdit;

    public static FragmentUser newInstance() {

        Log.v(LOG_TAG, "Create new instance of Fragment Settings.");
        return new FragmentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);

        initLayout(v);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((ZWayApplication) getContext().getApplicationContext()).getComponent().inject(this);
        bus.register(this);
        m_zwayProfile = userData.getZWayProfile();
        updateLayout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bus != null) bus.unregister(this);
    }

    private void initLayout(View aView) {

        m_loginEdit = (EditText) aView.findViewById(R.id.login);
        m_loginEdit.setOnEditorActionListener(new FragmentUser.ClearFocusListener(m_loginEdit));
        m_loginEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    String oldRemoteLogin = m_zwayProfile.getRemoteLogin();
                    String newRemoteLogin = m_loginEdit.getText().toString();
                    if(!oldRemoteLogin.equals(newRemoteLogin)) {
                        m_zwayProfile.setRemoteLogin(newRemoteLogin);
                        notifyZWayProfileChanged(m_zwayProfile);
                    }
                }
            }
        });

        m_passwordEdit = (EditText) aView.findViewById(R.id.password);
        m_passwordEdit.setOnEditorActionListener(new FragmentUser.ClearFocusListener(m_passwordEdit));
        m_passwordEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    String oldPassword = m_zwayProfile.getPassword();
                    String newPassword = m_passwordEdit.getText().toString();
                    if(!oldPassword.equals(newPassword)) {
                        m_zwayProfile.setPassword(newPassword);
                        notifyZWayProfileChanged(m_zwayProfile);
                    }
                }
            }
        });

    }

    private void updateLayout() {
        m_loginEdit.setText(m_zwayProfile.getRemoteLogin());
        m_passwordEdit.setText(m_zwayProfile.getPassword());
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
