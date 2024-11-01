package com.example.Niuniu_Judger.service;

import com.example.Niuniu_Judger.dto.DeveloperDTO;
import com.example.Niuniu_Judger.model.Developer;
import com.example.Niuniu_Judger.model.Project;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DeveloperService {

    // --- Developer Retrieval ---

    /**
     * 根据ID获取开发者信息
     * @param githubId 开发者ID
     * @return DeveloperDTO
     */
    DeveloperDTO getDeveloperByGithubId(Long githubId);

    /**
     * 根据name获取开发者信息
     * @param name 开发者的用户名
     * @return DeveloperDTO
     */
    DeveloperDTO getDeveloperByUsername(String name);

    /**
     * 根据名称搜索开发者
     * @param name 开发者姓名或用户名
     * @return 开发者列表
     */
    List<DeveloperDTO> searchDevelopersByName(String name);

    // --- Developer Search and Filtering ---

    /**
     * 搜索开发者
     * @param domain 领域
     * @param nation 国家/地区
     * @return 开发者列表
     */
    List<DeveloperDTO> searchDevelopers(String domain, String nation);

    // --- Developer Evaluation and Ranking ---

    /**
     * 获取开发者的评估结果
     * @param username 开发者的 GitHub 用户名
     * @return DeveloperDTO
     */
    DeveloperDTO getDeveloperEvaluation(String username);

    /**
     * 生成开发者评估报告
     * @param developer 开发者对象
     * @return 评估报告
     */
    String generateDeveloperEvaluation(Developer developer);

    /**
     * 计算开发者的 PageRank 分数
     * @param developer 开发者对象
     * @return PageRank 分数
     */
    double calculateDeveloperPageRank(Developer developer);

    /**
     * 计算开发者的 TalentRank，结合 PageRank 和其他指标
     * @param developer 开发者对象
     * @return TalentRank 值
     */
    double calculateDeveloperTalentRank(Developer developer);

    // --- Contribution and Project Metrics ---

    /**
     * 获取开发者参与的所有项目
     * @param developer 开发者对象
     * @return 项目列表
     */
    List<Project> getProjectsByDeveloper(Developer developer);

    /**
     * 获取开发者在项目中的贡献度
     * @param developer 开发者对象
     * @param project 项目对象
     * @return 贡献度得分
     */
    double calculateContributionScore(Developer developer, Project project);

    /**
     * 计算项目的重要程度
     * @param project 项目对象
     * @return 项目重要程度得分
     */
    double calculateProjectImportance(Project project);

    /**
     * 计算开发者的平均加权贡献度
     * @param developer 开发者对象
     * @return 平均加权贡献度得分
     */
    double calculateAverageWeightedContribution(Developer developer);

    // --- Inference and Confidence ---

    /**
     * 推断开发者的国家/地区
     * @param developer 开发者对象
     * @return 国家/地区
     */
    String inferNation(Developer developer);

    /**
     * 计算开发者的国家/地区置信度
     * @param developer 开发者对象
     * @return 置信度得分
     */
    double calculateNationConfidence(Developer developer);

    /**
     * 推断开发者的领域
     * @param developer 开发者对象
     * @return 领域
     */
    String inferDomain(Developer developer);

    /**
     * 计算开发者的领域置信度
     * @param developer 开发者对象
     * @return 置信度得分
     */
    double calculateDomainConfidence(Developer developer);

    // --- Data Synchronization ---

    /**
     * 同步GitHub数据，更新开发者信息
     */
    void syncDeveloperData();

}

