package com.am.ds.raft.util

import org.slf4j.LoggerFactory

/**
 * Description goes here
 * @author ashrith 
 */
trait Logging {
  val LOG = LoggerFactory.getLogger(this.getClass)

  def loggingErrors[T](f: => T) = {
    try {
      f
    } catch {
      case e: Exception => LOG.error("Error", e)
        throw e
    }
  }
}
