drop table if exists users;
CREATE TABLE "users" (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
  `latitude` REAL,
  `longitude` REAL,
  `accuracy` REAL,
  `last_updated` INTEGER
)
