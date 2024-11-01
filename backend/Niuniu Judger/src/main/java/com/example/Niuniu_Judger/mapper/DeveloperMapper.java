package com.example.Niuniu_Judger.mapper;

import com.example.Niuniu_Judger.model.Developer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeveloperMapper {

    int insertDeveloper(Developer developer);

    int updateDeveloper(Developer developer);

    Developer selectDeveloperByUsername(@Param("name") String name);

    Developer selectDeveloperByGitHubId(@Param("githubId") String githubId);

    List<Developer> selectDevelopersByCriteria(@Param("domain") String domain,
                                               @Param("nation") String nation);

    void updateDeveloperScores(Developer developer);

    void updateDeveloperTalentRank(String username, double talentRank);
}

