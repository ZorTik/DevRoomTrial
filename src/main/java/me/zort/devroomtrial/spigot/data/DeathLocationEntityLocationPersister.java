package me.zort.devroomtrial.spigot.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.SQLException;

import static me.zort.devroomtrial.DevRoomTrial.GSON;

public class DeathLocationEntityLocationPersister extends StringType {

    private static DeathLocationEntityLocationPersister SINGLETON_INSTANCE = null;

    public static DeathLocationEntityLocationPersister getSingleton() {
        if(SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new DeathLocationEntityLocationPersister();
        }
        return SINGLETON_INSTANCE;
    }

    protected DeathLocationEntityLocationPersister() {
        super(SqlType.STRING, new Class<?>[0]);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object attribute) throws SQLException {
        Location loc = (Location) attribute;
        if(loc == null) return null;
        World world = loc.getWorld();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("world", world != null ? world.getName() : "world");
        jsonObject.addProperty("x", loc.getX());
        jsonObject.addProperty("y", loc.getY());
        jsonObject.addProperty("z", loc.getZ());
        return GSON.toJson(jsonObject);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object attribute, int columnPos) {
        String sqlArg = (String) attribute;
        if(sqlArg == null) return null;
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(sqlArg);
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
