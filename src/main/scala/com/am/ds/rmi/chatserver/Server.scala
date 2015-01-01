package com.am.ds.rmi.chatserver

import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject
import java.rmi.{Naming, RemoteException, Remote}

/**
 * Description goes here
 * @author ashrith 
 */
trait RMIServer extends Remote {
  @throws(classOf[RemoteException]) def connect(client: RMIClient): String
  @throws(classOf[RemoteException]) def disconnect(client: RMIClient): Unit
  @throws(classOf[RemoteException]) def getClients: Seq[RMIClient]
  @throws(classOf[RemoteException]) def publicMessage(client: RMIClient, text: String): Unit
}

class RMIServerImpl extends UnicastRemoteObject with RMIServer {
  private val clients = collection.mutable.Buffer[RMIClient]()
  private var history = collection.mutable.ListBuffer("Server Started\n")

  @throws(classOf[RemoteException])
  override def connect(client: RMIClient): String = {
    clients += client
    sendUpdate
    history.mkString("\n") + "\n"
  }

  @throws(classOf[RemoteException])
  override def getClients: Seq[RMIClient] = clients

  @throws(classOf[RemoteException])
  override def disconnect(client: RMIClient): Unit = {
    clients -= client
    sendUpdate
  }

  @throws(classOf[RemoteException])
  override def publicMessage(client: RMIClient, text: String): Unit = {
    history += client.name + " : " + text
    if (history.length > 10) history.remove(0)
  }

  private def sendUpdate: Unit = {
    val deadClients = clients.filter(c =>
      try {
        c.name
        false
      } catch {
        case ex: RemoteException => true
      }
    )
  }
}

object Server {
  def main(args: Array[String]) {
    val server = new RMIServerImpl
    System.setProperty("java.rmi.server.hostname", "localhost")
    LocateRegistry.createRegistry(1099)
    Naming.rebind("ChatServer", server)
    println("Server running on port 1099 ...")
  }
}
