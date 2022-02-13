package me.zort.devroomtrial.spigot.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.zort.devroomtrial.data.annotation.EntityAutoConfigure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@EntityAutoConfigure(idClass = String.class)
@Entity(name = "death_locations")
@Setter
@Getter
public class DeathLocationEntity {

    @Id
    @Column(nullable = false)
    private String name;

    @Convert(converter = DeathLocationEntityLocationConverter.class)
    @Column(nullable = false)
    private Location loc;

    @Convert(converter = DeathLocationEntityReplacementConverter.class)
    private Replacement replacement;

    @Column(nullable = false)
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
            }
        }

    }

}
