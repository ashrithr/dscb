package com.am.ds.rmi.networkgame

import java.rmi.{Remote, RemoteException}

/**
 * Remote calls available on clients
 * @author ashrith 
 */
trait TronClient extends Remote {
  // countdown displayed to client before joining the game
  @throws(classOf[RemoteException])
  def gameStart(countDown: Int): Unit

  @throws(classOf[RemoteException])
  def gameEnd(winner: Int): Unit

  @throws(classOf[RemoteException])
  def stepTaken(p1: Seq[(Int, Int)], p2: Seq[(Int, Int)]): Unit
}
