# Distributed Systems Code Base

Collection of Distributed System algorithm implementations and examples

### sockets.echoserver  

Illustrates building echo server & client using Socket's API

**  Running** :

* Initialize Server instance by running:  

    ``` 
    sbt "runMain com.am.ds.sockets.echoserver.Server" 
    ```  

* In another terminal window, initialize Client instance by running:  

    ``` 
    sbt "runMain com.am.ds.sockets.echoserver.Client" 
    ```  

NOTE: You could terminate server instance by pressing `Ctrl+C`