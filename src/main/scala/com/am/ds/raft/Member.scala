package com.am.ds.raft


import com.am.ds.raft.rpc.Command
import com.am.ds.raft.util.Logging

import scala.concurrent.Future

/**
 * Description goes here
 * @author ashrith 
 */
abstract class Member(binding: String) extends Logging {
  def id = s"$binding"

  def forwardCommand[T](command: Command): Future[T]

  override def toString = id
}
