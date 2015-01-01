package com.am.ds.raft.statemachine.j

import java.nio.ByteBuffer

import com.am.ds.raft.rpc.{ReadCommand, WriteCommand}

/**
 * Description goes here
 * @author ashrith 
 */
trait StateMachine {

  def deserialize(byteBuffer: ByteBuffer)

  def serialize(): ByteBuffer

  def applyWrite(index:Long, write: WriteCommand[_]):Any

  def applyRead(read: ReadCommand[_]):Any

  def lastAppliedIndex: Long

}