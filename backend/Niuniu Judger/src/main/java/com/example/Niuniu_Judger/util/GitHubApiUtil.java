package com.example.Niuniu_Judger.util;

import com.example.Niuniu_Judger.model.Developer;
import com.example.Niuniu_Judger.model.Project;
import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.*;

@Component
public class GitHubApiUtil {

    @Value("${github.api.token}")
    private String gitHubApiToken;

    private final String baseUrl = "https://api.github.com";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final OpenAiUtil openAiUtil;

    public GitHubApiUtil(OpenAiUtil openAiUtil) {
        this.openAiUtil = openAiUtil;
        this.client = new OkHttpClient.Builder()
                .callTimeout(Duration.ofSeconds(60))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> getDeveloperFollowers(String username) throws IOException {
        List<String> followersList = new ArrayList<>();
        String url = baseUrl + "/users/" + username + "/followers";

        while (url != null) {
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "token " + gitHubApiToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
                JsonNode usersArray = objectMapper.readTree(responseBody);

                if (usersArray.isArray()) {
                    for (JsonNode userNode : usersArray) {
                        String login = userNode.path("login").asText();
                        followersList.add(login);
                    }
                }

                String linkHeader = response.header("Link");
                url = getNextPageUrl(linkHeader);
            }
        }
        return followersList;
    }

    public int getDeveloperFollowersCount(String username) throws IOException {
        String url = baseUrl + "/users/" + username;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode developerNode = objectMapper.readTree(responseBody);
            return developerNode.path("followers").asInt(0);
        }
    }

    public List<String> getDeveloperFollowing(String username) throws IOException {
        List<String> followingList = new ArrayList<>();
        String url = baseUrl + "/users/" + username + "/following";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        while (url != null) {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
                JsonNode usersArray = objectMapper.readTree(responseBody);

                if (usersArray.isArray()) {
                    for (JsonNode userNode : usersArray) {
                        String login = userNode.path("login").asText();
                        followingList.add(login);
                    }
                }

                String linkHeader = response.header("Link");
                url = getNextPageUrl(linkHeader);
            }
        }
        return followingList;
    }

    /**
     * 获取开发者参与的所有项目
     */
    public List<Project> getProjectsByDeveloper(String username) throws IOException {
        List<Project> projects = new ArrayList<>();
        String url = baseUrl + "/users/" + username + "/repos?type=owner&sort=updated";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode reposArray = objectMapper.readTree(responseBody);

            if (reposArray.isArray()) {
                for (JsonNode repoNode : reposArray) {
                    Project project = new Project();
                    project.setGithubId(String.valueOf(repoNode.path("id").asLong()));
                    project.setName(repoNode.path("name").asText());
                    project.setFullName(repoNode.path("full_name").asText());
                    project.setDescription(repoNode.path("description").asText(""));
                    project.setLanguage(repoNode.path("language").asText(""));  // 获取项目语言
                    project.setStars(repoNode.path("stargazers_count").asInt());
                    project.setForks(repoNode.path("forks_count").asInt());
                    project.setWatchers(repoNode.path("watchers_count").asInt());
                    project.setHtmlUrl(repoNode.path("html_url").asText());
                    projects.add(project);
                }
            }
        }
        return projects;
    }

    /**
     * 获取下一页的URL
     */
    private String getNextPageUrl(String linkHeader) {
        if (linkHeader == null) {
            System.out.println("Link header is null");
            return null;
        }

        String[] links = linkHeader.split(",");
        for (String link : links) {
            String[] parts = link.split(";");
            if (parts.length < 2) {
                System.out.println("Skipping invalid link format: " + link);
                continue;
            }
            String url = parts[0].trim();
            if (url.startsWith("<") && url.endsWith(">")) {
                url = url.substring(1, url.length() - 1);
            }
            String rel = parts[1].trim();
            if ("rel=\"next\"".equals(rel)) {
                return url;
            }
        }

        System.out.println("No next page found in link header");
        return null;
    }

    public Developer getDeveloperDetails(String username) throws IOException {
        long startTime = System.currentTimeMillis(); // 获取开始时间
        String url = baseUrl + "/users/" + username;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        Developer developer = new Developer();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JsonNode developerNode = objectMapper.readTree(responseBody);

            developer.setUsername(developerNode.path("login").asText(""));
            developer.setGithubId(String.valueOf(developerNode.path("id").asLong()));
            developer.setAvatarUrl(developerNode.path("avatar_url").asText(""));
            developer.setBio(developerNode.path("bio").asText(""));
            developer.setLocation(developerNode.path("location").asText(""));
            developer.setEmail(developerNode.path("email").asText(""));
            developer.setPublicRepos(developerNode.path("public_repos").asInt(0));
            developer.setName(developerNode.path("name").asText(""));
            developer.setProfileUrl(developerNode.path("html_url").asText(""));
            developer.setBlog(developerNode.path("blog").asText(""));
            developer.setFollowersCount(developerNode.path("followers").asInt(0));
            developer.setFollowingCount(developerNode.path("following").asInt(0));
        }
        long endTime = System.currentTimeMillis(); // 获取结束时间
        long duration = endTime - startTime; // 计算运行时间（毫秒）
        System.out.println("程序运行时间: " + duration + " 毫秒");
        return developer;
    }

    public List<Developer> searchDevelopersByName(String name) throws IOException {
        List<Developer> developers = new ArrayList<>();
        String query = URLEncoder.encode(name, "UTF-8");
        String url = baseUrl + "/search/users?q=" + query + "+in:login+type:user";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode itemsNode = rootNode.path("items");

            if (itemsNode.isArray()) {
                for (JsonNode userNode : itemsNode) {
                    Developer developer = new Developer();
                    developer.setGithubId(String.valueOf(Long.valueOf(String.valueOf(userNode.path("id").asLong()))));
                    developer.setUsername(userNode.path("login").asText());
                    developer.setAvatarUrl(userNode.path("avatar_url").asText());
                    developers.add(developer);
                }
            }
        }
        return developers;
    }

