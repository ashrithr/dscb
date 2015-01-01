package com.am.ds.raft.states

import java.util.Random
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

import com.am.ds.raft.rpc._
import com.am.ds.raft.util.Logging
import com.am.ds.raft.{Member, Cluster}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise

import com.am.ds.raft.util.RaftConversions.fromFunctionToRunnable

/**
 * Description goes here
 * @author ashrith 
 */
/**
 *  •! RePCs from candidates and leaders.
 * •! Convert to candidate if election timeout elapses without
 * either:
 * •! Receiving valid AppendEntries RPC, or
 * •! Granting vote to candidate
 */
class Follower(cluster: Cluster, passive: Boolean = false, term: Int, leaderPromise: Promise[Member], vote: Option[String]) extends State(term, leaderPromise, vote) {

  val electionTimeout = new ElectionTimeout(cluster, term)

  override def begin() = {
    if (!passive) electionTimeout restart

    if (leaderPromise.isCompleted) LOG.info("Following {} in term[{}]", cluster.leader,term)
  }

  override def stop(stopTerm: Int) = {
    if (stopTerm > term) {
      electionTimeout stop
    }
  }

  override def on[T](command: Command): Future[T] = cluster.forwardToLeader[T](command)

  override def on(appendEntries: AppendEntries): Future[AppendEntriesResponse] = {
    if (appendEntries.term < term) {
      Future.successful(AppendEntriesResponse(term, false))
    } else {
      electionTimeout restart

      if (appendEntries.term > term) {
        stepDown(appendEntries.term, Some(appendEntries.leaderId))
        cluster.local on appendEntries
      } else {

        if (!leaderPromise.isCompleted) {
          cluster.obtainMember(appendEntries.leaderId) map { leader =>
            if (leaderPromise.trySuccess(leader)) {
              LOG.info("Following {} in term[{}]", cluster.leader,term)
              cluster.local.persistState
            }
          }
        }

        cluster.rlog.tryAppend(appendEntries) map { success =>
          AppendEntriesResponse(cluster.local.term, success)
        }
      }
    }
  }

  override def on(requestVote: RequestVote): Future[RequestVoteResponse] = {
    requestVote.term match {
      case requestTerm if requestTerm < term => Future.successful(RequestVoteResponse(term, false))
      case requestTerm if requestTerm > term => {
        stepDown(requestVote.term, None)
        cluster.local on requestVote
      }
      case requestTerm if requestTerm == term => {
        val couldGrantVote = checkGrantVotePolicy(requestVote)
        if (couldGrantVote) {
          if (votedFor.compareAndSet(None, Some(requestVote.memberId)) || votedFor.get().equals(Some(requestVote.memberId))) {
            LOG.debug(s"Granting vote to ${requestVote.memberId} in term[${term}]")
            electionTimeout.restart
            cluster.local.persistState()
            Future.successful(RequestVoteResponse(term, true))
          } else {
            LOG.debug(s"Rejecting vote to ${requestVote.memberId} in term[${term}]. Already voted for ${votedFor.get()}")
            Future.successful(RequestVoteResponse(term, false))
          }
        } else {
          LOG.debug(s"Rejecting vote to ${requestVote.memberId} in term[${term}]")
          Future.successful(RequestVoteResponse(term, false))
        }
      }
    }
  }

  private def checkGrantVotePolicy(requestVote: RequestVote) = {
    val vote = votedFor.get()
    (!vote.isDefined || vote.get == requestVote.memberId) && isMuchUpToDate(requestVote)
  }

  private def isMuchUpToDate(requestVote: RequestVote) = {
    val lastLogEntry = cluster.rlog.getLastLogEntry
    lastLogEntry.isEmpty || (requestVote.lastLogTerm >= lastLogEntry.get.term && requestVote.lastLogIndex >= lastLogEntry.get.index)
  }

  private def isCurrentTerm(term: Int) = term == cluster.local.term

  override def toString = s"Follower[$term]"

  override protected def getCluster: Cluster = cluster

}

class ElectionTimeout(cluster: Cluster, term: Int) extends Logging {

  val scheduledFuture = new AtomicReference[ScheduledFuture[_]]()
  val random = new Random()

  def restart = {
    stop
    start
  }

  private def start = {
    val electionTimeout =  randomTimeout
    LOG.trace(s"New timeout is $electionTimeout ms")
    val task: Runnable = () => {
      LOG.debug("Timeout reached! Time to elect a new leader")
      cluster.local becomeCandidate (term + 1)
    }
    val future = cluster.scheduledElectionTimeoutExecutor.schedule(task, electionTimeout, TimeUnit.MILLISECONDS)
    val previousFuture = scheduledFuture.getAndSet(future)
    cancel(previousFuture)
  }

  private def randomTimeout = {
    val conf = cluster.configuration
    val diff = conf.maxElectionTimeout - conf.minElectionTimeout
    conf.minElectionTimeout + random.nextInt(if (diff > 0) diff.toInt else 1)
  }

  def stop() = {
    val future = scheduledFuture.get()
    cancel(future)
  }

  private def cancel(future: java.util.concurrent.Future[_]) = if (future != null) future.cancel(false)

}