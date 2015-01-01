package com.am.ds.paxos

import akka.actor.Actor
import akka.event.Logging

/**
 * Description goes here
 * @author ashrith
 */
class Learner extends Actor {
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case Learn(value: Int) => {
      log.info(s"Learned a value! $value")
    }
  }
}
