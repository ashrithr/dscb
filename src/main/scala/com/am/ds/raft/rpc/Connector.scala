package com.am.ds.raft.rpc

import com.am.ds.raft.rlog.Snapshot

import scala.concurrent.Future

/**
 * Description goes here
 * @author ashrith 
 */
trait Connector {
  def send(request: RequestVote): Future[RequestVoteResponse]

  def send(appendEntries: AppendEntries): Future[AppendEntriesResponse]

  def send(snapshot: Snapshot): Future[Boolean]

  def send[T](command: Command): Future[T]

  def send(joinRequest: JoinRequest): Future[JoinResponse]

  def send(getMembersRequest: GetMembersRequest): Future[GetMembersResponse]
}
