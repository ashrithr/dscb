package com.am.ds.raft

import com.am.ds.raft.statemachine.StateMachine
import com.am.ds.raft.statemachine.j.StateMachineWrapper
import com.typesafe.config.ConfigFactory

/**
 * Description goes here
 * @author ashrith 
 */
class RaftBuilder {

  private val configuration = new Configuration(ConfigFactory.load())
  private var stateMachine: StateMachine = _

  def minElectionTimeout(minElectionTimeout: Int): RaftBuilder = {
    configuration.withMinElectionTimeout(minElectionTimeout)
    RaftBuilder.this
  }

  def maxElectionTimeout(maxElectionTimeout: Int): RaftBuilder = {
    configuration.withMaxElectionTimeout(maxElectionTimeout)
    RaftBuilder.this
  }

  def heartbeatsPeriod(heartbeatsInterval: Int): RaftBuilder = {
    configuration.withHeartbeatsInterval(heartbeatsInterval)
    RaftBuilder.this
  }

  def listenAddress(localBinding: String): RaftBuilder = {
    configuration.withLocalBinding(localBinding)
    RaftBuilder.this
  }

  def dataDir(dataDir: String): RaftBuilder = {
    configuration.withDataDir(dataDir)
    RaftBuilder.this
  }

  def members(memberBindings: Seq[String]): RaftBuilder = {
    configuration.withMemberBindings(memberBindings)
    RaftBuilder.this
  }

  def members(memberBindings: String): RaftBuilder = {
    configuration.withMemberBindings(memberBindings.split(","))
    RaftBuilder.this
  }

  def compactionThreshold(threshold: Int): RaftBuilder = {
    configuration.withLogCompactionThreshold(threshold)
    RaftBuilder.this
  }

  def stateMachine(stateMachine: StateMachine): RaftBuilder = {
    RaftBuilder.this.stateMachine = stateMachine
    RaftBuilder.this
  }

  def stateMachine(stateMachine: com.am.ds.raft.statemachine.j.StateMachine): RaftBuilder = {
    RaftBuilder.this.stateMachine = new StateMachineWrapper(stateMachine)
    RaftBuilder.this
  }

  def flushSize(flushSize: Long): RaftBuilder = {
    configuration.withFlushSize(flushSize)
    RaftBuilder.this
  }

  def sync(enabled: Boolean):RaftBuilder = {
    configuration.withSyncEnabled(enabled)
    RaftBuilder.this
  }

  def bootstrap(enabled: Boolean): RaftBuilder = {
    configuration.bootstrap(enabled)
    RaftBuilder.this
  }

  def build(): Raft = {
    new Raft(new Cluster(stateMachine, configuration), RaftBuilder.this)
  }

}

object RaftBuilder {

  def apply() = {
    new RaftBuilder()
  }

}