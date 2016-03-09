package me.dags.installer.github;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import me.dags.installer.Installer;

public class GithubTag
{
    private String name = "";
    private String zipball_url = "";
    private String tarball_url = "";
    private Commit commit = new Commit();

    public Optional<URL> getZipUrl()
    {
        return getUrl(zipball_url);
    }

    public Optional<URL> getTarUrl()
    {
        return getUrl(tarball_url);
    }

    public boolean valid()
    {
        return !name.isEmpty() && !zipball_url.isEmpty() && !tarball_url.isEmpty();
    }

    @Override
    public String toString()
    {
        return "name=" + name + ",zip_url=" + zipball_url;
    }

    private static Optional<URL> getUrl(String address)
    {
        try
        {
            URL url = new URL(address);
            return Optional.of(url);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static class Commit
    {
        private String sha = "";
        private String url = "";
    }

    public static Optional<GithubTag> getLatest()
    {
        Optional<GithubTag[]> tags = GithubRequest.get(Installer.properties().getTagsQuery(), GithubTag[].class);
        if (tags.isPresent() && tags.get().length > 0)
        {
            return Optional.of(tags.get()[0]);
        }
        return Optional.empty();
    }
}
