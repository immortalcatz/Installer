package me.dags.installer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */

public class Versions
{
    private final Set<String> forgeVersions = new LinkedHashSet<>();

    public Versions()
    {
        File versionsDir = new File(Installer.profile().mcDir, "versions");
        if (!versionsDir.exists())
        {
            return;
        }
        File[] files = versionsDir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                add(file);
            }
        }
    }

    public boolean empty()
    {
        return forgeVersions.isEmpty();
    }

    public Collection<String> getVersions()
    {
        return forgeVersions;
    }

    private void add(File versionDir)
    {
        File[] files = versionDir.listFiles();
        if (files == null)
        {
            return;
        }
        String targetVersion = Installer.profile().minecraft_version;
        for (File file : files)
        {
            if (file.isDirectory())
            {
                continue;
            }
            if (file.getName().endsWith(".json"))
            {
                JsonObject version = read(file);
                if (version.has("id")  && version.has("jar"))
                {
                    String id = version.get("id").getAsString();
                    String jar = version.get("jar").getAsString();
                    if (id.toLowerCase().contains("forge") && jar.equals(targetVersion))
                    {
                        forgeVersions.add(id);
                    }
                }
            }
        }
    }

    private JsonObject read(File file)
    {
        try
        {
            FileReader reader = new FileReader(file);
            JsonElement element = new JsonParser().parse(reader);
            if (element != null && element.isJsonObject())
            {
                return element.getAsJsonObject();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return new JsonObject();
    }
}
