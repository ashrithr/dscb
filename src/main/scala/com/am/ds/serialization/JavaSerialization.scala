package com.am.ds.serialization

import java.io._

/**
 * Simple serialization example writing to a file and reading back
 * @author ashrith 
 */
object JavaSerialization {
  // case classes are by default serializable, if dealing with other classes just extend the class
  // with `Serializable` interface
  case class Shape(ct: String, color: String)

  def main (args: Array[String]) {
    val shapes = Array(
      Shape("Circle", "Blue"),
      Shape("Rectangle", "Green")
    )

    val oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("/tmp/shapes.bin")))
    oos.writeObject(shapes)
    oos.close()

    val ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream("/tmp/shapes.bin")))
    // shapes2 is an object, so lets pattern match for array of type shapes
    val shapes2 = ois.readObject match {
      case arr: Array[Shape] => arr
      case _ => throw new Exception("Value read was not an array")
    }
    shapes2.foreach(println)
    ois.close()
  }
}
