package com.example.Niuniu_Judger.service;

import com.example.Niuniu_Judger.dto.DeveloperDTO;
import com.example.Niuniu_Judger.model.Developer;
import com.example.Niuniu_Judger.model.Project;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface DeveloperService {


    /**
     * 根据名称搜索开发者
     * @param name 开发者姓名或用户名
     * @return 开发者列表
     */
    List<DeveloperDTO> searchDevelopersByName(String name);

    /**
     * 获取开发者的评估结果
     * @param username 开发者的 GitHub 用户名
     * @return DeveloperDTO
     */
    DeveloperDTO getDeveloperEvaluation(String username);

    /**
     * 生成开发者评价
     * @param developer 开发者对象
     * @return 评估报告
     */
    String generateDeveloperEvaluation(Developer developer) throws IOException;

    DeveloperDTO getDeveloperEvaluationv2(String username);
    /**
     * 计算开发者的 Follower 分数
     * @param developer 开发者对象
     * @return Follower 分数
     */
    /*
    double calculateDeveloperFollower(Developer developer) throws IOException;
*/
    /**
     * 计算开发者的 TalentRank，结合 Follower分数 和 项目贡献度分数
     * @param developer 开发者对象
     * @return TalentRank 值
     */
    double calculateDeveloperTalentRank(Developer developer) throws IOException;

    /**
     * 获取开发者参与的所有项目
     * @param developer 开发者对象
     * @return 项目列表
     */
    List<Project> getProjectsByDeveloper(Developer developer) throws IOException;

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
    double calculateAverageWeightedContribution(Developer developer) throws IOException;

    /**
     * 推断开发者的国家/地区
     * @param developer 开发者对象
     * @return 国家/地区
     */
    String inferNation(Developer developer);

    /**
     * 推断开发者的领域
     * @param developer 开发者对象
     * @return 领域
     */
    String inferDomain(Developer developer) throws IOException;

    /**
     * 根据领域搜索开发者
     * @param keyword 领域
     * @return List
     */
    List<DeveloperDTO> searchDevelopersByKeyword(String keyword);
}

