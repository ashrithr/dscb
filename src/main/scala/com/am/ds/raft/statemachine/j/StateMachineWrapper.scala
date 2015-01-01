package com.am.ds.raft.statemachine.j

import java.nio.ByteBuffer

import com.am.ds.raft.rpc.{ReadCommand, WriteCommand}

/**
 * Description goes here
 * @author ashrith 
 */
class StateMachineWrapper(jstateMachine: StateMachine) extends com.am.ds.raft.statemachine.StateMachine {

  def deserialize(byteBuffer: ByteBuffer) = jstateMachine.deserialize(byteBuffer)

  def serialize(): ByteBuffer = jstateMachine.serialize

  def applyWrite: PartialFunction[(Long, WriteCommand[_]),Any] = {
    case (index, write) => jstateMachine.applyWrite(index, write)
  }

  def applyRead: PartialFunction[ReadCommand[_],Any] = {
    case read => jstateMachine.applyRead(read)
  }

  def lastAppliedIndex: Long = jstateMachine.lastAppliedIndex
}