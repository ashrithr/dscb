package com.am.ds.akka.chatserver.CmdLineClient

import akka.actor.ActorRef

/**
 * Messages passed between actors
 * @author ashrith 
 */
object Messages {
  case object MainStart
  case object ConsoleListen
  case object ConsoleListeningForCommand
  case class Username(user: String)
  case class CreateMessage(command: String, username: String)
  case class UserLoggedIn(ar: ActorRef)
  case class UserLoggedOut(ar: ActorRef)
  case class MessageWithByteArray(msg: Array[Byte]) {
    private val message = msg
    def getArray = message
  }
  case class WriteToSocket(byteArray: Array[Byte])
  case class WaitForACK(byteArray: Array[Byte])
  case class ACK(byteArray: Array[Byte])
  case object ListenForChatMessages
}
