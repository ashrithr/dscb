package com.am.ds.raft.statemachine

import com.am.ds.raft.rpc.{ReadCommand, WriteCommand}
import com.am.ds.raft.util.Logging

/**
 * Description goes here
 * @author ashrith 
 */
class CommandExecutor(stateMachine: StateMachine) extends Logging {

  val writeFunction = stateMachine.applyWrite
  val readFunction = stateMachine.applyRead

  def applyWrite[T](index: Long, write: WriteCommand[T]): T = {
    val params = (index, write)
    if (writeFunction.isDefinedAt(params)) writeFunction(params).asInstanceOf[T]
    else {
      LOG.warn(s"No handler for ${write} is available in the StateMachine")
      throw new IllegalStateException(s"No handler for ${write}")
    }
  }

  def applyRead[T](read: ReadCommand[T]): T = {
    if (readFunction.isDefinedAt(read)) readFunction(read).asInstanceOf[T]
    else {
      LOG.warn(s"No handler for ${read} is available in the StateMachine")
      throw new IllegalStateException(s"No handler for ${read}")
    }
  }

}