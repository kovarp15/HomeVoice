package cz.kovar.petr.homevoice.modules;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import cz.kovar.petr.homevoice.app.ZWayApplication;
import cz.kovar.petr.homevoice.bus.MainThreadBus;
import cz.kovar.petr.homevoice.bus.events.IntentEvent;
import cz.kovar.petr.homevoice.nlu.UserIntent;
import cz.kovar.petr.homevoice.zwave.DataContext;

public abstract class Module {

    private static final String LOG_TAG = "Module";

    static String ENTITY_YES_NO = "yes_no";
    static String VALUE_YES = "yes";
    static String VALUE_NO = "no";

    Context m_context;

    ModuleContext moduleContext = new ModuleContext();

    @Inject
    DataContext dataContext;
    @Inject
    MainThreadBus bus;

    public Module(Context aContext) {
        ((ZWayApplication) aContext.getApplicationContext()).getComponent().inject(this);
        bus.register(this);
        m_context = aContext;
    }

    abstract Set<String> getSupportedIntents();

    void updateContext(Module.ModuleContext aContext, UserIntent aIntent) {
        updateContextIntent(aContext, aIntent);
    }

    private void updateContextIntent(Module.ModuleContext aContext, UserIntent aIntent) {
        if(aIntent.hasIntent())
            aContext.setIntent(aIntent.getIntent().getValue().toString());
    }

    boolean supportsIntent(String aIntent) {
        return aIntent != null && getSupportedIntents().contains(aIntent);
    }

    public abstract void handleIntent(UserIntent aIntent);

    void notifyIntentHandled(List<String> aResponse) {
        bus.post(new IntentEvent.Handled(aResponse));
    }

    void notifyIntentHandled(final String aResponse) {
        bus.post(new IntentEvent.Handled(new ArrayList<String>() {{
            add(aResponse);
        }}));
    }

    void notifyContextNotReady(Module aModule, final String aRequest) {
        bus.post(new IntentEvent.ContextIncomplete(aModule, new ArrayList<String>() {{
            add(aRequest);
        }}));
    }

    void notifyContextNotReady(Module aModule, final List<String> aRequest) {
        bus.post(new IntentEvent.ContextIncomplete(aModule, aRequest));
    }

    public void reset() {
        resetModule();
    }

    abstract void resetModule();

    class ModuleContext {

        String intent = "";

        void inject(Module.ModuleContext aContext) {
            if(!aContext.intent.isEmpty()) intent = aContext.intent;
        }

        void setIntent(String aIntent) {
            intent = aIntent;
        }

        boolean hasIntent() {
            return !intent.isEmpty();
        }

        void clear() {
            intent = "";
        }

        boolean isEmpty() {
            return intent.isEmpty();
        }

    }

}
