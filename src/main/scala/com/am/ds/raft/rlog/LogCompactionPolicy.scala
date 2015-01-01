package com.am.ds.raft.rlog

import com.am.ds.raft.statemachine.StateMachine

/**
 * Description goes here
 * @author ashrith 
 */
trait LogCompactionPolicy {

  def applies(persistentLog: PersistentLog, stateMachine: StateMachine): Boolean

}