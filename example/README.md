This is an demo of play-cassandra Plugin. 

####Prerequisites
1. Java >=1.6  
2. Cassandra (>=2.0.4) running at localhost:9042. If not change the host and port in the configuration
3. SBT 

####To run the project,
Execute the following commands from project home,

```
[example]$ sbt
[PlayCassandraDemo]$ run
```

The application will be started at localhost:9000. 
Initially, the example keyspace - music ,given in Cassandra documentation
 is generated with table playlists by loading script `evolutions\cassandra\1.cql`

On the applications home page, if the button add sample songs is clicked, 
`Playlist.sampleSongs` are inserted into the database and the view is reloaded.