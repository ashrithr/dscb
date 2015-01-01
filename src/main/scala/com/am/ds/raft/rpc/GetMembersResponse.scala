package com.am.ds.raft.rpc

/**
 * Description goes here
 * @author ashrith 
 */
case class GetMembersResponse(success: Boolean, members: Seq[String])