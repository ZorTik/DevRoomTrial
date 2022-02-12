package me.zort.devroomtrial;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zort.devroomtrial.data.MysqlDataBridge;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class DevRoomTrial extends JavaPlugin {
    private static final String PCG_URL = DevRoomTrial.class.getPackage().getName();
    public static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().create();
    public static DevRoomTrial INSTANCE = null;
    private final Reflections reflections = new Reflections(new ConfigurationBuilder().filterInputsBy(new FilterBuilder().includePackage(PCG_URL)).setUrls(ClasspathHelper.forPackage(PCG_URL)).setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner()));
    private DevRoomTrialBootstrap bootstrap;
    private MysqlDataBridge sql;

    @Override
    public void onEnable() {
        INSTANCE = this;
        File configFile = new File(getDataFolder().getAbsolutePath() + "/config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            saveDefaultConfig();
        } else {
            getConfig().options().copyDefaults(true);
            try {
                getConfig().save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bootstrap = new DevRoomTrialBootstrap(this);
        List<String> errors = bootstrap.reload();
        Logger logger = getLogger();
        if (!errors.isEmpty()) {
            logger.info("There were " + errors.size() + " errors while starting plugin:");
            errors.forEach(System.out::println);
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // TODO: Continue
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.unload();
        }
    }

    @SuppressWarnings("all")
    public Reflections getReflections() {
        return this.reflections;
    }

    @SuppressWarnings("all")
    public DevRoomTrialBootstrap getBootstrap() {
        return this.bootstrap;
    }

    @SuppressWarnings("all")
    public void setSql(final MysqlDataBridge sql) {
        this.sql = sql;
    }

    @SuppressWarnings("all")
    public MysqlDataBridge getSql() {
        return this.sql;
    }
}
