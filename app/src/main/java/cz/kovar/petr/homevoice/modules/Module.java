package cz.kovar.petr.homevoice.modules;

import android.content.Context;

import java.util.Random;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.zwave.DataContext;

public abstract class Module {

    Context m_context;

    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    public Module(Context aContext) {
        ((ZWayApplication) aContext.getApplicationContext()).getComponent().inject(this);
        m_context = aContext;
    }

    public abstract void handleIntent(UserIntent aIntent);

    String randomResponse(int aStringArrayID) {
        String[] responseArray = m_context.getResources().getStringArray(aStringArrayID);
        return responseArray[new Random().nextInt(responseArray.length)];
    }

}
