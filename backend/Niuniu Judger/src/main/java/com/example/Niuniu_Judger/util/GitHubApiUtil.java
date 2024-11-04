package com.example.Niuniu_Judger.util;

import com.example.Niuniu_Judger.model.Contributor;
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
import java.util.List;

@Component
public class GitHubApiUtil {

    @Value("${github.api.token}")
    private String gitHubApiToken;

    private final String baseUrl = "https://api.github.com";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public GitHubApiUtil() {
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

                // 获取下一页链接（如果有分页）
                String linkHeader = response.header("Link");
                url = getNextPageUrl(linkHeader);
            }
        }
        return followersList;
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

                // 获取下一页链接（如果有分页）
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
                    project.setStars(repoNode.path("stargazers_count").asInt());
                    project.setForks(repoNode.path("forks_count").asInt());
                    project.setWatchers(repoNode.path("watchers_count").asInt());
                    projects.add(project);
                }
            }
        }
        return projects;
    }
    /**
     * 获取开发者在项目中的贡献详情
     */
    /*
    public Contributor getContributorDetails(String fullName, String username) throws IOException {
        String url = baseUrl + "/repos/" + fullName + "/contributors";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 处理API请求限制等情况
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode contributorsArray = objectMapper.readTree(responseBody);

            if (contributorsArray.isArray()) {
                for (JsonNode contributorNode : contributorsArray) {
                    if (contributorNode.path("login").asText().equals(username)) {
                        Contributor contributor = new Contributor();
                        contributor.setLogin(username);
                        contributor.setContributions(contributorNode.path("contributions").asInt());
                        return contributor;
                    }
                }
            }
        }
        return null;
    }
*/
    /**
     * 获取开发者在项目中的代码增删行数
     */
    public int getLinesAdded(String username, String fullName) throws IOException {
        // 实现获取代码增删行数的方法
        // 由于GitHub API不提供直接获取某个作者的代码增删行数，需要遍历提交记录

        int linesAdded = 0;

        String url = baseUrl + "/repos/" + fullName + "/commits?author=" + username;
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
                JsonNode commitsArray = objectMapper.readTree(responseBody);

                if (commitsArray.isArray()) {
                    for (JsonNode commitNode : commitsArray) {
                        String commitSha = commitNode.path("sha").asText();
                        linesAdded += getCommitLinesAdded(fullName, commitSha);
                    }
                }

                // 获取下一页链接（如果有分页）
                String linkHeader = response.header("Link");
                url = getNextPageUrl(linkHeader);
            }
        }

        return linesAdded;
    }

    private int getCommitLinesAdded(String fullName, String commitSha) throws IOException {
        String url = baseUrl + "/repos/" + fullName + "/commits/" + commitSha;
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        int additions = 0;

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode commitNode = objectMapper.readTree(responseBody);

            additions = commitNode.path("stats").path("additions").asInt();
        }
        return additions;
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
                url = url.substring(1, url.length() - 1); // 去掉尖括号
            }

            String rel = parts[1].trim();
            if ("rel=\"next\"".equals(rel)) {
                return url;
            }
        }

        // 明确返回 null 表示没有下一页
        System.out.println("No next page found in link header");
        return null;
    }




    public Developer getDeveloperDetails(String username) throws IOException {
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

            // 使用设置默认值的方式，避免不必要的空检查
            developer.setUsername(developerNode.path("login").asText(""));
            developer.setGithubId(String.valueOf(developerNode.path("id").asLong()));
            developer.setAvatarUrl(developerNode.path("avatar_url").asText(""));
            developer.setBio(developerNode.path("bio").asText(""));
            developer.setCompany(developerNode.path("company").asText(""));
            developer.setLocation(developerNode.path("location").asText(""));
            developer.setEmail(developerNode.path("email").asText(""));
            developer.setPublicRepos(developerNode.path("public_repos").asInt(0));
            developer.setName(developerNode.path("name").asText(""));
            developer.setProfileUrl(developerNode.path("html_url").asText(""));
            developer.setBlog(developerNode.path("blog").asText(""));
            developer.setTalentRank(developerNode.path("talent_rank").asDouble(0.0));
            developer.setNation(developerNode.path("nation").asText(""));
            developer.setNationConfidence(developerNode.path("nation_confidence").asDouble(0.0));
            developer.setDomain(developerNode.path("domain").asText(""));
            developer.setDomainConfidence(developerNode.path("domain_confidence").asDouble(0.0));
            developer.setFollowersCount(developerNode.path("followers").asInt(0));
            developer.setFollowingCount(developerNode.path("following").asInt(0));
            developer.setPageRankScore(developerNode.path("page_rank_score").asDouble(0.0));
            developer.setContributionValue(developerNode.path("contribution_value").asDouble(0.0));
            developer.setComment("");  // 视需求填入默认注释或从其他数据源获取
        }

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
    /**
     * 获取项目的贡献者用户名列表
     */
    /**
     * 获取项目的贡献者用户名列表
     */
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
    /**
     * 获取项目的代码总行数
     */
    public int getTotalLinesOfCode(String fullName) throws IOException {
        int totalLines = 0;

        String url = baseUrl + "/repos/" + fullName + "/stats/code_frequency";
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "token " + gitHubApiToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode statsArray = objectMapper.readTree(responseBody);

            if (statsArray.isArray()) {
                for (JsonNode weekStats : statsArray) {
                    int additions = weekStats.get(1).asInt(0);
                    int deletions = weekStats.get(2).asInt(0);
                    totalLines += additions - deletions;
                }
            }
        }
        return totalLines;
    }
}
