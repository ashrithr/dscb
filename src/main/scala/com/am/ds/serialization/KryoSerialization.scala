package com.am.ds.serialization

import java.io.{FileInputStream, FileOutputStream}

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy

/**
 * Simple serialization example using kryo
 * @author ashrith 
 */
object KryoSerialization {
  case class Shape(ct: String, color: String)

  def main(args: Array[String]) {
    val shapes = Array(Shape("Circle", "Blue"), Shape("Rectangle", "Green"))

    val kryo: Kryo = new Kryo()
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy)

    val output: Output = new Output(new FileOutputStream("/tmp/file.bin"))
    kryo.writeClassAndObject(output, shapes)
    output.close()

    val input: Input = new Input(new FileInputStream("/tmp/file.bin"))
    val shapes2 = kryo.readClassAndObject(input).asInstanceOf[Array[Shape]]
    shapes2.foreach(println)
    input.close()
  }
}
