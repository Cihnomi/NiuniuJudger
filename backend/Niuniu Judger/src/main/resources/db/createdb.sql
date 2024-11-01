CREATE TABLE Developer (
                           id BIGINT PRIMARY KEY,
                           github_id VARCHAR(255) UNIQUE NOT NULL,
                           username VARCHAR(255) NOT NULL,
                           avatar_url VARCHAR(512),
                           bio TEXT,
                           company VARCHAR(255),
                           location VARCHAR(255),
                           email VARCHAR(255),
                           public_repos INT,
                           name VARCHAR(255),
                           profile_url VARCHAR(512),
                           blog VARCHAR(512),
                           talent_rank DOUBLE,
                           nation VARCHAR(255),
                           nation_confidence DOUBLE,
                           domain VARCHAR(255),
                           domain_confidence DOUBLE,
                           followers_count INT,
                           following_count INT,
                           page_rank_score DOUBLE,
                           contribution_value DOUBLE,
                           html_url VARCHAR(512),
                           comment TEXT
);

CREATE TABLE Project (
                         id BIGINT PRIMARY KEY,
                         github_id VARCHAR(255) UNIQUE NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         full_name VARCHAR(255),
                         description TEXT,
                         stars INT,
                         forks INT,
                         watchers INT
);
CREATE TABLE contributors (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              username VARCHAR(255) NOT NULL,
                              project_id VARCHAR(255) NOT NULL,
                              contributions INT NOT NULL,
                              UNIQUE (username, project_id),
                              FOREIGN KEY (project_id) REFERENCES projects(github_id)
);
