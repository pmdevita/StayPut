package nl.zandervdm.stayput.Utils;

import nl.zandervdm.stayput.Main;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

public class MVManager {
    MultiverseCoreApi core;
    public MVManager(Main main) {
        try {
            core = MultiverseCoreApi.get();
        } catch (IllegalStateException e) {
            this.core = null;
            main.getLogger().severe("StayPut loaded but Multiverse did not, something is wrong.");
        }
    }

    public MultiverseCoreApi getCore() {return this.core;}

    public boolean loadedMultiverse() {
        return this.core != null;
    }

}
