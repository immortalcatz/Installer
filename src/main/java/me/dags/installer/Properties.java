package me.dags.installer;

import java.io.File;

public class Properties
{
    public String profile_name = "";
    public String target_dir = "";
    public String minecraft_version = "";
    public GithubProperties github = new GithubProperties();

    public transient File mcDir = new File("");

    public String getRateLimitQuery()
    {
        return trim(github.api, "/") + "/" + "rate_limit";
    }

    public String getTagsQuery()
    {
        return trim(github.api, "/") + "/repos/" + trim(github.repository, "/") + "/tags";
    }

    @Override
    public String toString()
    {
        return "profile='" + profile_name + "',target_dir='" + target_dir + "',github={" + github.toString() + "}";
    }

    private static String trim(String in, String s)
    {
        in = in.startsWith(s) ? in.substring(1) : in;
        in = in.endsWith(s) ? in.substring(0, in.length() - 1) : in;
        return in;
    }

    public static class GithubProperties
    {
        public String api = "https://api.github.com/";
        public String repository = "dags-/ArdaCraftBlocks";

        @Override
        public String toString()
        {
            return "api='" + api + "',repository='" + repository + "'";
        }
    }
}
