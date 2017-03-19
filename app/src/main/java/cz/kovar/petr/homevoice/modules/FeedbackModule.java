/*
 * HomeVoice for Android a UI for Z-Way server
 *
 * Created by Petr Kovář on 17.03.2017.
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
package cz.kovar.petr.homevoice.modules;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.kovar.petr.homevoice.R;
import cz.kovar.petr.homevoice.app.AppConfig;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.utils.SentenceHelper;

public class FeedbackModule extends Module {

    private static final String LOG_TAG = "FeedbackModule";

    private static final String INTENT_COMPLIMENT = "COMPLIMENT";
    private static final String INTENT_CRITICISM  = "CRITICISM";

    public FeedbackModule(Context aContext) {
        super(aContext);
        moduleContext = new FeedbackModuleContext();
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "INITIALIZED");
    }

    @Override
    Set<String> getSupportedIntents() {
        return new HashSet<String>() {{
            add(INTENT_COMPLIMENT);
            add(INTENT_CRITICISM);
        }};
    }

    @Override
    public void handleIntent(UserIntent aIntent) {

        updateContext(moduleContext, aIntent);

        if(!supportsIntent(moduleContext.intent)) return;

        switch(getCurrentState(moduleContext)) {
            case FeedbackModuleContext.STATE_INITIAL_REQUEST:
                switch(moduleContext.intent) {
                    case INTENT_COMPLIMENT:
                        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Compliment received");
                        provideComplimentResponse();
                        break;
                    case INTENT_CRITICISM:
                        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Criticism received");
                        provideCriticismResponse();
                        break;
                }
                break;
            case FeedbackModuleContext.STATE_FAULT_DESCRIPTION:
                String faultDescription = aIntent.getText();
                if(AppConfig.DEBUG) Log.d(LOG_TAG, "Obtained fault description: " + faultDescription);
                reportFaultDescription(faultDescription);
                provideFaultDescriptionResponse();
                break;
        }
    }

    @Override
    void resetModule() {
        moduleContext = new FeedbackModuleContext();
    }

    private void provideComplimentResponse() {
        notifyIntentHandled(SentenceHelper.randomResponse(m_context, R.array.compliment_response));
    }

    private void provideCriticismResponse() {
        setCurrentState(moduleContext, FeedbackModuleContext.STATE_FAULT_DESCRIPTION);
        List<String> response = new ArrayList<>();
        response.add(SentenceHelper.randomResponse(m_context, R.array.criticism_response));
        response.add(SentenceHelper.randomResponse(m_context, R.array.fault_description_request));
        bus.post(new IntentEvent.ContextIncomplete(FeedbackModule.this, response));
    }

    private void provideFaultDescriptionResponse() {
        notifyIntentHandled(SentenceHelper.randomResponse(m_context, R.array.fault_description_response));
    }

    private void reportFaultDescription(String aFaultDescription) {
        if(AppConfig.DEBUG) Log.d(LOG_TAG, "Reporting fault description via Firebase...");
        FirebaseCrash.report(new FaultException(aFaultDescription));
    }

    private void setCurrentState(ModuleContext aContext, int aState) {
        ((FeedbackModuleContext) aContext).currentState = aState;
    }

    private int getCurrentState(ModuleContext aContext) {
        return ((FeedbackModuleContext) aContext).currentState;
    }

    private class FeedbackModuleContext extends ModuleContext {

        static final int STATE_INITIAL_REQUEST   = 0;
        static final int STATE_FAULT_DESCRIPTION = 1;

        private int currentState = STATE_INITIAL_REQUEST;

        void clear() {
            super.clear();
            currentState = STATE_INITIAL_REQUEST;
        }

    }

}