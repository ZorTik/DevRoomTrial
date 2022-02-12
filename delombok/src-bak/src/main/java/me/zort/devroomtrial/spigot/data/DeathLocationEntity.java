package me.zort.devroomtrial.spigot.data;

import lombok.Getter;
import lombok.Setter;
import me.zort.devroomtrial.data.annotation.EntityAutoConfigure;
import org.bukkit.Location;

import javax.persistence.*;

@EntityAutoConfigure(idClass = String.class)
@Entity(name = "death_locations")
@Setter
@Getter
public class DeathLocationEntity {

    @Id
    @Column(nullable = false)
    private String name;

    @Convert(converter = DeathLocationEntityLocationConverter.class)
    private Location loc;

}
