drop table if exists users;
CREATE TABLE `users` (
  `id`  INTEGER,
  `randid`  INTEGER DEFAULT 0 UNIQUE,
  `latitude`  REAL DEFAULT 0,
  `longitude` REAL DEFAULT 0,
  `accuracy`  REAL DEFAULT 0,
  `last_updated`  INTEGER DEFAULT 0,
  `hit_count` INTEGER DEFAULT 0,
  PRIMARY KEY(`id`)
);
