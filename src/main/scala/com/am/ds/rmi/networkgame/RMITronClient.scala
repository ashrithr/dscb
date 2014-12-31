package com.am.ds.rmi.networkgame

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Graphics2D}
import java.rmi.server.UnicastRemoteObject
import java.rmi.{Naming, RemoteException}

import scala.swing.event._
import scala.swing.{MainFrame, Panel}

/**
 * Description goes here
 * @author ashrith 
 */
object RMITronClient extends UnicastRemoteObject with TronClient {

  val server = Naming.lookup("rmi://localhost/RMITronServer") match {
    case s: TronServer => s
    case _ => throw new RuntimeException("Invalid type for RMI Server.")
  }

  val playerNumber = server.connect(this)
  var message = ""
  val img: BufferedImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB)

  val panel = new Panel {
    override def paint(g: Graphics2D): Unit = {
      g.drawImage(img, 0, 0, null)
      g.setPaint(Color.white)
      g.drawString(message, 250, 250)
    }
    preferredSize = new Dimension(img.getWidth, img.getHeight)
    listenTo(mouse.clicks, keys)
    reactions += {
      case e: MouseClicked => requestFocus()
      case e: MouseEntered => requestFocus()
      case e: KeyPressed =>
        e.key match {
          case Key.Left => server.turnLeft(playerNumber)
          case Key.Right => server.turnRight(playerNumber)
        }
    }
  }

  val frame = new MainFrame {
    title = "Net Tron"
    contents = panel
    centerOnScreen()
  }

  def main(args: Array[String]) {
    frame.open()
    panel.requestFocus()
  }

  @throws(classOf[RemoteException])
  override def gameStart(countDown: Int): Unit = {
    message = countDown.toString
    if (panel != null) panel.repaint()
  }

  @throws(classOf[RemoteException])
  override def gameEnd(winner: Int): Unit = {
    message = if (winner == playerNumber) "You Won!" else "You Lost."
    if (panel != null) panel.repaint()
  }

  @throws(classOf[RemoteException])
  override def stepTaken(p1: Seq[(Int, Int)], p2: Seq[(Int, Int)]): Unit = {
    message = "" // reset the message to empty string so that
    for((x, y) <- p1) img.setRGB(x, y, Color.red.getRGB)
    for((x, y) <- p2) img.setRGB(x, y, Color.blue.getRGB)
    panel.repaint()
  }
}
