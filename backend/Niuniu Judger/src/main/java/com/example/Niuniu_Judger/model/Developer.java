package com.example.Niuniu_Judger.model;

public class Developer {
    private Long id;
    private String githubId;
    private String username;
    private String avatarUrl;
    private String bio;
    private String company;
    private String location;
    private String email;
    private int publicRepos;
    private String name;
    private String profileUrl;
    private String blog;
    private double talentRank;
    private String nation;
    private double nationConfidence;
    private String domain;
    private double domainConfidence;
    private int followersCount;
    private int followingCount;
    private double pageRankScore;
    private double contributionValue;

    private String htmlUrl;
    private String comment;

    public Developer() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGithubId() {
        return githubId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public double getTalentRank() {
        return talentRank;
    }

    public void setTalentRank(double talentRank) {
        this.talentRank = talentRank;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public double getNationConfidence() {
        return nationConfidence;
    }

    public void setNationConfidence(double nationConfidence) {
        this.nationConfidence = nationConfidence;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public double getDomainConfidence() {
        return domainConfidence;
    }

    public void setDomainConfidence(double domainConfidence) {
        this.domainConfidence = domainConfidence;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public double getPageRankScore() {
        return pageRankScore;
    }

    public void setPageRankScore(double pageRankScore) {
        this.pageRankScore = pageRankScore;
    }

    public double getContributionValue() {
        return contributionValue;
    }

    public void setContributionValue(double contributionValue) {
        this.contributionValue = contributionValue;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
    public String getHtmlUrl(){
        return htmlUrl;
    }
    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return comment;
    }
}

