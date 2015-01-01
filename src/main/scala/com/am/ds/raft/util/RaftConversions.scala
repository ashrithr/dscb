package com.am.ds.raft.util

import java.util.concurrent.Callable

/**
 * Handy conversions to convert function to runnable and callable in the scope
 * @author ashrith
 */
object RaftConversions {
  implicit def fromFunctionToRunnable(f: () => Any): Runnable = new Runnable {
    override def run(): Unit = f()
  }

  implicit def fromFunctionToCallable[V](f: () => V): Callable[V] = new Callable[V] {
    override def call(): V = f()
  }
}
