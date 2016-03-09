package me.dags.installer.github;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

import com.google.gson.Gson;

import me.dags.installer.Installer;

public class GithubRequest
{
    public static <T> Optional<T> get(String query, Class<T> type)
    {
        try
        {
            URL url = new URL(query);
            InputStreamReader reader = new InputStreamReader(url.openConnection().getInputStream());
            T t = new Gson().fromJson(reader, type);
            return Optional.of(t);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Installer.phase("request error").log("IOException:\n{}", e.getMessage());
        }
        return Optional.empty();
    }
}
