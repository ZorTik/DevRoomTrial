package me.zort.devroomtrial.spigot.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import static me.zort.devroomtrial.DevRoomTrial.GSON;

@Converter
public class DeathLocationEntityLocationConverter implements AttributeConverter<Location, String> {

    @Override
    public String convertToDatabaseColumn(Location attribute) {
        World world = attribute.getWorld();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("world", world != null ? world.getName() : "world");
        jsonObject.addProperty("x", attribute.getX());
        jsonObject.addProperty("y", attribute.getY());
        jsonObject.addProperty("z", attribute.getZ());
        return GSON.toJson(jsonObject);
    }

    @Override
    public Location convertToEntityAttribute(String dbData) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(dbData);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String worldName = jsonObject.get("world").getAsString();
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        World w = Bukkit.getWorld(worldName);
        if(w == null) {
            w = Bukkit.getWorld("world");
        }
        return new Location(w, x, y, z);
    }

}
