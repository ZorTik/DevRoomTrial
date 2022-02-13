package me.zort.devroomtrial.boot.inclusions;

import me.zort.devroomtrial.DevRoomTrial;
import me.zort.devroomtrial.DevRoomTrialInclusion;
import me.zort.devroomtrial.spigot.DeathLocationsService;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Nullable;

public class RecipesInclusion implements DevRoomTrialInclusion {

    @Override
    public boolean load(DevRoomTrial base) {
        DeathLocationsService locationsService = base.getDeathLocations();
        ItemStack deathKeyItem = locationsService.buildDeathKey();
        NamespacedKey key = new NamespacedKey(base, "death-key");
        ShapelessRecipe recipe = new ShapelessRecipe(key, deathKeyItem);
        recipe.addIngredient(Material.TOTEM_OF_UNDYING);
        recipe.addIngredient(Material.EMERALD);
        base.getServer().addRecipe(recipe);
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
        return 1;
    }

}
