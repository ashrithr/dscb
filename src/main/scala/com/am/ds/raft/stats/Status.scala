package com.am.ds.raft.stats

import com.am.ds.raft.rpc.LogEntry


/**
 * Description goes here
 * @author ashrith 
 */
case class Status(cluster: ClusterStatus, log: LogStatus)

case class ClusterStatus(term: Int, state: String, stateInfo: StateInfo)

case class LogStatus(length: Long, commitIndex: Long, lastEntry: Option[LogEntry])
