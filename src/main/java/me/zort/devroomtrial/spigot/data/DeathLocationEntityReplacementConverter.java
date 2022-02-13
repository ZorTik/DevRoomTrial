package me.zort.devroomtrial.spigot.data;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import static me.zort.devroomtrial.DevRoomTrial.GSON;

@Converter
public class DeathLocationEntityReplacementConverter implements AttributeConverter<DeathLocationEntity.Replacement, String> {

    @Override
    public String convertToDatabaseColumn(DeathLocationEntity.Replacement attribute) {
        return GSON.toJson(attribute);
    }

    @Override
    public DeathLocationEntity.Replacement convertToEntityAttribute(String dbData) {
        return GSON.fromJson(dbData, DeathLocationEntity.Replacement.class);
    }

}
