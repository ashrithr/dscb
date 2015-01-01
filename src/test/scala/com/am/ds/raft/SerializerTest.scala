package com.am.ds.raft

import com.am.ds.raft.rpc.{NoOp, LogEntry}
import com.am.ds.raft.util.{Serializer, Logging}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._


/**
 * Description goes here
 * @author ashrith 
 */
@RunWith(classOf[JUnitRunner])
class SerializerTest extends FlatSpec with Matchers with Logging {

  "a serializer" should "serialize and deserialize" in {
    val logEntry = LogEntry(1,1,NoOp())

    val bytes = Serializer.serialize(logEntry)

    val deserialized:LogEntry = Serializer.deserialize(bytes)
  }
}
