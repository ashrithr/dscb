package com.am.ds.paxos

import java.io.File

import akka.actor._
import akka.routing.BroadcastGroup

/**
 * Runner illustrating Paxos, with leader election
 * @author ashrith
 */
object Main extends App {
  // create data dir if does not exits
  val dataDir = new File("/tmp/data")
  dataDir.mkdirs()

  // root object of our akka application
  val system = ActorSystem("paxos")

  val learners = Seq()

  val acceptors = Seq(
    system.actorOf(Props(new Acceptor(dataDir, learners)), name = "acceptor1"),
    system.actorOf(Props(new Acceptor(dataDir, learners)), name = "acceptor2"),
    system.actorOf(Props(new Acceptor(dataDir, learners)), name = "acceptor3"),
    system.actorOf(Props(new Acceptor(dataDir, learners)), name = "acceptor4"),
    system.actorOf(Props(new Acceptor(dataDir, learners)), name = "acceptor5")
  )

  val proposers = Seq(
    system.actorOf(Props(new Proposer(dataDir, acceptors, 1, 5)), name = "proposer1"),
    system.actorOf(Props(new Proposer(dataDir, acceptors, 1, 5)), name = "proposer2"),
    system.actorOf(Props(new Proposer(dataDir, acceptors, 1, 5)), name = "proposer3"),
    system.actorOf(Props(new Proposer(dataDir, acceptors, 1, 5)), name = "proposer4"),
    system.actorOf(Props(new Proposer(dataDir, acceptors, 1, 5)), name = "proposer5")
  )

  val paths = proposers.map(_.path.toString).toList
  val proposersBroadcast: ActorRef = system.actorOf(BroadcastGroup(paths).props())

  proposersBroadcast ! Proposers(proposers)
}
