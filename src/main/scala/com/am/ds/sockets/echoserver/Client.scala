package com.am.ds.sockets.echoserver

import java.io.PrintStream
import java.net.{Socket, InetAddress}

import scala.io.BufferedSource

/**
 * Simple client to talk to echo server
 * @author ashrith 
 */
object Client extends App {
  val s = new Socket(InetAddress.getByName("localhost"), 9999)
  lazy val in = new BufferedSource(s.getInputStream).getLines()
  val out = new PrintStream(s.getOutputStream)

  out.println("hello")
  out.flush()
  println("Received " + in.next())
  s.close()
}
