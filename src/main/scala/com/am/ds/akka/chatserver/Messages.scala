package com.am.ds.akka.chatserver

import java.io.DataOutputStream
import java.net.Socket

/**
 * Messages passed between actors
 * @author ashrith 
 */
object Messages {
  case class CreateActor(socket: Socket)
  case class Parse(cmd: Byte, msg: Array[Byte])
  case object UserExists
  case class UserCreated(name: String)
  case class Login(name: String)
  case class Logout(name: String, out: DataOutputStream)
  case object Listen
  case class ForwardAll(from: String, bytes: Array[Byte])
  case class Message(bytes: Array[Byte])
}
