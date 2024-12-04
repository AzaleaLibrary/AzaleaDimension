package net.azalealibrary.dimension;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DimensionManager {
    private static final Logger logger = Logger.getLogger(DimensionManager.class.getName());
    private final Map<String, World> dimensions = new HashMap<>();
    private final String worldFolderPath;
    private final String mainWorldName;

    public DimensionManager(String worldFolderPath) {
        this.worldFolderPath = worldFolderPath;
        this.mainWorldName = findLevelName();
        if (mainWorldName != null) dimensions.put(mainWorldName, Bukkit.getWorld(mainWorldName));

        File worldFolder = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + File.separator + worldFolderPath);
        if (!worldFolder.mkdir()) {
            File[] worldDirectories = worldFolder.listFiles(File::isDirectory);
            if (worldDirectories != null) {
                for (File worldDirectory : worldDirectories) {
                    String worldName = worldFolderPath + "/" + worldDirectory.getName();
                    World world = new WorldCreator(worldName).createWorld();
                    dimensions.put(worldName, world);
                }
            }
        }
    }

    public DimensionManager() {
        this("azalea-worlds");
    }

    public Map<String, World> getDimensions() {
        return dimensions;
    }

    public World getMainWorld() {
        return dimensions.get(mainWorldName);
    }

    public World getWorld(String worldName) {
        return dimensions.get(worldName);
    }

    public void createWorld(String worldName) {
        World world = new WorldCreator(worldFolderPath + "/" + worldName).createWorld();
        dimensions.put(worldFolderPath + "/" + worldName, world);
    }

    public boolean deleteWorld(String worldName) {
        if (worldName.equals(mainWorldName)) {
            logger.log(Level.INFO, "Attempted to delete the main world");
            return false;
        }
        World world = dimensions.get(worldName);
        if (world == null) return false;
        Bukkit.unloadWorld(world, false);
        File worldDir = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + File.separator + worldFolderPath);
        dimensions.remove(worldName);
        return FileUtil.deleteDirectory(worldDir);
    }

    private String findLevelName() {
        try {
            BufferedReader is = new BufferedReader(new FileReader("server.properties"));
            Properties props = new Properties();
            props.load(is);
            is.close();
            return props.getProperty("level-name");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not find level name", e);
            return null;
        }
    }
}