CREATE TABLE IF NOT EXISTS ratings
(
    rating_id INT PRIMARY KEY AUTO_INCREMENT,
    code      VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    user_id  INT PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL UNIQUE,
    login    VARCHAR(255) NOT NULL UNIQUE,
    name     VARCHAR(255),
    birthday DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    film_id      INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(200),
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL,
    rating_id    INT,
    FOREIGN KEY (rating_id) REFERENCES ratings (rating_id)
);

CREATE TABLE IF NOT EXISTS directors
(
    director_id INT PRIMARY KEY AUTO_INCREMENT,
    director_name VARCHAR(60) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_directors
(
    film_id INT,
    director_id INT,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (director_id) REFERENCES directors(director_id)
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INT PRIMARY KEY AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  INT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id),
    FOREIGN KEY (genre_id) REFERENCES genres (genre_id)
);

CREATE TABLE IF NOT EXISTS friendships
(
    user_id   INT,
    friend_id INT,
    status    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (friend_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    film_id INT,
    user_id INT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS reviews
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    content     TEXT    NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id     BIGINT  NOT NULL,
    film_id     BIGINT  NOT NULL,
    useful      INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_reactions
(
    review_id BIGINT  NOT NULL,
    user_id   BIGINT  NOT NULL,
    is_like   BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id),
    CONSTRAINT fk_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS events
(
    event_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp  BIGINT      NOT NULL,
    user_id    INT         NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    operation  VARCHAR(20) NOT NULL,
    entity_id  INT         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_likes_film_id ON likes (film_id);
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes (user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_user_id ON friendships (user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_friend_id ON friendships (friend_id);
CREATE INDEX IF NOT EXISTS idx_reviews_film_id ON reviews(film_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_useful ON reviews(useful);
CREATE INDEX IF NOT EXISTS idx_events_user_id ON events(user_id);
CREATE INDEX IF NOT EXISTS idx_events_timestamp ON events(timestamp);