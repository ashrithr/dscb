# Distributed Systems Code Base

Collection of Distributed System algorithm implementations and examples

### sockets.echoserver  

Illustrates building echo server & client using [Socket](http://docs.oracle.com/javase/7/docs/api/java/net/Socket.html)'s API

Running:

* Initialize Server instance by running:  

    ```
    sbt "runMain com.am.ds.sockets.echoserver.Server"       
    ```

* In another terminal window, initialize Client instance by running:  
 
    ```
    sbt "runMain com.am.ds.sockets.echoserver.Client" 
    ```

NOTE: You could terminate server instance by pressing `Ctrl+C`

### akka.chatserver  

Chat Server implementation using [Akka](http://akka.io) & [Socket](http://docs.oracle.com/javase/7/docs/api/java/net/Socket.html)'s API

  Running:  

* Initialize server side application  

    ``` 
    sbt "runMain com.am.ds.akka.chatserver.ChatServerApp 9000" 
    ```  

* Initialize multiple command line client application instance's to simulate multiple users using the chat engine  

    ``` 
    sbt "runMain com.am.ds.akka.chatserver.CmdLineClient.CmdLineClientApp localhost 9000" 
    ```

NOTE: you could terminate server and client instance's by pressing `Ctrl+C` in their respective terminal windows