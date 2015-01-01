package com.am.ds.raft.rpc

/**
 * Description goes here
 * @author ashrith 
 */
case class RequestVoteResponse(currentTerm: Int, granted: Boolean)