package com.example.Niuniu_Judger.mapper;

import com.example.Niuniu_Judger.model.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMapper {

    int insertProject(Project project);

    int updateProject(Project project);

    Project selectProjectByGitHubId(@Param("githubId") String githubId);
}
