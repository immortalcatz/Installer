package me.dags.installer.install;

import me.dags.installer.Installer;
import me.dags.installer.github.GithubTag;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Optional;

public class Install extends Action
{
    private final File outDir;
    private final JFrame parent;
    private final JButton ok;
    private final JProgressBar progressBar = new JProgressBar();
    private final String forgeProfile;
    private final boolean createProfile;

    public Install(JFrame parent, JButton ok, File outDir, String selectedForgeProfile)
    {
        this.parent = parent;
        this.outDir = outDir;
        this.ok = ok;
        this.progressBar.setPreferredSize(new Dimension(300, 60));
        this.progressBar.setBounds( 20, 35, 260, 20 );
        this.progressBar.setStringPainted(true);
        this.add(progressBar);
        this.forgeProfile = selectedForgeProfile;
        this.createProfile = !forgeProfile.isEmpty();
    }

    @Override
    public void perform()
    {
        Installer.log("[INSTALL] Starting installation...");
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run()
    {
        try
        {
            Installer.log("[CONNECT] Searching for latest tag...");
            Optional<GithubTag> tag = GithubTag.getLatest();
            if (!tag.isPresent() || !tag.get().getZipUrl().isPresent())
            {
                Installer.log("[CONNECT] No tag found!");
                progressBar.setString("Nothing to download! Check the logs");
                ok.setEnabled(false);
                ok.setText("Done!");
                return;
            }
            String format = "%s/" + (createProfile ? "4" : "3") + " %s";

            progressBar.setString(String.format(format, 1, "Downloading..."));
            Thread.sleep(1000);
            progressBar.setString(null);

            InstallProcess installProcess = new InstallProcess(outDir, forgeProfile);

            int attempts = 1;
            while (attempts < 5)
            {
                Installer.log("[DOWNLOAD] Download attempt {}", attempts++);
                try
                {
                    progressBar.setString(null);
                    installProcess.download(tag.get(), progressBar);
                    break;
                }
                catch (Throwable t)
                {
                    Installer.log("[DOWNLOAD] Error occurred whilst downloading: {}", t.getMessage());
                    progressBar.setString("Download attempt: " + attempts);
                    Thread.sleep(1000);
                }
                if (attempts == 5)
                {
                    Installer.log("[ERROR] Unable to download from github!");
                    return;
                }
            }

            progressBar.setString(String.format(format, 1, "Downloading complete!"));
            Thread.sleep(1000);

            progressBar.setString(String.format(format, 2, "Extracting..."));
            Thread.sleep(1000);

            progressBar.setString(null);
            installProcess.extract(progressBar);

            progressBar.setString(String.format(format, 2, "Extract complete!"));
            Thread.sleep(1000);

            progressBar.setString(String.format(format, 3, "Removing temporary files..."));
            installProcess.removeTempFiles();

            progressBar.setString(String.format(format, 3, "Temporary files removed!"));
            Thread.sleep(1000);

            if (!createProfile)
            {
                progressBar.setString("Finished!");
                Thread.sleep(1500);
                parent.setVisible(false);
                return;
            }

            progressBar.setString(String.format(format, 4, "Creating launcher profile..."));
            Thread.sleep(500);
            installProcess.addProfile();
            Thread.sleep(500);

            progressBar.setString("Finished!");
            Thread.sleep(1500);
            parent.setVisible(false);

            ok.setEnabled(false);
            ok.setText("Done!");
        }
        catch (Throwable e)
        {
            progressBar.setString("An error occurred! Check the logs");
            e.printStackTrace();
        }
    }
}
