package kikijiji.reachdisplay.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Files;

import net.fabricmc.loader.api.FabricLoader;

import kikijiji.reachdisplay.ReachDisplay;


public class ReachDisplayConfigManager
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "reach-display.json";


    /* ----- 불러오기 ----- */
    public static ReachDisplayConfig load()
    {
        Path configDir  = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(FILE_NAME);

        if (Files.exists(configPath))
        {
            try (Reader reader = Files.newBufferedReader(configPath))
            {
                ReachDisplayConfig config = GSON.fromJson(reader, ReachDisplayConfig.class);
                if (config != null)
                {
                    return config;
                }
            }
            catch (IOException exception)
            {
                ReachDisplay.LOGGER.error("Failed to load ReachDisplay config from {}", configPath, exception);
            }
        }

        ReachDisplayConfig config = new ReachDisplayConfig();
        save(config);
        return config;
    }

    /* ----- 저장 ----- */
    public static void save(ReachDisplayConfig config)
    {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(FILE_NAME);

        try
        {
            if (!Files.exists(configDir))
            {
                Files.createDirectories(configDir);
            }

            try (Writer writer = Files.newBufferedWriter(configPath))
            {
                GSON.toJson(config, writer);
            }
        }
        catch (IOException exception)
        {
            ReachDisplay.LOGGER.error("Failed to save ReachDisplay config to {}", configPath, exception);
        }
    }
}