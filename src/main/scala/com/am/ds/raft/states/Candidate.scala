package com.am.ds.raft.states

import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.SynchronousQueue
import com.am.ds.raft.rpc._
import com.am.ds.raft.util.Logging
import com.am.ds.raft.{Member, Cluster}
import com.twitter.concurrent.NamedPoolThreadFactory
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future
import scala.concurrent.Promise

import com.am.ds.raft.util.RaftConversions._


/**
 * Description goes here
 * @author ashrith 
 */
/** 	•! Increment currentTerm, vote for self
  * •! Reset election timeout
  * •! Send RequestVote RPCs to all other servers, wait for either:
  * •! Votes received from majority of servers: become leader
  * •! AppendEntries RPC received from new leader: step
  * down
  * •! Election timeout elapses without election resolution:
  * increment term, start new election
  * •! Discover higher term: step down (§5.1)
  */
class Candidate(cluster: Cluster, term: Int, leaderPromise: Promise[Member]) extends State(term, leaderPromise, Some(cluster.local.id)) {

  val election = new Election(cluster)

  override def canTransitionTo(newState: State) = {
    newState match {
      case leader:Leader => leader.term == term
      case follower:Follower => follower.term >= term //in case of split vote or being an old candidate
      case _ => newState.term > term
    }
  }

  override def begin() = {
    LOG.debug(s"Start election")
    election.start(term)
  }

  override def on(appendEntries: AppendEntries): Future[AppendEntriesResponse] = {
    if (appendEntries.term < cluster.local.term) {
      Future.successful(AppendEntriesResponse(cluster.local.term, false))
    }
    else {
      LOG.debug("Leader already elected in term[{}]", term)
      election.abort
      //warn lock
      stepDown(appendEntries.term, Some(appendEntries.leaderId))
      cluster.local on appendEntries
    }
  }

  override def on(requestVote: RequestVote): Future[RequestVoteResponse] = {
    if (requestVote.term <= term) {
      Future.successful(RequestVoteResponse(term, false))
    } else {
      election.abort
      stepDown(requestVote.term, None)
      cluster.local on requestVote
    }
  }

  override def on[T](command: Command): Future[T] = {
    cluster.forwardToLeader[T](command)
  }

  override def toString = s"Candidate[$term]"

  override protected def getCluster: Cluster = cluster

}

class Election(cluster: Cluster) extends Logging {

  val executor =  new ThreadPoolExecutor(1, 1,
    60L, TimeUnit.SECONDS,
    new SynchronousQueue[Runnable](),
    new NamedPoolThreadFactory("CandidateElection-worker", true))
  val electionFutureTask = new AtomicReference[java.util.concurrent.Future[_]]()

  def start(inTerm: Int) = {
    val task: Runnable = () => {
      val votes = cluster collectVotes inTerm

      LOG.debug(s"Got ${votes.size} votes in a majority of ${cluster.majority}")
      if (cluster.reachMajority(votes)) {
        cluster.local becomeLeader inTerm
      } else {
        LOG.info(s"Not enough votes to be a Leader")
        cluster.local.becomeFollower(term = inTerm, vote = Some(cluster.local.id)) //voted for my self when Candidate
      }
    }
    electionFutureTask.set(executor.submit(task))
  }

  def abort = {
    LOG.debug("Abort Election")
    val future = electionFutureTask.get()
    if (future != null) future.cancel(true)
  }

}