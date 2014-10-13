Play-Cassandra
=========

A Play Plugin for using Cassandra

The Plugin initializes a Cassandra Session on startup and provides access to session 
and other properties through the `Cassandra` object.
 
The Plugin also provides play-evolutions like functionality if it is not disabled. 

####Usage
In library dependencies, include
 
```
"com.tuplejump" %% "play-cassandra" % "1.0.0-SNAPSHOT"
```

Now, Cassandra host, port, cluster and session can be accessed through the API exposed by the Plugin.
In addition to that, a method `loadCQLFile` is also available. The API is documented at TODO

**Note:The cluster and session exposed are closed by the Plugin when the application is stopped.**

#####Evolution

Evolution is enabled by default and the file names are expected to be integers in order,
similar to play-evolutions for SQL or SQL-like databases.
**The configuration property `cassandraPlugin.appName` should be set when evolution is enabled. 
 The plugin adds an entry for each appName and the default value is `appWithCassandraPlugin`** 

To disable evolution, add the following to `conf/application.conf`,

```
cassandraPlugin.evolution.enabled=false
```

**Note: The plugin loads before `GlobalSettings`, so it is accessible in a custom `Global` object.**

####Configuration
The default configuration is,

```
cassandraPlugin {
  //host and port of where Cassandra is running
  host = "127.0.0.1"    
  port = 9042           
  
  evolution {
    enabled = true
    directory = "evolutions/cassandra/" //directory within conf to look for CQL files
  }
  
  appName = "appWithCassandraPlugin" // appName to be saved in DB when using evolutions
}
```

