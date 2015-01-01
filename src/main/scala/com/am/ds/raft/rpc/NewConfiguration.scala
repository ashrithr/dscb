package com.am.ds.raft.rpc

import com.esotericsoftware.kryo.{KryoSerializable, Kryo}
import com.esotericsoftware.kryo.io.{Input, Output}

/**
 * Description goes here
 * @author ashrith 
 */
case class NewConfiguration (var bindings: List[String]) extends WriteCommand[Boolean] with KryoSerializable with ClusterConfigurationCommand {
  def write(kryo: Kryo, output: Output) = {
    output.writeString(bindings.mkString(","))
  }

  def read(kryo: Kryo, input: Input) = {
    bindings = input.readString().split(",").toList
  }
}