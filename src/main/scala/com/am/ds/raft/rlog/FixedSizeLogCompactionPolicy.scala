package com.am.ds.raft.rlog

import com.am.ds.raft.statemachine.StateMachine

/**
 * Description goes here
 * @author ashrith 
 */
class FixedSizeLogCompactionPolicy(fixedSize: Long) extends LogCompactionPolicy {

  def applies(persistentLog: PersistentLog, stateMachine: StateMachine) = persistentLog.size >= fixedSize

}
