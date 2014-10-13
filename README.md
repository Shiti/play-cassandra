Play-Cassandra
=========

A Play Plugin for using Cassandra

The Plugin initializes a Cassandra Session on startup and provides access to session 
and other properties through the `Cassandra` object. 

####Usage
In library dependencies, include
 
```
"com.tuplejump" %% "play-cassandra" % "1.0.0-SNAPSHOT"
```

####Configuration
The default configuration is,

```
cassandraPlugin {
  //host and port of where cassandra is running
  host = "127.0.0.1"    
  port = 9042           
  
  evolution {
    enabled = true
    directory = "evolutions/cassandra/" //directory within conf to look for CQL files
  }
  
  appName = "appWithCassandraPlugin" // appName to be saved in DB when using evolutions
}
```

