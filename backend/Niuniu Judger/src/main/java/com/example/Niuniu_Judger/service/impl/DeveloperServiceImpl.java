package com.example.Niuniu_Judger.service.impl;

import com.example.Niuniu_Judger.dto.DeveloperDTO;
import com.example.Niuniu_Judger.model.Developer;
import com.example.Niuniu_Judger.model.Project;
import com.example.Niuniu_Judger.service.DeveloperService;
import com.example.Niuniu_Judger.util.GitHubApiUtil;
import com.example.Niuniu_Judger.util.OpenAiUtil;
import org.jsoup.Jsoup;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.nodes.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.*;

@Service
public class DeveloperServiceImpl implements DeveloperService {
    private static final double MAX_Follower_LOG_VALUE =(5000 * 0.99) + (150000 * 0.01);

    @Autowired
    private GitHubApiUtil gitHubApiUtil;

    @Autowired
    private OpenAiUtil openAiUtil;


    /**
     * 根据名称搜索开发者
     */
    @Override
    public List<DeveloperDTO> searchDevelopersByName(String name) {
        try {
            List<Developer> developers = gitHubApiUtil.searchDevelopersByName(name);
            List<DeveloperDTO> dtoList = new ArrayList<>();
            for (Developer developer : developers) {
                DeveloperDTO dto = new DeveloperDTO();
                BeanUtils.copyProperties(developer, dto);
                dtoList.add(dto);
            }
            return dtoList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 获取开发者的评估结果
     */
    @Override
    public DeveloperDTO getDeveloperEvaluation(String username) {
        try {
            long startTime = System.currentTimeMillis();
            Developer developer = gitHubApiUtil.getDeveloperDetails(username);
            String nation = inferNation(developer);
            long endTime = System.currentTimeMillis(); // 获取结束时间
            System.out.println("程序运行时间L1: " + (endTime - startTime) + " 毫秒");
            startTime = System.currentTimeMillis();
            String domain = inferDomain(developer);
            endTime = System.currentTimeMillis(); // 获取结束时间
            System.out.println("程序运行时间L2: " + (endTime - startTime) + " 毫秒");
            startTime = System.currentTimeMillis();
            String comment = generateDeveloperEvaluation(developer);
            endTime = System.currentTimeMillis(); // 获取结束时间
            System.out.println("程序运行时间L3: " + (endTime - startTime) + " 毫秒");
            developer.setComment(comment);
            developer.setNation(nation);
            developer.setDomain(domain);
            double talentRank = calculateDeveloperTalentRank(developer);
            developer.setTalentRank(talentRank);
            DeveloperDTO dto = new DeveloperDTO();
            BeanUtils.copyProperties(developer, dto);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取开发者的评估结果v2
     */
    @Override
    public DeveloperDTO getDeveloperEvaluationv2(String username) {
        try {
            long startTime = System.currentTimeMillis();
            Developer developer = gitHubApiUtil.getDeveloperDetails(username);
            String nation = inferNation(developer);
            long endTime = System.currentTimeMillis(); // 获取结束时间
            System.out.println("程序运行时间L1: " + (endTime - startTime) + " 毫秒");
            startTime = System.currentTimeMillis();
            String domain = inferDomain(developer);
            endTime = System.currentTimeMillis(); // 获取结束时间
            System.out.println("程序运行时间L2: " + (endTime - startTime) + " 毫秒");
            developer.setNation(nation);
            developer.setDomain(domain);
            double talentRank = calculateDeveloperTalentRank(developer);
            developer.setTalentRank(talentRank);
            DeveloperDTO dto = new DeveloperDTO();
            BeanUtils.copyProperties(developer, dto);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 生成开发者的简要评价
     */

    @Override
    public String generateDeveloperEvaluation(Developer developer) throws IOException {
        String bio = developer.getBio();
        List<Project> projects = getProjectsByDeveloper(developer);

        String domain = "N/A";

        String developerInfo = bio != null ? bio : "";

        StringBuilder projectsInfo = new StringBuilder();
        for (Project project : projects) {
            projectsInfo.append("Project Name: ").append(project.getName())
                    .append("\nDescription: ").append(project.getDescription())
                    .append("\nLanguage: ").append(project.getLanguage())
                    .append("\n\n");
        }

        String combinedInfo = developerInfo + "\n\n" + projectsInfo.toString();

        if (!combinedInfo.isEmpty()) {
            domain = openAiUtil.PJDeveloperProfile(combinedInfo);
        }

        return domain;
    }
    /**
     * 获取开发者参与的所有项目
     */
    @Override
    public List<Project> getProjectsByDeveloper(Developer developer) throws IOException {
        List<Project> projects = gitHubApiUtil.getProjectsByDeveloper(developer.getUsername());
        for (Project project : projects) {
            double importance = calculateProjectImportance(project);
            project.setProjectImportance(importance);
        }
        return projects;
    }

    /**
     * 计算开发者在项目中的贡献度
     */

    public double calculateContributionScore(Developer developer, Project project) {
        ExecutorService executor = Executors.newFixedThreadPool(3);  // 创建一个包含3个线程的线程池
        try {
            // 创建任务并提交给线程池
            Future<Integer> totalCommitsFuture = executor.submit(() -> gitHubApiUtil.getTotalCommits(project.getFullName()));
            Future<Integer> developerCommitsFuture = executor.submit(() -> gitHubApiUtil.getDeveloperCommits(developer.getUsername(), project.getFullName()));

            Future<Integer> totalMergedPRsFuture = executor.submit(() -> gitHubApiUtil.getTotalMergedPullRequests(project.getFullName()));
            Future<Integer> developerMergedPRsFuture = executor.submit(() -> gitHubApiUtil.getDeveloperMergedPullRequests(developer.getUsername(), project.getFullName()));

            Future<Integer> totalResolvedIssuesFuture = executor.submit(() -> gitHubApiUtil.getTotalResolvedIssues(project.getFullName()));
            Future<Integer> developerResolvedIssuesFuture = executor.submit(() -> gitHubApiUtil.getDeveloperResolvedIssues(developer.getUsername(), project.getFullName()));

            // 获取结果
            int totalCommits = totalCommitsFuture.get();
            int developerCommits = developerCommitsFuture.get();

            int totalMergedPRs = totalMergedPRsFuture.get();
            int developerMergedPRs = developerMergedPRsFuture.get();

            int totalResolvedIssues = totalResolvedIssuesFuture.get();
            int developerResolvedIssues = developerResolvedIssuesFuture.get();

            // 检查数据
            if (totalCommits == 0 || totalMergedPRs == 0 || totalResolvedIssues == 0) {
                return 0.0;
            }

            // 权重设置
            double commitWeight = 0.4;
            double prWeight = 0.3;
            double issueWeight = 0.3;

            // 计算各项得分
            double commitScore = (double) developerCommits / totalCommits * commitWeight;
            double prScore = (double) developerMergedPRs / totalMergedPRs * prWeight;
            double issueScore = (double) developerResolvedIssues / totalResolvedIssues * issueWeight;

            // 计算贡献得分并返回
            double contributionScore = (commitScore + prScore + issueScore) * 100;
            return Math.min(contributionScore, 100.0) / 100;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            executor.shutdown();  // 关闭线程池
        }
    }


    /**
     * 计算项目的重要程度
     */
    @Override
    public double calculateProjectImportance(Project project) {
        int stars = project.getStars();
        int forks = project.getForks();
        int watchers = project.getWatchers();

        int maxStars = 1000;
        int maxForks = 500;
        int maxWatchers = 50000;

        double starsScore = Math.min((double) stars / maxStars, 1.0) * 100;
        double forksScore = Math.min((double) forks / maxForks, 1.0) * 100;
        double watchersScore = Math.min((double) watchers / maxWatchers, 1.0) * 100;

        return starsScore * 0.5 + forksScore * 0.3 + watchersScore * 0.2;
    }

    /**
     * 计算开发者的平均加权贡献度
     */
    @Override
    public double calculateAverageWeightedContribution(Developer developer) throws IOException {
        List<Project> projects = getProjectsByDeveloper(developer);
        if (projects.isEmpty()) {
            return 0.0;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Callable<Double>> tasks = new ArrayList<>();
        for (Project project : projects) {
            tasks.add(() -> {
                double projectImportance = calculateProjectImportance(project);
                double contributionScore = calculateContributionScore(developer, project);
                return projectImportance * contributionScore;
            });
        }

        double totalWeightedContribution = 0.0;
        try {
            List<Future<Double>> results = executorService.invokeAll(tasks);

            for (Future<Double> result : results) {
                totalWeightedContribution += result.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }

        return totalWeightedContribution / projects.size();
    }


    /**
     * 推断开发者的国家/地区
     */
    @Override
    public String inferNation(Developer developer) {
        String location = developer.getLocation();
        String bio = developer.getBio();
        String email = developer.getEmail();

        String nation = "N/A";

        if ((location != null && !location.isEmpty()) || (bio != null && !bio.isEmpty()) || (email != null && !email.isEmpty())) {
            nation = openAiUtil.inferNation(bio, location, email);
        }
/*
        if (nation.equals("N/A")) {
            nation = inferNationFromFollowings(developer.getUsername());
        }
*/
        return nation;
    }

    /**
     * 从关注的用户中推断国家/地区
     */
    private String inferNationFromFollowings(String username) {
        try {
            List<String> followings = gitHubApiUtil.getDeveloperFollowing(username);
            Map<String, Integer> nationCount = new HashMap<>();

            for (String followingUsername : followings) {
                Developer followingDeveloper = gitHubApiUtil.getDeveloperDetails(followingUsername);
                String followingNation = followingDeveloper.getLocation();

                if (followingNation != null && !followingNation.isEmpty()) {
                    nationCount.put(followingNation, nationCount.getOrDefault(followingNation, 0) + 1);
                }
            }

            return nationCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }


    /**
     * 推断开发者的领域
     */
    @Override
    public String inferDomain(Developer developer) throws IOException {
        String bio = developer.getBio();
        List<Project> projects = getProjectsByDeveloper(developer);

        String domain = "N/A";

        String developerInfo = bio != null ? bio : "";

        StringBuilder projectsInfo = new StringBuilder();
        for (Project project : projects) {
            projectsInfo.append("Project Name: ").append(project.getName())
                    .append("\nDescription: ").append(project.getDescription())
                    .append("\nLanguage: ").append(project.getLanguage())
                    .append("\n\n");
        }

        String combinedInfo = developerInfo + "\n\n" + projectsInfo.toString();

        if (!combinedInfo.isEmpty()) {
            domain = openAiUtil.analyzeDeveloperProfile(combinedInfo);
        }

        return domain;
    }

    /**
     * 计算开发者的 Follower 分数
     */
    /*
    @Override
    public double calculateDeveloperFollower(Developer developer) throws IOException {

        ExecutorService executorService = Executors.newFixedThreadPool(50);
        List<Future<Integer>> futures = new ArrayList<>();
        int userFollowersCount = developer.getFollowersCount();
        List<String> followerUsernames = gitHubApiUtil.getDeveloperFollowers(developer.getUsername());
        for (String username : followerUsernames) {
            Future<Integer> future = executorService.submit(() ->
                    gitHubApiUtil.getDeveloperFollowersCount(username)
            );
            futures.add(future);
        }

        int totalFollowerCount = 0;
        for (Future<Integer> future : futures) {
            try {
                totalFollowerCount += future.get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error retrieving follower count for username: " + e.getMessage());
            }
        }
        executorService.shutdown();
        double Follower = (userFollowersCount * 0.99) + (totalFollowerCount * 0.01);
        Follower = Math.log(Follower + 1);
        return (Follower / MAX_Follower_LOG_VALUE) * 100;
    }

     */
    /**
     * 计算开发者的 TalentRank，结合 Follower 和贡献度
     */

    @Override
    public double calculateDeveloperTalentRank(Developer developer) throws IOException {
        long startTime = System.currentTimeMillis();
        double FollowerScore = developer.getFollowersCount();
        long endTime = System.currentTimeMillis(); // 获取结束时间
        System.out.println("程序运行时间L4: " + (endTime - startTime) + " 毫秒");
        startTime = System.currentTimeMillis();
        double contributionScore = calculateAverageWeightedContribution(developer);
        endTime = System.currentTimeMillis(); // 获取结束时间
        System.out.println("程序运行时间L5: " + (endTime - startTime) + " 毫秒");
        return ( (FollowerScore / MAX_Follower_LOG_VALUE) * 100 * 0.5) + (contributionScore * 0.5);
    }

    /**
     * 从 URL 获取文本内容
     */
    public String fetchContentFromUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return "";
        }
        try {
            Document document = Jsoup.connect(url).get();
            String textContent = document.body().text();
            return textContent;
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public List<DeveloperDTO> searchDevelopersByKeyword(String keyword) {
        try {
            List<Developer> developers = gitHubApiUtil.searchDevelopersByKeywordAndSynonyms(keyword);
            List<DeveloperDTO> dtoList = new ArrayList<>();
            developers.parallelStream().forEach(developer -> {
                DeveloperDTO dto = getDeveloperEvaluationv2(developer.getUsername());
                dtoList.add(dto);
            });


            dtoList.sort((d1, d2) -> Double.compare(d2.getTalentRank(), d1.getTalentRank()));

            return dtoList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
