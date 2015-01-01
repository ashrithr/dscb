package com.am.ds.raft.example

import com.am.ds.raft.rpc.WriteCommand

/**
 * Description goes here
 * @author ashrith 
 */
case class Put(key: String, value: String) extends WriteCommand[String]