package me.zort.devroomtrial.boot.inclusions;

import me.zort.devroomtrial.DevRoomTrial;
import me.zort.devroomtrial.DevRoomTrialInclusion;
import me.zort.devroomtrial.data.MysqlCredentials;
import me.zort.devroomtrial.data.MysqlDataBridge;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class MysqlInclusion implements DevRoomTrialInclusion {

    @Override
    public boolean load(DevRoomTrial base) {
        FileConfiguration config = base.getConfig();
        MysqlDataBridge dataBridge = new MysqlDataBridge();
        ConfigurationSection section = config.getConfigurationSection("sql");
        Logger logger = base.getLogger();
        logger.info("Connecting to MySQL...");
        if(section == null) return false;
        MysqlCredentials credentials = new MysqlCredentials(
                section.getString("url", "localhost"),
                section.getInt("port", 3306),
                section.getString("database", "database"),
                section.getString("user", "user"),
                section.getString("password", "password")
        );
        if(dataBridge.connect(credentials) != null) {
            logger.info("Successfully connected to MySQL.");
            return true;
        }
        logger.info("Could not connect to MySQL");
        return false;
    }

    @Override
    public void unload(DevRoomTrial base, boolean err) {
        if(base.getSql() == null) return;
        base.getSql().close();
        base.setSql(null);
    }

    @Nullable
    @Override
    public String canBoot(DevRoomTrial base) {
        return base.getSql() != null
                ? "SQL is already loaded. Please unload first."
                : null;
    }

}
