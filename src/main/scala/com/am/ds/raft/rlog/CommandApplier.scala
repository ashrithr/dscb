package com.am.ds.raft.rlog

import java.util.concurrent.{LinkedBlockingQueue, SynchronousQueue, ThreadPoolExecutor, TimeUnit}

import com.am.ds.raft.RLog
import com.am.ds.raft.rpc._
import com.am.ds.raft.statemachine.{CommandExecutor, StateMachine}
import com.am.ds.raft.util.Logging
import com.am.ds.raft.util.RaftConversions.fromFunctionToRunnable
import com.twitter.concurrent.NamedPoolThreadFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, future}


/**
 * Description goes here
 * @author ashrith 
 */
class CommandApplier(rlog: RLog, stateMachine: StateMachine) extends Logging {

  val commandExecutor = new CommandExecutor(stateMachine)
  val commitIndexQueue = new LinkedBlockingQueue[Long]()

  val workerPool = new ThreadPoolExecutor(0, 1,
    10L, TimeUnit.SECONDS, new SynchronousQueue[Runnable](), new NamedPoolThreadFactory("CommandApplier-worker", true))

  @volatile
  var commitIndex: Long = 0
  @volatile
  var lastApplied: Long = stateMachine.lastAppliedIndex

  def start: Unit = {
    workerPool.execute(asyncApplier _)
  }

  def start(index: Long): Unit = {
    lastApplied = index
    start
  }

  def stop = {
    workerPool.shutdownNow()
    workerPool.awaitTermination(10, TimeUnit.SECONDS)
  }

  def commit(index: Long) = if (lastApplied < index) commitIndexQueue.offer(index)

  private def asyncApplier() = {
    LOG.info(s"Starting applier from index #{}", lastApplied)
    try {
      while (true) {
        val index = next
        if (lastApplied < index) {
          val entry = rlog.logEntry(index)
          if (isFromCurrentTerm(entry)) {
            applyUntil(entry.get)
          }
        }
      }
    } catch {
      case e: InterruptedException => LOG.info("Shutdown CommandApplier...")
    }
  }

  def replay: Unit = {
    val latestClusterConfigurationEntry = findLatestClusterConfiguration
    latestClusterConfigurationEntry foreach { entry =>
      LOG.info("Found cluster configuration in the log: {}", entry.command)
      rlog.cluster.apply(entry.index, entry.command.asInstanceOf[ClusterConfigurationCommand])
    }
    val from = lastApplied + 1
    val to = commitIndex
    if (from > to) {
      LOG.info("No entry to replay. commitIndex is #{}", commitIndex)
      return
    }
    replay(from, to)
  }

  private def findLatestClusterConfiguration: Option[LogEntry] = {
    rlog.findLastLogIndex to 1 by -1 find { index =>
      val logEntry = rlog.logEntry(index)
      if (!logEntry.isDefined) return None
      logEntry.collect { case LogEntry(term, entry, c: ClusterConfigurationCommand) => true }.getOrElse(false)
    } map { index => rlog.logEntry(index) } flatten
  }

  private def replay(from: Long, to: Long) = {
    LOG.debug("Start log replay from index #{} to #{}", from, to)
    rlog.logEntry(to).foreach {
      entry =>
        applyUntil(entry)
    }
    LOG.debug("Finished log replay")
  }

  private def isFromCurrentTerm(entryOption: Option[LogEntry]) = {
    entryOption.map(entry => entry.term == rlog.cluster.local.term).getOrElse(false)
  }

  private def applyUntil(entry: LogEntry) = rlog.shared {
    (lastApplied + 1) to entry.index foreach { index =>
      entryToApply(index, entry).map { entry =>
        updateCommitIndex(index)
        val command = entry.command
        LOG.debug("Will apply committed entry {}", entry)
        val result = execute(entry.index, entry.command)
        updateLastAppliedIndex(index)
        notifyResult(index, result)
      }.orElse {
        LOG.error(s"Missing index #$index")
        None
      }
    }
  }

  private def updateCommitIndex(index: Long) = {
    commitIndex = index
    LOG.debug("New commitIndex is #{}", index)
  }

  private def updateLastAppliedIndex(index: Long) = {
    lastApplied = index //What do we assume about the StateMachine persistence?
    LOG.debug("Last applied index is #{}", index)
  }

  private def entryToApply(index: Long, entry: LogEntry) = {
    if (index == entry.index) Some(entry) else rlog.logEntry(index)
  }

  private def notifyResult(index: Long, result: Any) = {
    val applyPromise = rlog.applyPromises.get(index).asInstanceOf[Promise[Any]]
    if (applyPromise != null) {
      applyPromise.success(result)
      rlog.applyPromises.remove(index)
    }
  }

  private def isCommitted(index: Long) = index <= commitIndex

  private def execute(index: Long, command: Command) = {
    command match {
      case jointConf: JointConfiguration => executeEnterJointConsensus(index, jointConf)
      case newConf: NewConfiguration => true
      case c: NoOp => true
      case write: WriteCommand[_] => {
        LOG.debug("Executing write {}", write)
        commandExecutor.applyWrite(index, write)
      }
    }
  }

  private def executeEnterJointConsensus(index: Long, c: JointConfiguration) = {
    if (index >= rlog.cluster.membership.index) {
      future {
        rlog.cluster.on(MajorityJointConsensus(c.newBindings))
      }
    } else {
      LOG.debug("Skipping old configuration {}", c)
    }
    true
  }

  def applyRead[T](read: ReadCommand[T]) = commandExecutor.applyRead(read)

  private def next = {
    if (commitIndexQueue.isEmpty()) {
      commitIndexQueue.take()
    } else {
      commitIndexQueue.poll
    }
  }

}