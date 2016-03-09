package me.dags.installer;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.dags.installer.github.GithubTag;

public class InstallProcess extends JPanel implements Runnable
{
    private final JFrame parent;
    private final JButton ok;
    private final JProgressBar progress = new JProgressBar();
    private final String forgeProfile;
    private final boolean createProfile;

    private final File tempDir;
    private final File tempFile;

    private File targetDir;

    public InstallProcess(JFrame parent, JButton ok, File targetDir, String forgeProfile)
    {
        this.parent = parent;
        this.ok = ok;
        this.progress.setPreferredSize(new Dimension(300, 60));
        this.progress.setBounds( 20, 35, 260, 20 );
        this.progress.setStringPainted(true);
        this.add(progress);
        this.forgeProfile = forgeProfile;
        this.createProfile = !forgeProfile.isEmpty();
        this.tempDir = new File(targetDir, "temp_" + System.currentTimeMillis());
        this.tempFile = new File(tempDir, System.currentTimeMillis() + ".zip");
        this.targetDir = targetDir;
    }

    public void perform()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        try
        {
            Installer.phase("get tag").log("Searching for latest tag...");
            Optional<GithubTag> tag = GithubTag.getLatest();
            if (!tag.isPresent() || !tag.get().getZipUrl().isPresent())
            {
                Installer.phase("error").log("No tag found!");
                progress.setString("Nothing to download! Check the logs");
                ok.setEnabled(false);
                ok.setText("Done!");
                return;
            }

            if (!targetDir.equals(Installer.properties().mcDir))
            {
                String name = Installer.properties().profile_name;
                String version = tag.get().name;
                String dirName = (name + "-" + version).replaceAll("^A-Za-z0-9\\.\\-\\_", "");
                targetDir = new File(targetDir, dirName.toLowerCase());
                targetDir.mkdirs();
            }

            String format = "%s/" + (createProfile ? "4" : "3") + " %s";
            progress.setString(String.format(format, 1, "Downloading..."));
            Thread.sleep(1000);
            progress.setString(null);

            int attempts = 1;
            while (attempts < 5)
            {
                Installer.phase("connect").log("Download attempt {}", attempts++);
                try
                {
                    progress.setString(null);
                    this.download(tag.get());
                    break;
                }
                catch (Throwable t)
                {
                    Installer.phase("failed connect").log("Error occurred whilst downloading!");
                    progress.setString("Download attempt: " + attempts);
                    Thread.sleep(1000);
                }
                if (attempts == 5)
                {
                    Installer.phase("error").log("Unable to download from github!");
                    return;
                }
            }

            progress.setString(String.format(format, 1, "Downloading complete!"));
            Thread.sleep(1000);

            progress.setString(String.format(format, 2, "Extracting..."));
            Thread.sleep(1000);

            progress.setString(null);
            this.extract();

            progress.setString(String.format(format, 2, "Extract complete!"));
            Thread.sleep(1000);

            progress.setString(String.format(format, 3, "Removing temporary files..."));
            this.removeTempFiles();

            progress.setString(String.format(format, 3, "Temporary files removed!"));
            Thread.sleep(1000);

            if (!createProfile)
            {
                progress.setString("Finished!");
                Thread.sleep(1500);
                parent.setVisible(false);
                return;
            }

            progress.setString(String.format(format, 4, "Creating launcher profile..."));
            Thread.sleep(500);
            this.addProfile();
            Thread.sleep(500);

            progress.setString("Finished!");
            Thread.sleep(1500);
            parent.setVisible(false);

            ok.setEnabled(false);
            ok.setText("Done!");
        }
        catch (Throwable e)
        {
            progress.setString("An error occurred! Check the logs");
            e.printStackTrace();
        }
    }

    public void download(GithubTag tag) throws IOException, InterruptedException
    {
        Optional<URL> optionalUrl = tag.getZipUrl();
        if (!optionalUrl.isPresent())
        {
            return;
        }
        Installer.phase("download init").log("Creating temp files and folders...");
        targetDir.mkdirs();
        tempDir.mkdirs();

        URL url = optionalUrl.get();
        Installer.phase("download connect").log("Opening connection to {}", url.toString());

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int length = Integer.valueOf(connection.getHeaderFields().get("Content-Length").get(0));

        int segment = length / 100;
        progress.setMinimum(0);
        progress.setMaximum(length);

        Installer.phase("download").log("Commencing download...");
        InputStream input = connection.getInputStream();
        ReadableByteChannel channel = Channels.newChannel(input);
        FileOutputStream output = new FileOutputStream(tempFile);
        FileChannel fileChannel = output.getChannel();

        for (int i = 0; i <= 100; i++)
        {
            fileChannel.transferFrom(channel, i * segment, segment);
            progress.setValue(i * segment);
        }

        Installer.logMessage("Download completed");
        progress.setString("Complete!");
        fileChannel.close();
        channel.close();
        output.flush();
        output.close();
        input.close();
    }

    public void extract() throws IOException
    {
        if (!tempFile.exists())
        {
            return;
        }
        Installer.phase("extract").log("Extracting {}", tempFile);
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
            File targetFile = new File(targetDir, path);
            targetFile.getParentFile().mkdirs();

            Installer.logMessage("Extracting file {}", targetFile);

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
        Installer.logMessage("Extraction complete!");
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
        Installer.phase("profile get").log("Adding launcher profile for {}...", Installer.properties().profile_name);
        File profilesFile = new File(Installer.properties().mcDir, "launcher_profiles.json");
        if (!profilesFile.exists())
        {
            Installer.phase("error").log("File does not exist: {}", profilesFile);
            return;
        }
        JsonObject profile = new JsonObject();
        profile.addProperty("name", Installer.properties().profile_name);
        profile.addProperty("gameDir", targetDir.getAbsolutePath());
        profile.addProperty("lastVersionId", forgeProfile);
        Installer.phase("profile edit").log("Appending profile {}", profile.toString());

        FileInputStream in = new FileInputStream(profilesFile);
        JsonObject profiles = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
        profiles.get("profiles").getAsJsonObject().add(Installer.properties().profile_name, profile);
        in.close();

        Installer.phase("profle write").log("Writing profiles to disk...");
        FileWriter writer = new FileWriter(profilesFile);
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(profiles));
        writer.flush();
        writer.close();
    }
}
