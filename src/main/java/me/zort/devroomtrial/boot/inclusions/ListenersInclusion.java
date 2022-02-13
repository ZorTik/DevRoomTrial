package me.zort.devroomtrial.boot.inclusions;

import me.zort.devroomtrial.DevRoomTrial;
import me.zort.devroomtrial.DevRoomTrialInclusion;
import me.zort.devroomtrial.spigot.DeathLocationsService;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

public class ListenersInclusion implements DevRoomTrialInclusion {

    @Override
    public boolean load(DevRoomTrial base) {
        PluginManager pluginManager = base.getServer().getPluginManager();
        DeathLocationsService locationsService = new DeathLocationsService(base, base.getSql());
        base.setDeathLocations(locationsService);
        pluginManager.registerEvents(locationsService, base);
        return true;
    }

    @Override
    public void unload(DevRoomTrial base, boolean err) {
        HandlerList.unregisterAll(base);
    }

    @Nullable
    @Override
    public String canBoot(DevRoomTrial base) {
        return null;
    }

    @Override
    public int getPriority() {
        return 2;
    }

}
