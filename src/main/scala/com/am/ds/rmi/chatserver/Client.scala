package com.am.ds.rmi.chatserver

import java.rmi.server.UnicastRemoteObject
import java.rmi.{Naming, RemoteException, Remote}
import scala.swing._

/**
 * Description goes here
 * @author ashrith 
 */
trait RMIClient extends Remote {
  @throws(classOf[RemoteException]) def name: String
  @throws(classOf[RemoteException]) def message(sender: RMIClient, text: String): Unit
  @throws(classOf[RemoteException]) def clientUpdate(clients: Seq[RMIClient]): Unit
}

class RMIClientImpl(myName: String, server: RMIServer) extends UnicastRemoteObject with RMIClient {
  private val chatText = new TextArea(server.connect(this))
  chatText.editable = false
  private var clients = server.getClients
  private val userList = new ListView(clients.map(_.name))

  private val chatField = new TextField("") {
    listenTo(this)
    reactions += {
      case e: swing.event.EditDone => if (text.trim.nonEmpty) {
        val recipients = if (userList.selection.items.isEmpty) {
          server.publicMessage(RMIClientImpl.this, text)
          clients
        } else {
          userList.selection.indices.map(clients).toSeq
        }
        recipients.foreach(r => try {
          r.message(RMIClientImpl.this, text)
        } catch {
          case ex: RemoteException => chatText.append("Couldn't send to one recipient.")
        })
        text = ""
      }
    }
  }

  private val frame = new MainFrame {
    title = "Chat"
    contents = new BorderPanel {
      val scrollList = new ScrollPane(userList)
      scrollList.preferredSize = new Dimension(200, 500)
      layout += scrollList -> BorderPanel.Position.West
      layout += new BorderPanel {
        val scrollChat = new ScrollPane(chatText)
        scrollChat.preferredSize = new Dimension(500, 200)
        layout += scrollChat -> BorderPanel.Position.Center
        layout += chatField -> BorderPanel.Position.South
      } -> BorderPanel.Position.Center
    }
    listenTo(this)
    reactions += {
      case e: event.WindowClosing => server.disconnect(RMIClientImpl.this)
      case _ =>
    }
  }

  frame.visible = true

  @throws(classOf[RemoteException])
  override def name: String = myName

  @throws(classOf[RemoteException])
  override def message(sender: RMIClient, text: String): Unit = {
    chatText.append(sender.name + " : " + text + "\n")
  }

  @throws(classOf[RemoteException])
  override def clientUpdate(cls: Seq[RMIClient]): Unit = {
    clients = cls
    if (userList != null)
      userList.listData = cls.map(c =>
        try {
          c.name
        } catch {
          case ex: RemoteException => "Error"
        }
      )
  }
}

object Client {
  def main(args: Array[String]): Unit = {
    //    val mName = Dialog.showInput(null, "What server do you want to connect to ?",
    //      "Server Name", Dialog.Message.Question, null, Nil, "")
    //    mName match {
    //      case Some(machineName) =>
    Naming.lookup("rmi://localhost/ChatServer") match {
      case server: RMIServer =>
        val name = Dialog.showInput(null, "What name do you want to go by ?",
          "User Name", Dialog.Message.Question, null, Nil, "")
        if (name.nonEmpty)
          new RMIClientImpl(name.get, server)
      case _ => println("That machine does not have a registered server.")
    }
    //      case None =>
    //    }
  }
}
