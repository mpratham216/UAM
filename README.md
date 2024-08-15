UAM Project SQL Set UP

create database UAM_DB;

USE UAM_DB;
CREATE TABLE USERS(
	user_id INT auto_increment primary key,
    firstname varchar(50) NOT NULL,
    lastname varchar(50) NOT NULL,
    username varchar(100) NOT NULL unique,
    password varchar(255) not null,
    email varchar(100) not null unique
);



ALTER TABLE users
ADD COLUMN role varchar(10) not null default 'user';

CREATE TABLE resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    request_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
);


CREATE TABLE user_resources (
    user_id INT,
    resource_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
);

ALTER TABLE users
ADD COLUMN manager_id INT,
ADD CONSTRAINT fk_manager
FOREIGN KEY (manager_id) REFERENCES users(user_id);

ALTER TABLE resources ADD COLUMN manager_only BOOLEAN DEFAULT FALSE;

select * from resources;
select * from users;
select * from user_resources;
select * from requests;

desc user_resources;
