package cz.kovar.petr.homevoice.modules;

import cz.kovar.petr.homevoice.nlu.UserIntent;

public interface Module {

    void handleIntent(UserIntent aIntent);

}
