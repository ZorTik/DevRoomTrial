package me.zort.devroomtrial.spigot.data;

import me.zort.devroomtrial.data.annotation.EntityAutoConfigure;
import org.bukkit.Location;
import javax.persistence.*;

@EntityAutoConfigure(idClass = String.class)
@Entity(name = "death_locations")
public class DeathLocationEntity {
    @Id
    @Column(nullable = false)
    private String name;
    @Convert(converter = DeathLocationEntityLocationConverter.class)
    private Location loc;

    @SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @SuppressWarnings("all")
    public void setLoc(final Location loc) {
        this.loc = loc;
    }

    @SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("all")
    public Location getLoc() {
        return this.loc;
    }
}
