package com.am.ds.raft.example

import com.am.ds.raft.rpc.ReadCommand

/**
 * Description goes here
 * @author ashrith 
 */
case class Get(key: String) extends ReadCommand[String]