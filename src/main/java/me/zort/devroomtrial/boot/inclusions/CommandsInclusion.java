package me.zort.devroomtrial.boot.inclusions;

import me.zort.devroomtrial.DevRoomTrial;
import me.zort.devroomtrial.DevRoomTrialInclusion;
import me.zort.devroomtrial.spigot.commands.TrialCommandExecutor;
import org.jetbrains.annotations.Nullable;

public class CommandsInclusion implements DevRoomTrialInclusion {

    private boolean firstLoad = true;

    @Override
    public boolean load(DevRoomTrial base) {
        if(firstLoad) {
            base.getCommand("rip").setExecutor(new TrialCommandExecutor(base));
        }
        firstLoad = false;
        return true;
    }

    @Override
    public void unload(DevRoomTrial base, boolean err) {}

    @Nullable
    @Override
    public String canBoot(DevRoomTrial base) {
        return null;
    }

}
