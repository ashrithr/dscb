package com.am.ds.logicalclocks

import java.util.concurrent.{TimeUnit, Executors, CountDownLatch}
import java.util.concurrent.atomic.AtomicLong

/**
 * Lamport clock implementation from coda hale
 */
object LamportClock {
  case class Timestamp(time: Long) extends Ordered[Timestamp] {
    override def compare(that: Timestamp) = time.compare(that.time)
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
   * A strictly-increasing clock. Guaranteed to return a value greater than its
   * last return value.
   */
  trait LamportClock extends Clock {
    private val counter = new AtomicLong(System.currentTimeMillis)

    def timestamp: Timestamp = {
      var newTime: Long = 0
      while(newTime == 0) {
        val last = counter.get
        val current = System.currentTimeMillis
        val next = if (current > last) current else last + 1
        // Atomically sets the value to the given updated value if the current value is equal (==)
        // to the expected value.
        if (counter.compareAndSet(last, next)) {
          newTime = next
        }
      }
      Timestamp(newTime)
    }
  }

  /**
   * Test lamport clock implementation
   */
  def main(args: Array[String]) {
    object ConcreteLamportClock extends LamportClock

    /*
      With a CountDownLatch you can specify a number and then count down by 1 once an operation has
      completed. If all operations have completed and the count is 0, another thread that uses the
      same CountDownLatch as a synchronisation tool using the await method can do it’s work.
     */
    val startGate = new CountDownLatch(1)
    val pool = Executors.newFixedThreadPool(20)
    // submit 40 tasks
    for (i <- 0 to 40) {
      pool.submit(new Runnable {
        override def run() = {
          var timestamps = List[Timestamp]()
          /*
            The first interaction with CountDownLatch is with main thread which is going to wait
            for other threads. This main thread must call, CountDownLatch.await() method immediately
            after starting other threads. The execution will stop on await() method till the time,
            other threads complete their execution.
           */
          startGate.await()
          for (j <- 0 to 1000) {
            timestamps = ConcreteLamportClock.timestamp :: timestamps
          }
          // check if the timestamps are strictly incrementing
          assert(timestamps.sortWith(_ > _) == timestamps)
        }
      })
    }
    startGate.countDown() // start all threads
    pool.shutdown()
    pool.awaitTermination(30, TimeUnit.SECONDS)
  }
}
