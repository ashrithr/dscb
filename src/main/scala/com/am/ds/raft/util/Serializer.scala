package com.am.ds.raft.util

import java.io.{InputStream, ByteArrayOutputStream}
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{UnsafeInput, UnsafeOutput}
import com.esotericsoftware.shaded.org.objenesis.strategy.StdInstantiatorStrategy

/**
 * Wrapper class to serialize and deserialize objects using KRYO
 * @author ashrith 
 */
object Serializer {
  val kryoSerializer = new KryoSerializer()

  def serialize[T](anObject: T): Array[Byte] = kryoSerializer.serialize(anObject)

  def deserialize[T](bytes: Array[Byte]): T = kryoSerializer.deserialize(bytes)
}

trait Factory[T] {
  def newInstance(): T
}

class KryoPool(factory: Factory[Kryo], initInstances: Int) {
  val instances = new AtomicLong(initInstances)
  val maxInstances = initInstances * 2
  val objects = new ConcurrentLinkedQueue[Kryo]()

  (1 to initInstances) foreach { _ => objects.offer(factory.newInstance()) }

  def take(): Kryo = {
    val pooledKryo = objects.poll()
    if (pooledKryo == null)
      return factory.newInstance()
    instances.decrementAndGet()
    pooledKryo
  }

  def release(kh: Kryo) = {
    if (instances.intValue() < maxInstances) {
      instances.incrementAndGet()
      objects.offer(kh)
    }
  }

  def close() = {
    objects.clear()
  }
}

class KryoSerializer(hintedClasses: List[Class[_]] = List.empty) extends Logging {
  val kryoFactory = new Factory[Kryo] {
    override def newInstance(): Kryo = {
      val kryo = new Kryo()
      kryo.setReferences(false)
      kryo.setInstantiatorStrategy(new StdInstantiatorStrategy)
      hintedClasses.foreach(hintedClass => kryo.register(hintedClass))
      kryo
    }
  }

  val pool = new KryoPool(kryoFactory, 10)

  def serialize[T](anObject: T): Array[Byte] = {
    val kryo = pool.take()
    try {
      val outputStream = new ByteArrayOutputStream()
      val output = new UnsafeOutput(outputStream)
      kryo.writeClassAndObject(output, anObject)
      output.flush()
      output.close()
      val bytes = outputStream.toByteArray
      bytes
    } catch {
      case e: Exception => LOG.trace("serialization error", e); throw e
    } finally {
      pool.release(kryo)
    }
  }

  def deserialize[T](bytes: Array[Byte]): T = {
    deserialize(new UnsafeInput(bytes))
  }

  def deserialize[T](inputStream: InputStream): T = {
    deserialize(new UnsafeInput(inputStream))
  }

  def deserialize[T](input: UnsafeInput): T = {
    val kryo = pool.take()
    try {
      kryo.readClassAndObject(input).asInstanceOf[T]
    } catch {
      case e: Exception => LOG.trace("deserialization error", e); throw e
    } finally {
      pool.release(kryo)
    }
  }
}

class KryoSerializerFactory {
  def create(hintedClasses: List[Class[_]]) = {
    new KryoSerializer(hintedClasses)
  }
}
