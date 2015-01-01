package com.am.ds.raft.exception

import com.am.ds.raft.rpc.LogEntry

/**
 * Description goes here
 * @author ashrith
 */
class WriteTimeoutException(logEntry: LogEntry) extends RuntimeException(s"$logEntry")
