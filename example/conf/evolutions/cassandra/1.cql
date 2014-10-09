# Playlist table

CREATE KEYSPACE IF NOT EXISTS music
 WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1};


CREATE TABLE IF NOT EXISTS music.playlists (
                                             id uuid,
                                             song_id uuid,
                                             title text,
                                             album text,
                                             artist text,
                                             PRIMARY KEY  (id,song_id) );
