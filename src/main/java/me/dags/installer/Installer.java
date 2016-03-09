package me.dags.installer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class Installer
{
    private static final Installer instance = new Installer();

    private final Logger logger = Logger.getLogger("Installer");
    private final FileWriter filewriter;
    private final StringBuffer buffer = new StringBuffer();
    private Properties properties = new Properties();
    private String phase = "[STARTUP]";

    public Installer()
    {
        File logfile = new File("install.log");
        FileWriter filewriter = null;
        try
        {
            if (logfile.exists() && logfile.createNewFile());
            filewriter = new FileWriter(logfile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            instance.buffer.append(e.getMessage());
        }
        this.filewriter = filewriter;
    }

    public static Installer phase(String phase)
    {
        instance.phase = "[" + phase.toUpperCase() + "]";
        return instance;
    }

    public static void applyProperties(Properties properties)
    {
        if (properties == null)
        {
            return;
        }
        instance.properties = properties;
    }

    public static Properties properties()
    {
        return instance.properties;
    }

    public static String getLog()
    {
        return instance.buffer.toString();
    }

    public static void logMessage(String message, Object...args)
    {
        instance.log(message, args);
    }

    public void log(String message, Object...args)
    {
        String log = instance.phase + " " + format(message, args);
        instance.logger.info(log);
        instance.buffer.append(log).append('\n');
        try
        {
            if (instance.filewriter != null)
            {
                instance.filewriter.write(log);
                instance.filewriter.write('\n');
                instance.filewriter.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            instance.buffer.append(e.getMessage());
        }
    }

    private static String format(String in, Object... args)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = 0; i < in.length(); i++)
        {
            char c = in.charAt(i);
            if (c == '{' && i + 1 < in.length() && in.charAt(i + 1) == '}' && j < args.length)
            {
                i++;
                sb.append(args[j++]);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
