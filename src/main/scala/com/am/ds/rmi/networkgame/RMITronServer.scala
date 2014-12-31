package com.am.ds.rmi.networkgame

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject
import java.rmi.{Naming, RemoteException}

import scala.collection.mutable

/**
 * Server implementation
 * @author ashrith 
 */
object RMITronServer extends UnicastRemoteObject with TronServer {
  // immutable player to keep track of player (client) and player x, y position & direction headed
  private class Player(val client: TronClient, var x: Int, var y: Int, var direction: Int)

  private val clients = mutable.Buffer[Player]()

  def main (args: Array[String]) {
    LocateRegistry.createRegistry(1099)
    Naming.rebind("RMITronServer", this)
  }

  @throws(classOf[RemoteException])
  override def connect(client: TronClient): Int = {
    // synchronize because rmi happens in multi-threaded environment
    clients synchronized {
      if (clients.length < 2) {
        val initialX = if(clients.isEmpty) 100 else 400
        val initialY = 250
        val initialDirection = if(clients.isEmpty) 1 else 3
        clients += new Player(client, initialX, initialY, initialDirection)
        if (clients.length > 1) {
          println("Starting Game")
          new Thread {
            override def run: Unit = {
              startGame()
            }
          }.start()
        }
        clients.length - 1
      } else {
        -1
      }
    }
  }

  @throws(classOf[RemoteException])
  override def turnRight(player: Int): Unit = {
    /* Directions:

         0
       3   1
         2
     */
    clients(player).direction = (clients(player).direction + 3) % 4
  }

  @throws(classOf[RemoteException])
  override def turnLeft(player: Int): Unit = {
    clients(player).direction = (clients(player).direction + 1) % 4
  }


  // Check the player's direction and move accordingly
  private def move(p: Player): Unit = {
    p.direction match {
      case 0 => p.y -= 1
      case 1 => p.x += 1
      case 2 => p.y += 1
      case 3 => p.x -= 1
    }
  }

  private def startGame(): Unit = {
    // Countdown
    for (i <- 5 to 0 by -1) {
      clients.foreach(_.client.gameStart(i))
      Thread.sleep(1000)
    }
    var winner = -1
    val p1 = mutable.Buffer(clients(0).x -> clients(0).y)
    val p2 = mutable.Buffer(clients(1).x -> clients(1).y)
    val board = Array.fill(500, 500)(false)
    while (winner < 0) {
      /* game logic */
      // move players
      clients.foreach(move)
      // Send to clients
      p1 += clients(0).x -> clients(0).y
      p2 += clients(1).x -> clients(1).y
      // Check if dead
      if (clients(0).x < 0 || clients(0).x >= 500 || clients(0).y < 0 || clients(0).y >= 500 ||
        board(clients(0).x)(clients(0).y)) {
        winner = 1
      } else if (clients(1).x < 0 || clients(1).x >= 500 || clients(1).y < 0 ||
        clients(1).y >= 500 || board(clients(1).x)(clients(1).y)) {
        winner = 0
      }
      board(clients(0).x)(clients(0).y) = true
      board(clients(1).x)(clients(1).y) = true
      clients.foreach(_.client.stepTaken(p1, p2))
      // Pause
      Thread.sleep(50)
    }
    // tell all the clients game ended and who the winner was
    clients.foreach(_.client.gameEnd(winner))
  }
}
