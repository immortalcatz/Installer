package me.dags.installer.github;

import java.util.Date;
import java.util.Optional;

import me.dags.installer.Installer;

public class GithubRateLimit
{
    public Resources resources = new Resources();
    public Data rate = new Data();

    public static class Resources
    {
        public Data core = new Data();
        public Data search = new Data();
    }

    public static class Data
    {
        public String limit = "";
        public String remaining = "";
        public String reset = "";

        public String resetDate()
        {
            return new Date(Long.valueOf(reset) * 1000).toString();
        }

        @Override
        public String toString()
        {
            return "remaining=" + remaining + ",limit=" + limit + ",reset=" + resetDate();
        }
    }

    public static Optional<GithubRateLimit> getLimit()
    {
        return GithubRequest.get(Installer.properties().getRateLimitQuery(), GithubRateLimit.class);
    }
}
