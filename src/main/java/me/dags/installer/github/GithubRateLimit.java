package me.dags.installer.github;

import java.util.Date;
import java.util.Optional;

import me.dags.installer.Installer;

public class GithubRateLimit
{
    private Resources resources = new Resources();
    private Data rate = new Data();

    public static class Resources
    {
        private Data core = new Data();
        private Data search = new Data();
    }

    public static class Data
    {
        private String limit = "";
        private String remaining = "";
        private String reset = "";

        @Override
        public String toString()
        {
            return "remaining=" + remaining + ",limit=" + limit + ",reset=" + new Date(Long.valueOf(reset) * 1000);
        }
    }

    public static void printLimit()
    {
        Installer.log("Checking API rate limit...");

        Optional<GithubRateLimit> limit = getLimit();
        if (limit.isPresent())
        {
            Installer.log(limit.get().rate.toString());
        }
    }

    public static Optional<GithubRateLimit> getLimit()
    {
        return GithubRequest.get(Installer.profile().getRateLimitQuery(), GithubRateLimit.class);
    }
}
