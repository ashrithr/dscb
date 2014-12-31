package com.am.ds.sockets.echoserver

import java.io.PrintStream
import java.net.ServerSocket

import scala.io.BufferedSource

/**
 * Simple echo server using Socket's
 * @author ashrith 
 */
object Server extends App {
  val server = new ServerSocket(9999)
  while (true) {
    // A call to the `accept` will block until a connection is made. `accept` method
    // returns an instance of `Socket`.
    val s = server.accept()
    /*
      Streams from Sockets:
        To send & receive data using plain Socket class for TCP we use streams, the
        `Socket` class provides following methods:
        * `getInputStream(): InputStream`
        * `getOutputStream(): OutputStream`
     */
    val in = new BufferedSource(s.getInputStream).getLines()
    val out = new PrintStream(s.getOutputStream)

    printMessage(in.next(), out)
    s.close()
  }

  def printMessage(message: String, out: PrintStream) = message match {
    case "hello" => out.println("Got hello. Hello yourself!"); out.flush()
    case _ => out.println("I don't know what you are talking about ?"); out.flush()
  }
}
