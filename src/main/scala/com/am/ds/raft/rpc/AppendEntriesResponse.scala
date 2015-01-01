package com.am.ds.raft.rpc

/**
 * Description goes here
 * @author ashrith 
 */
case class AppendEntriesResponse(term: Int, success: Boolean)