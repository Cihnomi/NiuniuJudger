package com.example.Niuniu_Judger.dto;

import lombok.Data;

@Data
public class DeveloperDTO {
    private Long id;
    private String githubId;
    private String username;
    private String avatarUrl;
    private String bio;
    private String location;
    private String email;
    private int publicRepos;
    private String name;
    private String profileUrl;
    private String blog;
    private double talentRank;
    private String nation;
    private String domain;
    private int followersCount;
    private int followingCount;
    private double FollowerScore;
    private double contributionValue;
    private String htmlUrl;
    private String comment;
}