package com.example.Niuniu_Judger.mapper;

import com.example.Niuniu_Judger.model.Contributor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContributorMapper {
    int insertContributor(Contributor contributor);

    int updateContributorContributions(@Param("username") String username, @Param("projectId") String projectId, @Param("contributions") int contributions);

    List<Contributor> selectContributorsByProjectId(@Param("projectId") String projectId);

    Contributor selectContributorByUsernameAndProjectId(@Param("username") String username, @Param("projectId") String projectId);
}
