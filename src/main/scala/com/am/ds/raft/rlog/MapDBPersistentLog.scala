package com.am.ds.raft.rlog

import java.io.File
import java.util.concurrent.atomic.AtomicLong

import com.am.ds.raft.RLog
import com.am.ds.raft.rpc.LogEntry
import com.am.ds.raft.util.{Serializer, Logging}
import org.mapdb.DBMaker


/**
 * Description goes here
 * @author ashrith 
 */
class MapDBPersistentLog(dataDir: String, rlog: RLog) extends PersistentLog with Logging {

  val logDB = DBMaker.newFileDB(file(dataDir)).mmapFileEnable().closeOnJvmShutdown().transactionDisable().cacheDisable().make()

  val entries = logDB.getTreeMap[Long, Array[Byte]]("logEntries")
  val cachedSize = new AtomicLong(entries.size())
  val lastIndex = new AtomicLong(if (entries.isEmpty) 0 else entries.lastKey())

  def commit = logDB.commit()

  def append(entry: LogEntry): Unit = {
    entries.put(entry.index, Serializer.serialize(entry))
    cachedSize.incrementAndGet()
    lastIndex.set(entry.index)
  }

  def getEntry(index: Long): LogEntry = {
    val bytes = entries.get(index)
    if (bytes != null) Serializer.deserialize(bytes) else null.asInstanceOf[LogEntry]
  }

  def rollLog(upToIndex: Long) = {
    val range = firstIndex to upToIndex
    LOG.debug(s"Compacting ${range.size} LogEntries")
    range foreach { index => remove(index) }
    LOG.debug(s"Finished compaction")
  }

  def getLastIndex(): Long = lastIndex.longValue()

  def size() = cachedSize.longValue()

  private def remove(index: Long) = {
    entries.remove(index)
    cachedSize.decrementAndGet()
  }

  def discardEntriesFrom(index: Long) = {
    index to lastIndex.longValue() foreach { i =>
      remove(i)
    }
    lastIndex.set(index - 1)
  }

  def close() = logDB.close()

  private def firstIndex: Long = if (!entries.isEmpty) entries.firstKey else 1

  private def file(dataDir: String): File = {
    val dir = new File(dataDir)
    dir.mkdirs()
    val file = new File(dir, "rlog")
    file
  }
}