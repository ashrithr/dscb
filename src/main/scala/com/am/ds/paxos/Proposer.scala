package com.am.ds.paxos

import java.io.{FileReader, BufferedReader, FileWriter, File}

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Proposer class
 * TODO write a detail description of what propose does
 * @author ashrith
 */
class Proposer(dataDir: File, acceptors: Seq[ActorRef], firstN: Int, nIncrement: Int) extends Actor {
  val log = Logging(context.system, this)
  val proposalNumberFile = new File(dataDir, s"${self.path.name}-proposalNumber")
  val T = 1000.millis

  override def receive: Actor.Receive = leaderElection orElse preparation

  case class Leader(name: String, receivedTs: Long)

  private var leader: Option[Leader] = None

  // The current proposal number
  private var n  = firstN

  // The value that we are going to propose
  private val v = Random.nextInt(10)

  private var prepareResponses: Seq[(Int, Option[Proposal])] = Nil

  // Set to true once our proposal has been accepted
  private var complete = false

  def leaderElection: Receive = {
    case Proposers(proposes: Seq[ActorRef]) => {
      proposes.foreach { p =>
        // send my name to all proposers every T-100 millis
        context.system.scheduler.schedule(
          0.millis,
          T - 100.millis,
          p,
          StandForElection(self.path.name)
        )
      }
      // Start a proposal cycle every T millis
      context.system.scheduler.schedule(T, T, self, StartPreparation)
    }
    case StandForElection(name: String) => {
      val now = System.currentTimeMillis
      leader match {
        case None => updateLeader(name, now)
        case Some(Leader(_, ts)) if (now - ts).millis > T => updateLeader(name, now)
        /*
          The Paxons chose as president the priest whose name was last in alphabetical order
          among the names of all priests in the chamber, though we don't know exactly how this was
          done
         */
        case Some(Leader(pName, _)) if pName < name => updateLeader(name, now)
        case _ => // Keep the current leader
      }
    }
  }

  def updateLeader(name: String, now: Long): Unit = {
    leader = Some(Leader(name, now))
    log.debug(s"Updated leader, it is now $name")
  }

  def preparation: Receive = {
    case StartPreparation => {
      if (!complete && iAmTheLeader) {
        incrementProposalNumber()
        acceptors.foreach(_ ! PrepareRequest(n))
      }
    }
    case resp @ PrepareResponse(proposalNumber, prevProposal) => {
      log.debug(s"Received a prepare response: $resp. Current n is $n")

      // make a note of the response
      prepareResponses = (proposalNumber, prevProposal) +: prepareResponses
      log.debug(s"Added response to prepareResponses list. $prepareResponses")

      // remove any old responses that we don't need any more
      prepareResponses = prepareResponses.filter { case(pN, _) => pN == n }
      log.debug(s"Removed responses with old proposal numbers. $prepareResponses")

      // if we have a quorum of responses, send an AcceptRequest
      if (prepareResponses.size > acceptors.size / 2) {
        log.info(s"Received PrepareResponses from a majority of acceptors. $prepareResponses")

        // choose the value of the highest numbered proposal amongst the responses, or our
        // own value if there were no responses
        val valueToPropose = prepareResponses.collect { case (_, Some(proposal)) => proposal }
          .sortBy { case Proposal(pN, _) => pN }
          .reverse
          .headOption
          .map { case Proposal(_, value) => value }
          .getOrElse(v)

        for (acceptor <- acceptors) acceptor ! AcceptRequest(Proposal(n, valueToPropose))
        log.debug(s"Sent AcceptRequest for proposal(n=$n, v=$valueToPropose)")

        prepareResponses = Nil
        log.debug(s"Cleared prepareResponses list")
      }
    }
    case AcceptResponse(proposal) => {
      log.info(s"Success! My proposal $proposal was accepted by ${sender.path.name}")
      complete = true
    }
  }

  private def iAmTheLeader = {
    leader map { l =>
      l.name == self.path.name
    } getOrElse {
      // Haven't received any messages from proposers. That's weird.
      // Just assume I'm the leader.
      true
    }
  }

  private def incrementProposalNumber(): Unit = {
    n += nIncrement
    writeProposalNumberToFile(n)
  }

  def writeProposalNumberToFile(n: Int): Unit = {
    val writer = new FileWriter(proposalNumberFile)
    try {
      writer.write(n.toString)
      log.debug(s"Wrote proposal number $n to file")
    } finally {
      writer.close()
    }
  }

  def readProposalNumberFromFile(): Int = {
    val reader: BufferedReader = new BufferedReader(new FileReader(proposalNumberFile))
    try {
      val n = reader.readLine().toInt
      log.debug(s"Read proposal number $n from file")
      n
    } finally {
      reader.close()
    }
  }

  override def preStart(): Unit = {
    if (proposalNumberFile.exists()) {
      n = readProposalNumberFromFile()
    }
  }
}
