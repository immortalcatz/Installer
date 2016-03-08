package me.dags.installer;

import com.google.gson.Gson;
import me.dags.installer.github.GithubRateLimit;
import me.dags.installer.ui.InstallerPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class Launch
{
    public static void main( String[] args )
    {
        InputStream inputStream = Launch.class.getResourceAsStream("/properties.json");
        Properties properties = new Gson().fromJson(new InputStreamReader(inputStream), Properties.class);
        Installer.applyProperties(properties);
        Installer.log("Loaded properties: {}", properties);

        // Liteloader Installer
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        File mcDir;
        String mcDirName = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null)
        {
            mcDir = new File(System.getenv("APPDATA"), mcDirName);
        }
        else if (osType.contains("mac"))
        {
            mcDir = new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
        }
        else
        {
            mcDir = new File(userHomeDir, mcDirName);
        }
        //

        Installer.profile().mcDir = mcDir;

        try
        {
            GithubRateLimit.printLimit();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame();
            frame.setLayout(new GridBagLayout());
            frame.add(new InstallerPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}