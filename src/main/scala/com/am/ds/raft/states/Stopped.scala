package com.am.ds.raft.states


import com.am.ds.raft.Cluster
import com.am.ds.raft.rpc.{RequestVoteResponse, RequestVote, AppendEntriesResponse, AppendEntries}
import com.am.ds.raft.stats.{NonLeaderInfo, StateInfo}

import scala.concurrent.{Future, Promise}

/**
 * Description goes here
 * @author ashrith 
 */
case object Stopped extends State(Int.MaxValue, Promise.failed(new IllegalStateException("Stopped"))) {

  override def begin() = {}

  override def on(appendEntries: AppendEntries): Future[AppendEntriesResponse] = Future.successful(AppendEntriesResponse(appendEntries.term, false))

  override def on(requestVote: RequestVote): Future[RequestVoteResponse] = Future.successful(RequestVoteResponse(requestVote.term,false))

  override def canTransitionTo(state: State): Boolean = false

  override def stepDown(term: Int, leaderId: Option[String]) = { }

  override def info(): StateInfo = NonLeaderInfo("")

  override protected def getCluster: Cluster = throw new UnsupportedOperationException()

}
