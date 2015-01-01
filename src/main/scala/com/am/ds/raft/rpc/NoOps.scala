package com.am.ds.raft.rpc

/**
 * Description goes here
 * @author ashrith 
 */
case class NoOp extends WriteCommand[Unit]

case object Void

case class CompactedEntry() extends ReadCommand[Unit]