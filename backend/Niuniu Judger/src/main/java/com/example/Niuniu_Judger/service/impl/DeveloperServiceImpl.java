package com.example.Niuniu_Judger.service.impl;

import com.example.Niuniu_Judger.dto.DeveloperDTO;
import com.example.Niuniu_Judger.mapper.DeveloperMapper;
import com.example.Niuniu_Judger.mapper.ProjectMapper;
import com.example.Niuniu_Judger.model.Contributor;
import com.example.Niuniu_Judger.model.Developer;
import com.example.Niuniu_Judger.model.Project;
import com.example.Niuniu_Judger.service.DeveloperService;
import com.example.Niuniu_Judger.util.GitHubApiUtil;
import com.example.Niuniu_Judger.util.OpenAiUtil;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class DeveloperServiceImpl implements DeveloperService {
    private static final double MAX_PAGERANK_LOG_VALUE = Math.log((1_000_000 * 0.9) + (1_000_000 * 100 * 0.1) + 1);

    @Autowired
    private DeveloperMapper developerMapper;

    @Autowired
    private GitHubApiUtil gitHubApiUtil;

    @Autowired
    private OpenAiUtil openAiUtil;

    @Autowired
    private ProjectMapper projectMapper;

    // --- Developer Retrieval ---

    /**
     * 根据 GitHub ID 获取开发者信息
     */
    @Override
    public DeveloperDTO getDeveloperByGithubId(Long githubId) {
        Developer developer = developerMapper.selectDeveloperByGitHubId(String.valueOf(githubId));
        if (developer != null) {
            DeveloperDTO dto = new DeveloperDTO();
            BeanUtils.copyProperties(developer, dto);
            return dto;
        }
        return null;
    }

    /**
     * 根据用户名获取开发者信息
     */
    @Override
    public DeveloperDTO getDeveloperByUsername(String name) {
        Developer developer = developerMapper.selectDeveloperByUsername(name);
        if (developer != null) {
            DeveloperDTO dto = new DeveloperDTO();
            BeanUtils.copyProperties(developer, dto);
            return dto;
        }
        return null;
    }

    // --- Developer Search and Filtering ---

    /**
     * 根据领域和国家/地区搜索开发者
     */
    @Override
    public List<DeveloperDTO> searchDevelopers(String domain, String nation) {
        List<Developer> developers = developerMapper.selectDevelopersByCriteria(domain, nation);
        List<DeveloperDTO> dtoList = new ArrayList<>();
        for (Developer developer : developers) {
            DeveloperDTO dto = new DeveloperDTO();
            BeanUtils.copyProperties(developer, dto);
            dtoList.add(dto);
        }
        return dtoList;
    }

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

    // --- Developer Evaluation and Ranking ---

    /**
     * 获取开发者的评估结果
     */
    @Override
    public DeveloperDTO getDeveloperEvaluation(String username) {
        try {
            Developer developer = developerMapper.selectDeveloperByUsername(username);
            if (developer == null) {
                developer = gitHubApiUtil.getDeveloperDetails(username);
                String nation = inferNation(developer);
                double nationConfidence = calculateNationConfidence(developer);
                String domain = inferDomain(developer);
                double domainConfidence = calculateDomainConfidence(developer);
                String comment = generateDeveloperEvaluation(developer);

                developer.setComment(comment);
                developer.setNation(nation);
                developer.setNationConfidence(nationConfidence);
                developer.setDomain(domain);
                developer.setDomainConfidence(domainConfidence);

                developerMapper.insertDeveloper(developer);
            }

            double talentRank = calculateDeveloperTalentRank(developer);
            developer.setTalentRank(talentRank);
            developerMapper.updateDeveloperTalentRank(developer.getUsername(), talentRank);

            DeveloperDTO dto = new DeveloperDTO();
            BeanUtils.copyProperties(developer, dto);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成开发者的评估报告
     */
    @Override
    public String generateDeveloperEvaluation(Developer developer) {
        String bio = developer.getBio();
        String blogContent = fetchContentFromUrl(developer.getBlog());
        String websiteContent = fetchContentFromUrl(developer.getHtmlUrl());

        StringBuilder content = new StringBuilder();
        if (bio != null && !bio.isEmpty()) {
            content.append(bio).append("\n");
        }
        if (blogContent != null && !blogContent.isEmpty()) {
            content.append(blogContent).append("\n");
        }
        if (websiteContent != null && !websiteContent.isEmpty()) {
            content.append(websiteContent).append("\n");
        }

        if (content.length() > 0) {
            return openAiUtil.PJDeveloperProfile(content.toString());
        }
        return "N/A";
    }

    // --- Contribution and Project Metrics ---

    /**
     * 获取开发者参与的所有项目
     */
    @Override
    public List<Project> getProjectsByDeveloper(Developer developer) {
        try {
            return gitHubApiUtil.getProjectsByDeveloper(developer.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 计算开发者在项目中的贡献度
     */
    @Override
    public double calculateContributionScore(Developer developer, Project project) {
        try {
 /*
            Contributor contributor = gitHubApiUtil.getContributorDetails(project.getFullName(), developer.getUsername());

            if (contributor == null) {
                return 0.0;
            }
*/
            int totalLines = gitHubApiUtil.getTotalLinesOfCode(project.getFullName());
            int developerLines = gitHubApiUtil.getLinesAdded(developer.getUsername(), project.getFullName());

            double codeContribution = (double) developerLines / totalLines * 100;
            return Math.min(codeContribution, 100.0);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
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

        int maxStars = 100000;
        int maxForks = 50000;
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
    public double calculateAverageWeightedContribution(Developer developer) {
        List<Project> projects = getProjectsByDeveloper(developer);
        if (projects.isEmpty()) {
            return 0.0;
        }

        double totalWeightedContribution = 0.0;

        for (Project project : projects) {
            double projectImportance = calculateProjectImportance(project);
            double contributionScore = calculateContributionScore(developer, project);
            double weightedContribution = projectImportance * (contributionScore / 100.0);

            totalWeightedContribution += weightedContribution;
        }

        return totalWeightedContribution / projects.size();
    }

    // --- Inference and Confidence ---

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

        if (nation.equals("N/A")) {
            nation = inferNationFromFollowings(developer.getUsername());
        }

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
     * 计算国家/地区置信度
     */
    @Override
    public double calculateNationConfidence(Developer developer) {
        return developer.getNation() != null && !developer.getNation().equals("N/A") ? 1.0 : 0.0;
    }

    /**
     * 推断开发者的领域
     */
    @Override
    public String inferDomain(Developer developer) {
        String bio = developer.getBio();
        if (bio != null && !bio.isEmpty()) {
            return openAiUtil.analyzeDeveloperProfile(bio);
        }
        return "N/A";
    }

    /**
     * 计算领域置信度
     */
    @Override
    public double calculateDomainConfidence(Developer developer) {
        return developer.getDomain() != null && !developer.getDomain().equals("N/A") ? 1.0 : 0.0;
    }

    // --- Data Synchronization ---

    /**
     * 同步 GitHub 数据，更新开发者信息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Override
    public void syncDeveloperData() {
        try {
            List<Project> hotProjects = gitHubApiUtil.getHotProjects();

            for (Project project : hotProjects) {
                Project existingProject = projectMapper.selectProjectByGitHubId(project.getGithubId());
                if (existingProject == null) {
                    projectMapper.insertProject(project);
                } else {
                    project.setId(existingProject.getId());
                    projectMapper.updateProject(project);
                }

                List<String> contributorsUsernames = gitHubApiUtil.getProjectContributorsUsernames(project.getFullName());

                for (String username : contributorsUsernames) {
                    Developer developer = developerMapper.selectDeveloperByUsername(username);
                    if (developer == null) {
                        developer = gitHubApiUtil.getDeveloperDetails(username);
                        developer.setNation(inferNation(developer));
                        developer.setNationConfidence(calculateNationConfidence(developer));
                        developer.setDomain(inferDomain(developer));
                        developer.setDomainConfidence(calculateDomainConfidence(developer));

                        developerMapper.insertDeveloper(developer);
                    } else {
                        Developer updatedDeveloper = gitHubApiUtil.getDeveloperDetails(username);
                        developer.setFollowersCount(updatedDeveloper.getFollowersCount());
                        developer.setFollowingCount(updatedDeveloper.getFollowingCount());
                        developerMapper.updateDeveloper(developer);
                    }

                    developer.setPageRankScore(calculateDeveloperPageRank(developer));
                    developer.setContributionValue(calculateAverageWeightedContribution(developer));
                    developerMapper.updateDeveloperScores(developer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算开发者的 PageRank 分数
     */
    @Override
    public double calculateDeveloperPageRank(Developer developer) {
        try {
            int userFollowersCount = developer.getFollowersCount();
            List<String> followerUsernames = gitHubApiUtil.getDeveloperFollowers(developer.getUsername());

            int totalFollowersFollowersCount = followerUsernames.stream()
                    .mapToInt(followerUsername -> {
                        try {
                            return gitHubApiUtil.getDeveloperDetails(followerUsername).getFollowersCount();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sum();

            double pageRank = (userFollowersCount * 0.9) + (totalFollowersFollowersCount * 0.1);
            pageRank = Math.log(pageRank + 1);
            return (pageRank / MAX_PAGERANK_LOG_VALUE) * 100;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * 计算开发者的 TalentRank，结合 PageRank 和贡献度
     */
    @Override
    public double calculateDeveloperTalentRank(Developer developer) {
        double pageRankScore = developer.getPageRankScore();
        double contributionScore = calculateAverageWeightedContribution(developer);

        return (pageRankScore * 0.5) + (contributionScore * 0.5);
    }

    /**
     * 从 URL 获取文本内容
     */
    private String fetchContentFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        try {
            return Jsoup.connect(url).get().text();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
