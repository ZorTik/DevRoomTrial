package me.zort.devroomtrial.spigot.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.zort.devroomtrial.data.annotation.EntityAutoConfigure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@EntityAutoConfigure(idClass = String.class)
@DatabaseTable(tableName = "death_locations")
@Setter
@Getter
public class DeathLocationEntity {

    @DatabaseField(id = true, canBeNull = false)
    private String name;

    @DatabaseField(persisterClass = DeathLocationEntityLocationPersister.class)
    private Location loc;

    @DatabaseField(persisterClass = DeathLocationEntityReplacementPersister.class)
    private Replacement replacement;

    @DatabaseField(canBeNull = false)
    private long millis = System.currentTimeMillis();

    public boolean hasExpired() {
        long fromInit = System.currentTimeMillis() - millis;
        return fromInit >= TimeUnit.MINUTES.toMillis(10);
    }

    @Data
    public static class Replacement implements Serializable {

        public static Replacement fromBlock(Location loc) {
            return fromBlock(loc.getBlock());
        }

        public static Replacement fromBlock(Block block) {
            Replacement replacement = new Replacement();
            replacement.setMat(block.getType().name());
            String blockDataSerialized = block.getBlockData().getAsString();
            replacement.setBlockData(blockDataSerialized);
            return replacement;
        }

        private String mat;
        private String blockData;

        public void apply(Location loc) {
            apply(loc.getBlock());
        }

        public void apply(Block block) {
            Material mat = Material.matchMaterial(this.mat.toUpperCase());
            if(mat != null) {
                block.setType(mat, false);
                BlockData blockDataDeserialized = Bukkit.createBlockData(blockData);
                block.setBlockData(blockDataDeserialized, false);
                Block signBlock = block.getLocation().clone().subtract(0.0, 0.0, 1.0).getBlock();
                signBlock.setType(Material.AIR);
            }
        }

    }

}
