package com.am.ds.paxos

import java.io.{FileReader, BufferedReader, FileWriter, File}

import akka.actor.{Actor, ActorRef}
import akka.event.Logging

/**
 * Acceptor class
 * @author ashrith
 */
class Acceptor(dataDir: File, learners: Seq[ActorRef]) extends Actor {
  val log = Logging(context.system, this)
  val lastAcceptedProposalFile = new File(dataDir, s"${self.path.name}-lastAcceptedProposal")
  val lastRespondedPrepareRequestFile = new File(dataDir, s"${self.path.name}-lastRespondedPrepareRequest")

  private var lastAcceptedProposal: Option[Proposal] = None

  // The number of the highest-numbered prepare request to which we have responded
  private var lastRespondedPrepare: Option[Int] = None

  override def receive: Receive = {
    case PrepareRequest(n) => {
      if (lastRespondedPrepare.map(_ < n).getOrElse(true)) {
        updateLastRespondedPrepare(n)
        sender ! PrepareResponse(n, lastAcceptedProposal)
      }
    }
    case AcceptRequest(proposal) => {
      if (lastRespondedPrepare.map(_ <= proposal.n).getOrElse(true)) {
        // Accept the proposal
        log.debug(s"Accepted proposal: $proposal")
        updateLastAcceptedProposal(proposal)

        // Respond to the proposer
        sender ! AcceptResponse(proposal)

        // Tell the learners the new value
        for (learner <- learners) learner ! Learn(proposal.v)
      }
    }
  }

  private def updateLastRespondedPrepare(n: Int) {
    writeLastRespondedPrepareRequestToFile(n)
    lastRespondedPrepare = Some(n)
  }

  private def updateLastAcceptedProposal(proposal: Proposal) {
    writeLastAcceptedProposalToFile(proposal)
    lastAcceptedProposal = Some(proposal)
  }

  def writeLastRespondedPrepareRequestToFile(value: Int) {
    val writer = new FileWriter(lastRespondedPrepareRequestFile)
    try {
      writer.write(value.toString)
      log.debug(s"Wrote value $value to file")
    } finally {
      writer.close()
    }
  }

  def readLastRespondedPrepareRequestFromFile(): Int = {
    val reader: BufferedReader = new BufferedReader(new FileReader(lastRespondedPrepareRequestFile))
    try {
      val n = reader.readLine().toInt
      log.debug(s"Read proposal number $n from file")
      n
    } finally {
      reader.close()
    }
  }

  def writeLastAcceptedProposalToFile(proposal: Proposal) {
    val writer = new FileWriter(lastAcceptedProposalFile)
    try {
      writer.write(s"${proposal.n} ${proposal.v}")
      log.debug(s"Wrote proposal(n=${proposal.n}, v=${proposal.v}) to file")
    } finally {
      writer.close()
    }
  }

  def readLastAcceptedProposalFromFile(): Proposal = {
    val reader: BufferedReader = new BufferedReader(new FileReader(lastAcceptedProposalFile))
    try {
      val line = reader.readLine().split(" ")
      val proposal = Proposal(line(0).toInt, line(1).toInt)
      log.debug(s"Read proposal $proposal from file")
      proposal
    } finally {
      reader.close()
    }
  }

  override def preStart() {
    if (lastRespondedPrepareRequestFile.exists()) {
      lastRespondedPrepare = Some(readLastRespondedPrepareRequestFromFile())
    }
    if (lastAcceptedProposalFile.exists()) {
      lastAcceptedProposal = Some(readLastAcceptedProposalFromFile())
    }
  }
}
