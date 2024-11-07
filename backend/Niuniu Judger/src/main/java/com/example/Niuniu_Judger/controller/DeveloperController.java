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


    /**
     * 根据名称搜索开发者
     * @param name 开发者的姓名或用户名
     * @return List<DeveloperDTO> 开发者列表
     */
    @GetMapping("/searchByName")
    public List<DeveloperDTO> searchDevelopersByName(@RequestParam String name) {
        return developerService.searchDevelopersByName(name);
    }

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
    public String generateDeveloperEvaluation(@RequestBody Developer developer) throws IOException {
        return developerService.generateDeveloperEvaluation(developer);
    }

    /**
     * 获取开发者参与的所有项目
     * @param username 开发者的
     * @return List<Project> 项目列表
     */
    @GetMapping("/{username}/projects")
    public List<Project> getProjectsByDeveloper(@PathVariable String username) throws IOException {
        Developer developer = new Developer();
        developer.setUsername(username);
        return developerService.getProjectsByDeveloper(developer);
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

    /**
     * 根据领域搜索开发者
     * @param keyword 领域
     * @return List
     */
    @GetMapping("/searchbykeyword")
    public List<DeveloperDTO> searchDevelopersByKeyword(@RequestParam String keyword) {
        return developerService.searchDevelopersByKeyword(keyword);
    }
}

