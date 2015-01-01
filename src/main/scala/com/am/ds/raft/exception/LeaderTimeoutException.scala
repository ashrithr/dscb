package com.am.ds.raft.exception

import java.util.concurrent.TimeoutException

/**
 * Description goes here
 * @author ashrith
 */
class LeaderTimeoutException(exception: TimeoutException) extends RuntimeException(exception)
