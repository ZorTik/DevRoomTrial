package me.zort.devroomtrial.boot.inclusions;

import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.zort.devroomtrial.DevRoomTrial;
import me.zort.devroomtrial.DevRoomTrialInclusion;
import me.zort.devroomtrial.data.MysqlCredentials;
import me.zort.devroomtrial.data.MysqlDataBridge;
import me.zort.devroomtrial.spigot.data.DeathLocationEntity;
import me.zort.devroomtrial.spigot.data.DeathLocationEntityLocationPersister;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
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
        /*DataPersisterManager.registerDataPersisters(
                DeathLocationEntityLocationPersister.getSingleton()
        );*/
        JdbcPooledConnectionSource connection;
        if((connection = dataBridge.connect(credentials, "mariadb")) != null) {
            try {
                TableUtils.createTableIfNotExists(connection, DeathLocationEntity.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            base.setSql(dataBridge);
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

    @Override
    public int getPriority() {
        return 3;
    }

}
