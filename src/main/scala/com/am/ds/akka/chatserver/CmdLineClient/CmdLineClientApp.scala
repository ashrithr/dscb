package com.am.ds.akka.chatserver.CmdLineClient

import java.io._
import java.net.Socket

import akka.actor.{Props, ActorSystem}

import com.am.ds.akka.chatserver.CmdLineClient.Messages._
import com.am.ds.akka.chatserver.CmdLineClient.actors.MainActor

/**
 * Command line client interface
 * @author ashrith 
 */
object CmdLineClientApp {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("Usage: java CmdLineClientApp [hostname] [port]")
      System.exit(1)
    }

    val host = args(0)
    val port = args(1).toInt
    val socket = new Socket(host, port)

    val out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream))
    val in = new DataInputStream(new BufferedInputStream(socket.getInputStream))
    val stdIn = new BufferedReader(new InputStreamReader(System.in))

    val system = ActorSystem("system")
    val mainActor = system.actorOf(Props(new MainActor(in, out, stdIn)), name = "mainActor")

    mainActor ! MainStart
  }
}
