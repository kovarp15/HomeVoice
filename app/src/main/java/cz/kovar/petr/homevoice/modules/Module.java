package cz.kovar.petr.homevoice.modules;

import android.content.Context;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.zwave.DataContext;

public abstract class Module {

    static String ENTITY_YES_NO = "yes_no";
    static String ENTITY_VALUE_YES = "yes";
    static String ENTITY_VALUE_NO = "no";

    Context m_context;

    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    protected boolean followup = false;

    protected Set<String> m_supportedIntents = new HashSet<>();

    public Module(Context aContext) {
        ((ZWayApplication) aContext.getApplicationContext()).getComponent().inject(this);
        m_context = aContext;
    }

    void setSupportedIntents(Set<String> aSupportedIntents) {
        m_supportedIntents.clear();
        m_supportedIntents.addAll(aSupportedIntents);
    }

    boolean supportsIntent(String aIntent) {
        return aIntent != null && m_supportedIntents.contains(aIntent);
    }

    public abstract void handleIntent(UserIntent aIntent);

    String randomResponse(int aStringArrayID) {
        String[] responseArray = m_context.getResources().getStringArray(aStringArrayID);
        return responseArray[new Random().nextInt(responseArray.length)];
    }

}
