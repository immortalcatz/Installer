package me.dags.installer;

import java.awt.GridBagLayout;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.google.gson.Gson;

import me.dags.installer.github.GithubRateLimit;

public class Launch
{
    public static void main( String[] args )
    {
        InputStream inputStream = Launch.class.getResourceAsStream("/properties.json");
        Properties properties = new Gson().fromJson(new InputStreamReader(inputStream), Properties.class);
        Installer.applyProperties(properties);
        Installer.phase("startup").log("Loaded properties: {}", properties);
        Installer.logMessage("System information:{}", systemInformation());

        // Liteloader Installer
        String userHome = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        File mcDir;
        String mcDirName = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null)
        {
            mcDir = new File(System.getenv("APPDATA"), mcDirName);
        }
        else if (osType.contains("mac"))
        {
            mcDir = new File(new File(new File(userHome, "Library"), "Application Support"), "minecraft");
        }
        else
        {
            mcDir = new File(userHome, mcDirName);
        }
        //

        Installer.properties().mcDir = mcDir;
        Installer.logMessage("Set minecraft home dir to {}", mcDir);

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame();
            frame.setLayout(new GridBagLayout());
            frame.add(new InstallerPanel());
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

    private static String systemInformation()
    {
        List<String> lines = new ArrayList<>();
        lines.add("Java Version: " + System.getProperty("java.version"));
        lines.add("Java Home: " + System.getProperty("java.home"));

        lines.add("OS Name: " + System.getProperty("os.name"));
        lines.add("OS Version: " + System.getProperty("os.version"));
        lines.add("OS Architecture: " + System.getProperty("os.arch"));

        Optional<GithubRateLimit> rateLimit = GithubRateLimit.getLimit();
        if (rateLimit.isPresent())
        {
            lines.add("Github API Calls Remaining: " + rateLimit.get().rate.remaining);
            lines.add("Github API Calls Limit: " + rateLimit.get().rate.limit);
            lines.add("Github API Reset Date: " + rateLimit.get().rate.resetDate());;
        }
        else
        {
            lines.add("Github API: NO CONNECTION! THIS IS BAD!");
        }

        StringBuilder sb = new StringBuilder("\n");
        lines.forEach(s -> sb.append(sb.length() > 0 ? "\n" : "").append(s));
        return sb.append("\n").toString();
    }
}