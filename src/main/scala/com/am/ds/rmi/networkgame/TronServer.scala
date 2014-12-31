package com.am.ds.rmi.networkgame

import java.rmi.{RemoteException, Remote}

/**
 * Remote calls available on server
 * @author ashrith 
 */
trait TronServer extends Remote {
  @throws(classOf[RemoteException]) def connect(client: TronClient): Int
  @throws(classOf[RemoteException]) def turnLeft(player: Int): Unit
  @throws(classOf[RemoteException]) def turnRight(player: Int): Unit
}
