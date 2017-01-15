/* DROPPING OLD SCHEMA*/
DROP TABLE IF EXISTS donit.tasks;
DROP TABLE IF EXISTS donit.habits;
DROP TABLE IF EXISTS donit.goals;
DROP TABLE IF EXISTS donit.users;
DROP DATABASE IF EXISTS donit;

CREATE DATABASE donit ENCODING = 'UTF-8';
SET DATABASE = donit;

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,

    username CHAR(20) ,
    email CHAR(256) NOT NULL,

    join_at TIMESTAMP NOT NULL DEFAULT current_timestamp(),
    auth_pass CHAR(256) NOT NULL,
    auth_salt CHAR(20) NOT NULL,

    CONSTRAINT uname_uid UNIQUE(username)
);

CREATE TABLE goals(
    user_id SERIAL, goal_id SERIAL,
    PRIMARY KEY(user_id, goal_id),

    name CHAR(60) NOT NULL,
    description CHAR(100),
    last_updated TIMESTAMP DEFAULT current_timestamp() NOT NULL,
    img_url  STRING,

    done BOOL,
    visibility INT NOT NULL,

    CONSTRAINT user_fk FOREIGN KEY (user_id)
            REFERENCES users(user_id)
);

CREATE TABLE tasks(
    user_id SERIAL, goal_id SERIAL, task_id INT,
    PRIMARY KEY(user_id, goal_id, task_id),

    name CHAR(60) NOT NULL,
    done BOOL NOT NULL,
    /* when to remind for a task */
    at TIMESTAMP,
    /* duration of this task in minutes */
    duration INT NOT NULL,
    CONSTRAINT valid_task_duration CHECK (duration < 86400),

    CONSTRAINT task_fk FOREIGN KEY (user_id, goal_id)
            REFERENCES goals(user_id, goal_id)
);

CREATE TABLE habits (
    user_id SERIAL, goal_id SERIAL, habit_id INT,
    PRIMARY KEY(user_id, goal_id, habit_id),

    name CHAR(60) NOT NULL,
    done BOOL NOT NULL,
    /* repeat on which day */
    days INT8 NOT NULL,
    /* the offset from that day: precision sec */
    time_offset INT NOT NULL,
    CONSTRAINT valid_habit_offset CHECK (time_offset < 86400),
    /* duration of this habit: precision sec */
    duration INT NOT NULL,
    CONSTRAINT valid_task_duration CHECK (duration < 86400),

    CONSTRAINT habit_fk FOREIGN KEY(user_id, goal_id)
            REFERENCES goals(user_id, goal_id)
)