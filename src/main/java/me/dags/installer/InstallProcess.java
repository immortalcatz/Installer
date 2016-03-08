package me.dags.installer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dags.installer.github.GithubTag;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InstallProcess
{
    private final File extractDir;
    private final File tempDir;
    private final File tempFile;
    private final String forgeProfile;

    public InstallProcess(File targetDir, String forgeProfile)
    {
        this.extractDir = new File(targetDir, Installer.profile().profile_name.replace(" ", "_").toLowerCase());
        this.tempDir = new File(targetDir, "_temp");
        this.tempFile = new File(tempDir, System.currentTimeMillis() + ".zip");
        this.forgeProfile = forgeProfile;
    }

    public void download(GithubTag tag, JProgressBar progress) throws IOException, InterruptedException
    {
        Optional<URL> optionalUrl = tag.getZipUrl();
        if (!optionalUrl.isPresent())
        {
            return;
        }
        Installer.log("Creating temp files and folders...");
        extractDir.mkdirs();
        tempDir.mkdirs();

        URL url = optionalUrl.get();
        Installer.log("[DOWNLOAD] Opening connection to {}", url.toString());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int length = Integer.valueOf(connection.getHeaderFields().get("Content-Length").get(0));

        int segment = length / 100;
        progress.setMinimum(0);
        progress.setMaximum(length);

        Installer.log("[DOWNLOAD] Commencing download...");
        InputStream input = connection.getInputStream();
        ReadableByteChannel channel = Channels.newChannel(input);
        FileOutputStream output = new FileOutputStream(tempFile);
        FileChannel fileChannel = output.getChannel();

        for (int i = 0; i <= 100; i++)
        {
            fileChannel.transferFrom(channel, i * segment, segment);
            progress.setValue(i * segment);
        }

        Installer.log("[DOWNLOAD] Download completed");
        progress.setString("Complete!");
        fileChannel.close();
        channel.close();
        output.flush();
        output.close();
        input.close();
    }

    public void extract(JProgressBar progress) throws IOException
    {
        if (!tempFile.exists())
        {
            return;
        }
        Installer.log("[EXTRACT] Extracting {}", tempFile);
        ZipFile zipFile = new ZipFile(tempFile);
        progress.setValue(0);
        progress.setMinimum(0);
        progress.setMaximum(zipFile.size());

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            progress.setValue(progress.getValue() + 1);
            ZipEntry e = entries.nextElement();
            if (e.isDirectory() || e.getName().endsWith(".gitignore"))
            {
                continue;
            }
            int index = e.getName().indexOf('/');
            String path = index > 0 ? e.getName().substring(index + 1) : e.getName();
            File targetFile = new File(extractDir, path);
            targetFile.getParentFile().mkdirs();

            Installer.log("[EXTRACT] Extracting file {}", targetFile);

            InputStream input = zipFile.getInputStream(e);
            ReadableByteChannel channel = Channels.newChannel(input);

            FileOutputStream output = new FileOutputStream(targetFile);
            FileChannel fileChannel = output.getChannel();
            fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);

            fileChannel.close();
            channel.close();
            output.flush();
            output.close();
            input.close();
        }
        zipFile.close();
        Installer.log("[EXTRACT] Extraction complete!");
    }

    public void removeTempFiles()
    {
        File[] files = tempDir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                file.delete();
            }
        }
        tempDir.delete();
    }

    public void addProfile() throws IOException
    {
        Installer.log("Adding launcher profile for {}...", Installer.profile().profile_name);
        File profilesFile = new File(Installer.profile().mcDir, "launcher_profiles.json");
        if (!profilesFile.exists())
        {
            Installer.log("File does not exist: {}", profilesFile);
            return;
        }
        JsonObject profile = new JsonObject();
        profile.addProperty("name", Installer.profile().profile_name);
        profile.addProperty("gameDir", extractDir.getAbsolutePath());
        profile.addProperty("lastVersionId", forgeProfile);
        Installer.log("Appending profile {}", profile.toString());

        FileInputStream in = new FileInputStream(profilesFile);
        JsonObject profiles = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
        profiles.get("profiles").getAsJsonObject().add(Installer.profile().profile_name, profile);
        in.close();

        Installer.log("Writing profiles to disk...");
        FileWriter writer = new FileWriter(profilesFile);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(profiles));
        writer.flush();
        writer.close();
    }
}
