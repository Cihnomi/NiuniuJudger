package com.example.Niuniu_Judger.controller;

import com.example.Niuniu_Judger.dto.DeveloperDTO;
import com.example.Niuniu_Judger.model.Developer;
import com.example.Niuniu_Judger.model.Project;
import com.example.Niuniu_Judger.service.DeveloperService;
import com.example.Niuniu_Judger.util.GitHubApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    private final DeveloperService developerService;
    private final GitHubApiUtil gitHubApiUtil;

    @Autowired
    public DeveloperController(DeveloperService developerService, GitHubApiUtil gitHubApiUtil) {
        this.developerService = developerService;
        this.gitHubApiUtil = gitHubApiUtil;
    }

    // --- Developer Retrieval ---

    /**
     * 根据 GitHub ID 获取开发者信息
     * @param githubId 开发者的 GitHub ID
     * @return DeveloperDTO 开发者的数据传输对象
     */
    @GetMapping("/{githubId}")
    public DeveloperDTO getDeveloperByGithubId(@PathVariable Long githubId) {
        return developerService.getDeveloperByGithubId(githubId);
    }

    /**
     * 根据用户名获取开发者信息
     * @param name 开发者的用户名
     * @return DeveloperDTO 开发者的数据传输对象
     */
    @GetMapping("/username/{name}")
    public DeveloperDTO getDeveloperByUsername(@PathVariable String name) {
        return developerService.getDeveloperByUsername(name);
    }

    // --- Developer Search and Filtering ---

    /**
     * 根据领域和国家/地区搜索开发者
     * @param domain 开发者的专业领域
     * @param nation 开发者的国家或地区
     * @return List<DeveloperDTO> 开发者列表
     */
    @GetMapping("/search")
    public List<DeveloperDTO> searchDevelopers(@RequestParam String domain, @RequestParam String nation) {
        return developerService.searchDevelopers(domain, nation);
    }

    /**
     * 根据名称搜索开发者
     * @param name 开发者的姓名或用户名
     * @return List<DeveloperDTO> 开发者列表
     */
    @GetMapping("/searchByName")
    public List<DeveloperDTO> searchDevelopersByName(@RequestParam String name) {
        return developerService.searchDevelopersByName(name);
    }

    // --- Developer Evaluation and Ranking ---

    /**
     * 获取开发者的评估结果
     * @param username 开发者的 GitHub 用户名
     * @return DeveloperDTO 开发者的数据传输对象
     */
    @GetMapping("/evaluate/{username}")
    public DeveloperDTO getDeveloperEvaluation(@PathVariable String username) {
        return developerService.getDeveloperEvaluation(username);
    }

    /**
     * 生成开发者的评估报告
     * @param developer 开发者对象
     * @return String 评估报告
     */
    @GetMapping("/generateEvaluation")
    public String generateDeveloperEvaluation(@RequestBody Developer developer) {
        return developerService.generateDeveloperEvaluation(developer);
    }

    // --- Contribution and Project Metrics ---

    /**
     * 获取开发者参与的所有项目
     * @param developerId 开发者的 ID
     * @return List<Project> 项目列表
     */
    @GetMapping("/{developerId}/projects")
    public List<Project> getProjectsByDeveloper(@PathVariable Long developerId) {
        Developer developer = new Developer();
        developer.setId(developerId);
        return developerService.getProjectsByDeveloper(developer);
    }

    /**
     * 计算开发者在特定项目中的贡献度
     * @param developerId 开发者的 ID
     * @param projectId 项目的 ID
     * @return double 贡献度得分
     */
    @GetMapping("/{developerId}/contributionScore")
    public double calculateContributionScore(@PathVariable Long developerId, @RequestParam Long projectId) {
        Developer developer = new Developer();
        developer.setId(developerId);
        Project project = new Project();
        project.setId(projectId);
        return developerService.calculateContributionScore(developer, project);
    }

    /**
     * 计算项目的重要程度
     * @param projectId 项目的 ID
     * @return double 项目重要程度得分
     */
    @GetMapping("/projectImportance/{projectId}")
    public double calculateProjectImportance(@PathVariable Long projectId) {
        Project project = new Project();
        project.setId(projectId);
        return developerService.calculateProjectImportance(project);
    }

    // --- Inference and Confidence ---

    /**
     * 推断开发者的国家/地区
     * @param developerId 开发者的 ID
     * @return String 国家或地区
     */
    @GetMapping("/{developerId}/inferNation")
    public String inferNation(@PathVariable Long developerId) {
        Developer developer = new Developer();
        developer.setId(developerId);
        return developerService.inferNation(developer);
    }

    /**
     * 计算开发者的国家/地区置信度
     * @param developerId 开发者的 ID
     * @return double 国家/地区置信度得分
     */
    @GetMapping("/{developerId}/nationConfidence")
    public double calculateNationConfidence(@PathVariable Long developerId) {
        Developer developer = new Developer();
        developer.setId(developerId);
        return developerService.calculateNationConfidence(developer);
    }

    /**
     * 推断开发者的领域
     * @param developerId 开发者的 ID
     * @return String 开发者的领域
     */
    @GetMapping("/{developerId}/inferDomain")
    public String inferDomain(@PathVariable Long developerId) {
        Developer developer = new Developer();
        developer.setId(developerId);
        return developerService.inferDomain(developer);
    }

    /**
     * 计算开发者的领域置信度
     * @param developerId 开发者的 ID
     * @return double 领域置信度得分
     */
    @GetMapping("/{developerId}/domainConfidence")
    public double calculateDomainConfidence(@PathVariable Long developerId) {
        Developer developer = new Developer();
        developer.setId(developerId);
        return developerService.calculateDomainConfidence(developer);
    }

    // --- Data Synchronization ---

    /**
     * 同步 GitHub 数据，更新开发者信息
     */
    @PostMapping("/sync")
    public void syncDeveloperData() {
        developerService.syncDeveloperData();
    }
    @GetMapping("/getprojectcontributorsusernames/{fullname}")
    public List<String> getProjectContributorsUsernames(@PathVariable String fullname) throws IOException {
        return gitHubApiUtil.getProjectContributorsUsernames(fullname);
    }
}

