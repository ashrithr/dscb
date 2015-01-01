package com.am.ds.raft

import java.util.concurrent.atomic.AtomicBoolean

import com.am.ds.raft.rpc.{ReadCommand, WriteCommand}
import com.am.ds.raft.rpc.thrift.ThriftServer

import scala.concurrent.Future

/**
 * Description goes here
 * @author ashrith 
 */
class Raft(private[raft] val cluster: Cluster, private[raft] val builder: RaftBuilder) {

  private val thrift = ThriftServer(cluster)
  private val stopped = new AtomicBoolean(false)

  def write[T](writeCommand: WriteCommand[T]): Future[T] = cluster.on[T](writeCommand)

  def read[T](readCommand: ReadCommand[T]): Future[T] = cluster.on[T](readCommand)

  def readLocal[T](readCommand: ReadCommand[T]): T = cluster.onLocal(readCommand)

  def addMember(memberBinding: String) = cluster.addMember(memberBinding)

  def removeMember(memberBinding: String) = cluster.removeMember(memberBinding)

  def isLeader: Boolean = cluster.isLeader

  def getMembers: List[String] = cluster.getMembers()

  def status = cluster.getStatus

  def start = {
    thrift start()

    cluster start
  }

  def stop() = {
    if (!stopped.getAndSet(true)) {
      thrift stop()

      cluster stop
    }
  }
}