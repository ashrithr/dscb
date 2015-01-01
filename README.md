# Distributed Systems Code Base

Collection of Distributed System algorithm implementations and examples

### [sockets.echoserver  ](src/main/scala/com/am/ds/sockets/echoserver)

Illustrates building echo server & client using
[Socket](http://docs.oracle.com/javase/7/docs/api/java/net/Socket.html)'s API

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

### [akka.chatserver  ](src/main/scala/com/am/ds/akka/chatserver)

Chat Server implementation using [Akka](http://akka.io) &
[Socket](http://docs.oracle.com/javase/7/docs/api/java/net/Socket.html)'s API

  Running:  

* Initialize server side application:  

    ``` 
    sbt "runMain com.am.ds.akka.chatserver.ChatServerApp 9000" 
    ```  

* Initialize multiple command line client application instance's to simulate multiple users using
the chat engine  

    ``` 
    sbt "runMain com.am.ds.akka.chatserver.CmdLineClient.CmdLineClientApp localhost 9000" 
    ```

NOTE: you could terminate server and client instance's by pressing `Ctrl+C` in their respective
terminal windows

### [rmi.networkgame](src/main/scala/com/am/ds/rmi/networkgame)

A simple Tron game implementation using
[RMI](http://docs.oracle.com/javase/7/docs/api/java/rmi/package-summary.html)

Running:

* Initialize RMI Tron server instance:

    ```
    sbt "runMain com.am.ds.rmi.networkgame.RMITronServer"
    ```

* Initialize **two** instances of Tron client's in two terminal windows:

    ```
    sbt "runMain com.am.ds.rmi.networkgame.RMITronClient"
    ```

You could play the game by pressing the frame to get the focus and then pressing either `left` or
`right` arrows to move the line. Finally you could terminate the client and server session by
sending `Ctrl+C`.

### [logicalclocks](src/main/scala/com/am/ds/logicalclocks)

Implementation of [Lamport](http://en.wikipedia.org/wiki/Lamport_timestamps) and
[Vector](http://en.wikipedia.org/wiki/Vector_clock) Clock's

### [nameservices](src/main/scala/com/am/ds/nameservices)

Simple example illustrating how to perform DNS forward and reverse lookup's

### [serialization](src/main/scala/com/am/ds/serialization)

Example's illustrating how to perform simple file write and read serialization using

* [Java](http://docs.oracle.com/javase/tutorial/jndi/objects/serial.html) Serialization
* [Kryo](https://github.com/EsotericSoftware/kryo) Serialization
* [Scala Pickling](https://github.com/scala/pickling) (**TODO**)

### [paxos](src/main/scala/com/am/ds/paxos)

Implementation of [Paxos](http://en.wikipedia.org/wiki/Paxos_(computer_science)) using
[Akka](http://akka.io)

### [raft](src/main/scala/com/am/ds/raft)

An implementation of the [Raft distributed consensus algorithm](http://raftconsensus.github.io)

Current implementation features:

* Leader Election
* Log Replication
* Cluster Membership Changes
* Log Compaction
* Finagle based Thrift RPC between members

Running an example distributed KVStore built using RAFT: `sbt test`.