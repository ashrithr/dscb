package com.am.ds.logicalclocks

import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong
import scala.collection.mutable.{Map => MMap}

/**
 * Vector Clock Implementation from coda hale
 */
object VectorClock {
  case class Timestamp(time: Long) extends Ordered[Timestamp] {
    def compare(that: Timestamp) = time.compare(that.time)
    def max(that: Timestamp) = if (this < that) that else this
    override def toString = "%016x".format(time)
  }

  object Timestamp {
    def apply(): Timestamp = Timestamp(0)
  }

  /**
   * An abstract trait defining a class or object capable of returning a
   * timestamp.
   */
  trait Clock {
    def timestamp: Timestamp
  }

  /**
   * Hash representation of a versioned node name.
   */
  case class Node private(id: String, name: String) {
    override def toString = name.mkString("Node(", "", ")")
  }

  object Node {
    def apply(name: String): Node = {
      Node(hash(name), name)
    }

    private def hash(name: String): String = {
      val md5 = MessageDigest.getInstance("MD5")
      md5.update(name.getBytes)
      md5.digest().map { b => "%02x".format(0xFF & b) }.mkString
    }
  }

  trait LamportClock extends Clock {
    private val counter = new AtomicLong(System.currentTimeMillis)

    def timestamp: Timestamp = {
      var newTime: Long = 0
      while(newTime == 0) {
        val last = counter.get
        val current = System.currentTimeMillis
        val next = if (current > last) current else last + 1
        if (counter.compareAndSet(last, next)) {
          newTime = next
        }
      }
      Timestamp(newTime)
    }
  }

  case class VectorClock(clock: Clock, versions: Map[Node, Timestamp])
    extends PartiallyOrdered[VectorClock] {

    def tryCompareTo [B >: VectorClock](b: B)(implicit ev1: B => PartiallyOrdered[B]): Option[Int] = {
      b match {
        case VectorClock(`clock`, that) =>
          if (lessThan(versions, that))      Some(-1)
          else if (lessThan(that, versions)) Some( 1)
          else if (versions == that)         Some( 0)
          else                               None
        case _ => None
      }
    }

    def ==(that: VectorClock) = versions == that.versions

    /**
     * Returns true if <code>this</code> and <code>that</code> are concurrent.
     */
    def <>(that: VectorClock) = tryCompareTo(that) == None

    def +(node: Node) = VectorClock(clock, versions + (node -> clock.timestamp))

    def merge(that: VectorClock): VectorClock = VectorClock(clock, merge(versions, that.versions))

    private def merge(a: Map[Node, Timestamp], b: Map[Node, Timestamp]): Map[Node, Timestamp] = {
      val c = MMap[Node, Timestamp]() ++ b
      for ((n, t) <- a) {
        c(n) = t.max(c.getOrElse(n, t))
      }
      Map() ++ c
    }

    // A is less than B if and only if A[z] <= B[z] for all instances z and there
    // exists an index z' such that A[z'] < B[z']
    private def lessThan(a: Map[Node, Timestamp], b: Map[Node, Timestamp]): Boolean = {
      a.forall { case ((n, t)) => t <= b.getOrElse(n, Timestamp()) } &&
        (a.exists { case ((n, t)) => t < b.getOrElse(n, Timestamp()) } || (a.size < b.size))
    }

    override def toString: String = {
      versions.map { case ((x, y)) => x.name + " -> " + y }.mkString("VectorClock(", ", ", ")")
    }
  }

  object VectorClock extends LamportClock {
    def apply(): VectorClock = VectorClock(this, Map())
  }

  def main(args: Array[String]) {
    val server1 = Node("server 1")
    val server2 = Node("server 2")
    val clock = VectorClock()
    println("Clock: " + clock.clock.timestamp)

    // update the clock from server1
    val change1 = clock + server1

    println(change1 > clock)
    println(change1 < clock)

    // update the clock from server2
    val change2 = clock + server2

    println(change2 > clock)
    println(change2 < clock)

    // merge two clocks
    val reconciled = change1.merge(change2)
    println(change1 < reconciled)
    println(change2 < reconciled)
  }
}
