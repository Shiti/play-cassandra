CREATE KEYSPACE IF NOT EXISTS casplugin
    WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

CREATE TABLE IF NOT EXISTS casplugin.revision_history(
    app_id text,
    applied_at timestamp,
    revision int,
     PRIMARY KEY(app_id));