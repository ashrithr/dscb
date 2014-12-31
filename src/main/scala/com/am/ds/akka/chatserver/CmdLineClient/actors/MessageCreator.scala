package com.am.ds.akka.chatserver.CmdLineClient.actors

import akka.actor.{ActorLogging, Actor}
import com.am.ds.akka.chatserver.CmdLineClient.Messages._
import com.am.ds.akka.chatserver.Parsing._

/**
 * Description goes here
 * @author ashrith 
 */
class MessageCreator extends Actor with ActorLogging {

  val commandCodes = Map(
    "login" -> 1.toByte,
    "send" -> 3.toByte,
    "logout" -> 4.toByte
  )

  def receive: Receive = {

    case CreateMessage(command, message) =>
      log.info(s"got CreateMessage($command, $message)")

      // find socket writer
      val socketWriter = context.system.actorSelection("user/mainActor/socketWriter")
      // create byte message
      val msgByteArray =
        Array(commandCodes(command)) ++ intToByteArray(message.length) ++ message.getBytes("UTF-8")
      // send message to socket writer
      socketWriter ! MessageWithByteArray(msgByteArray)

    case m =>
      log.info(s"got unknown message $m")
  }

}