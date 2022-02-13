package me.zort.devroomtrial.spigot.data;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

import java.sql.SQLException;

import static me.zort.devroomtrial.DevRoomTrial.GSON;

public class DeathLocationEntityReplacementPersister extends StringType {

    private static DeathLocationEntityReplacementPersister SINGLETON_INSTANCE = null;

    public static DeathLocationEntityReplacementPersister getSingleton() {
        if(SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new DeathLocationEntityReplacementPersister();
        }
        return SINGLETON_INSTANCE;
    }

    protected DeathLocationEntityReplacementPersister() {
        super(SqlType.STRING, new Class<?>[0]);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object attribute) throws SQLException {
        DeathLocationEntity.Replacement replacement = (DeathLocationEntity.Replacement) attribute;
        if(replacement == null) return null;
        return GSON.toJson(replacement);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object attribute, int columnPos) {
        String sqlArg = (String) attribute;
        if(sqlArg == null) return null;
        return GSON.fromJson(sqlArg, DeathLocationEntity.Replacement.class);
    }

}
