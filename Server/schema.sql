drop table if exists users;
CREATE TABLE `users` (
  `id`  INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
  `location`  TEXT,
  `last_updated`  INTEGER
)
