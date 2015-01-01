package com.am.ds.raft.rlog

import com.am.ds.raft.rpc.LogEntry

/**
 * Description goes here
 * @author ashrith 
 */
trait PersistentLog {

  def append(entry: LogEntry): Unit
  def rollLog(upToIndex: Long)
  def commit
  def getEntry(index: Long): LogEntry
  def getLastIndex: Long
  def discardEntriesFrom(index: Long)
  //  def discardEntriesUntil(index: Long)
  //  def remove(index: Long)
  def size: Long
  def close

}