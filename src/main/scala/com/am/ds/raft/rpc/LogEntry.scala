package com.am.ds.raft.rpc

/**
 * Description goes here
 * @author ashrith 
 */
case class LogEntry(term: Int, index: Long, command: Command) {
  override def toString = s"LogEntry(term=$term, index=$index, $command)"
}