/*
    public List<String> getProjectContributorsUsernames(String fullName) throws IOException {
        List<String> contributors = new ArrayList<>();
        String url = baseUrl + "/repos/" + fullName + "/contributors";

        while (url != null) {
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "token " + gitHubApiToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
                JsonNode contributorsArray = objectMapper.readTree(responseBody);
                int a = 0;
                if (contributorsArray.isArray()) {
                    for (JsonNode contributorNode : contributorsArray) {
                        String login = contributorNode.path("login").asText();
                        contributors.add(login);
                        System.out.println(a++);
                        System.out.println(login);
                    }
                }
                // 获取下一页链接
                String linkHeader = response.header("Link");
                url = getNextPageUrl(linkHeader);
            }
        }
        System.out.println("finished");
        return contributors;
    }

 */
/*
    public List<Project> getHotProjects() throws IOException {
        List<Project> projects = new ArrayList<>();
        String url = baseUrl + "/search/repositories?q=stars:>1000&sort=stars&order=desc&per_page=100";

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode itemsNode = rootNode.path("items");

            if (itemsNode.isArray()) {
                for (JsonNode repoNode : itemsNode) {
                    Project project = new Project();
                    project.setGithubId(String.valueOf(repoNode.path("id").asLong()));
                    project.setName(repoNode.path("name").asText());
                    project.setFullName(repoNode.path("full_name").asText());
                    project.setDescription(repoNode.path("description").asText(""));
                    project.setStars(repoNode.path("stargazers_count").asInt(0));
                    project.setForks(repoNode.path("forks_count").asInt(0));
                    project.setWatchers(repoNode.path("watchers_count").asInt(0));
                    projects.add(project);
                }
            }
        }
        return projects;
    }

 */
    public int getTotalCommits(String repoFullName) throws IOException {
        String url = baseUrl + "/repos/" + repoFullName + "/stats/contributors";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonNode statsArray = objectMapper.readTree(response.body().string());
            int totalCommits = 0;

            for (JsonNode contributor : statsArray) {
                totalCommits += contributor.get("total").asInt(0);
            }
            return totalCommits;
        }
    }

    public int getDeveloperCommits(String developer, String repoFullName) throws IOException {
        String url = baseUrl + "/repos/" + repoFullName + "/stats/contributors";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonNode statsArray = objectMapper.readTree(response.body().string());

            for (JsonNode contributor : statsArray) {
                if (contributor.get("author").get("login").asText().equals(developer)) {
                    return contributor.get("total").asInt(0);
                }
            }
            return 0;
        }
    }

    public int getTotalMergedPullRequests(String repoFullName) throws IOException {
        String url = baseUrl + "/repos/" + repoFullName + "/pulls?state=closed";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        int mergedPRs = 0;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonNode pullsArray = objectMapper.readTree(response.body().string());

            for (JsonNode pull : pullsArray) {
                if (pull.get("merged_at") != null) {
                    mergedPRs++;
                }
            }
            return mergedPRs;
        }
    }

    public int getDeveloperMergedPullRequests(String developer, String repoFullName) throws IOException {
        String url = baseUrl + "/repos/" + repoFullName + "/pulls?state=closed";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        int developerMergedPRs = 0;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonNode pullsArray = objectMapper.readTree(response.body().string());

            for (JsonNode pull : pullsArray) {
                if (pull.get("user").get("login").asText().equals(developer) && pull.get("merged_at") != null) {
                    developerMergedPRs++;
                }
            }
            return developerMergedPRs;
        }
    }

    public int getTotalResolvedIssues(String repoFullName) throws IOException {
        String url = baseUrl + "/repos/" + repoFullName + "/issues?state=closed";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        int resolvedIssues = 0;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonNode issuesArray = objectMapper.readTree(response.body().string());
            resolvedIssues = issuesArray.size();  // 统计关闭状态的issues数量
            return resolvedIssues;
        }
    }

    public int getDeveloperResolvedIssues(String developer, String repoFullName) throws IOException {
        String url = baseUrl + "/repos/" + repoFullName + "/issues?state=closed";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        int developerResolvedIssues = 0;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JsonNode issuesArray = objectMapper.readTree(response.body().string());

            for (JsonNode issue : issuesArray) {
                if (issue.get("user").get("login").asText().equals(developer)) {
                    developerResolvedIssues++;
                }
            }
            return developerResolvedIssues;
        }
    }

    public List<Developer> searchDevelopersByKeywordAndSynonyms(String keyword) throws IOException {
        List<Developer> developers = new ArrayList<>();
        Set<String> usernamesSet = new HashSet<>(); // 用于去重

        List<String> keywordsAndSynonyms = generateKeywordAndSynonyms(keyword);

        for (String kw : keywordsAndSynonyms) {
            if (kw == null || kw.isEmpty()) continue;

            String query = URLEncoder.encode(kw + " in:bio", "UTF-8");
            String url = baseUrl + "/search/users?q=" + query + "&sort=followers&order=desc&per_page=2";

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "token " + gitHubApiToken)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode itemsNode = rootNode.path("items");

                if (itemsNode.isArray()) {
                    for (JsonNode userNode : itemsNode) {
                        String username = userNode.path("login").asText();
                        if (!usernamesSet.contains(username)) {
                            usernamesSet.add(username);
                            Developer developer = new Developer();
                            developer.setGithubId(String.valueOf(userNode.path("id").asLong()));
                            developer.setUsername(username);
                            developer.setAvatarUrl(userNode.path("avatar_url").asText());
                            developer.setFollowersCount(getDeveloperFollowersCount(username));
                            developer.setProfileUrl(userNode.path("html_url").asText(""));
                            developers.add(developer);
                        }
                    }
                }
            }
        }
        return developers;
    }

    private List<String> generateKeywordAndSynonyms(String keyword) {
        List<String> keywordsAndSynonyms = new ArrayList<>();

        keywordsAndSynonyms.add(keyword);

        String prompt = String.format("请为关键词 '%s' 提供3个近义词，你只能返回英文词汇，用逗号分隔每个词汇，除了词汇以外不提供其他内容。", keyword);
        String synonyms = openAiUtil.getGPTResponse(prompt);

        if (synonyms != null && !synonyms.isEmpty()) {
            String[] synonymsArray = synonyms.split(",");
            for (String synonym : synonymsArray) {
                keywordsAndSynonyms.add(synonym.trim());
            }
        }

        return keywordsAndSynonyms;
    }
}