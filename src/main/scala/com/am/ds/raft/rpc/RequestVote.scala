package com.am.ds.raft.rpc

/**
 * Description goes here
 * @author ashrith 
 */
case class RequestVote(memberId: String, term: Int, lastLogIndex: Long = -1, lastLogTerm: Int = -1) {
  override def toString: String = s"RequestVote($memberId,term=$term,lastLogIndex=$lastLogIndex,lastLogTerm=$lastLogTerm)"
}
