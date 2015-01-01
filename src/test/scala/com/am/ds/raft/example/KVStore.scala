package com.am.ds.raft.example

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

import com.am.ds.raft.statemachine.StateMachine
import com.am.ds.raft.util.{Serializer, Logging}

/**
 * Description goes here
 * @author ashrith 
 */
class KVStore extends StateMachine with Logging {

  val map = new ConcurrentHashMap[String, String]()
  @volatile
  var lastIndex: Long = 0

  def applyWrite = {
    case (index, Put(key: String, value: String)) => {
      LOG.debug(s"Put $key=$value")
      map.put(key, value);
      lastIndex = index
      value
    }
  }

  def applyRead = {
    case Get(key) => {
      LOG.debug(s"Get $key")
      map.get(key)
    }
  }

  def lastAppliedIndex: Long = lastIndex

  def deserialize(byteBuffer: ByteBuffer) = {
    val snapshotBytes = byteBuffer.array()
    val deserializedMap: ConcurrentHashMap[String, String] = Serializer.deserialize[ConcurrentHashMap[String, String]](snapshotBytes)
    map.clear()
    map.putAll(deserializedMap)
  }

  def serialize(): ByteBuffer = ByteBuffer.wrap(Serializer.serialize(map))

}