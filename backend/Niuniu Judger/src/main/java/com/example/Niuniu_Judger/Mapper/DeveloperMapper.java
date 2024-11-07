package com.example.Niuniu_Judger.mapper;

import com.example.Niuniu_Judger.model.Developer;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DeveloperMapper {

    // 插入一个Developer
    @Insert("INSERT INTO developers (github_id, username, avatarUrl, bio, company, location, email, publicRepos, name, profileUrl, blog, talentRank, nation, domain, followersCount, followingCount, FollowerScore, contributionValue, htmlUrl, comment) " +
            "VALUES (#{githubId}, #{username}, #{avatarUrl}, #{bio}, #{company}, #{location}, #{email}, #{publicRepos}, #{name}, #{profileUrl}, #{blog}, #{talentRank}, #{nation}, #{domain}, #{followersCount}, #{followingCount}, #{FollowerScore}, #{contributionValue}, #{htmlUrl}, #{comment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertDeveloper(Developer developer);

    // 根据ID查询Developer
    @Select("SELECT * FROM developers WHERE id = #{id}")
    Developer getDeveloperById(Long id);

    // 查询所有Developer
    @Select("SELECT * FROM developers")
    List<Developer> getAllDevelopers();

    // 根据GitHub ID查询Developer
    @Select("SELECT * FROM developers WHERE github_id = #{githubId}")
    Developer getDeveloperByGithubId(String githubId);

    // 更新Developer信息
    @Update("UPDATE developers SET github_id = #{githubId}, username = #{username}, avatarUrl = #{avatarUrl}, bio = #{bio}, company = #{company}, location = #{location}, email = #{email}, " +
            "publicRepos = #{publicRepos}, name = #{name}, profileUrl = #{profileUrl}, blog = #{blog}, talentRank = #{talentRank}, nation = #{nation}, domain = #{domain}, " +
            "followersCount = #{followersCount}, followingCount = #{followingCount}, FollowerScore = #{FollowerScore}, contributionValue = #{contributionValue}, htmlUrl = #{htmlUrl}, comment = #{comment} " +
            "WHERE id = #{id}")
    void updateDeveloper(Developer developer);

    // 删除Developer
    @Delete("DELETE FROM developers WHERE id = #{id}")
    void deleteDeveloper(Long id);
}
