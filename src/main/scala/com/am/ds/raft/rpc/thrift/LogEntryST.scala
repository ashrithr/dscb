/**
 * Generated by Scrooge
 *   version: 3.14.1
 *   rev: a996c1128a032845c508102d62e65fc0aa7a5f41
 *   built at: 20140501-114733
 */
package com.am.ds.raft.rpc.thrift

import com.twitter.scrooge.{
  TFieldBlob, ThriftException, ThriftStruct, ThriftStructCodec3, ThriftStructFieldInfo, ThriftUtil}
import org.apache.thrift.protocol._
import org.apache.thrift.transport.{TMemoryBuffer, TTransport}
import java.nio.ByteBuffer
import java.util.Arrays
import scala.collection.immutable.{Map => immutable$Map}
import scala.collection.mutable.Builder
import scala.collection.mutable.{
  ArrayBuffer => mutable$ArrayBuffer, Buffer => mutable$Buffer,
  HashMap => mutable$HashMap, HashSet => mutable$HashSet}
import scala.collection.{Map, Set}


object LogEntryST extends ThriftStructCodec3[LogEntryST] {
  private val NoPassthroughFields = immutable$Map.empty[Short, TFieldBlob]
  val Struct = new TStruct("LogEntryST")
  val TermField = new TField("term", TType.I32, 1)
  val TermFieldManifest = implicitly[Manifest[Int]]
  val IndexField = new TField("index", TType.I64, 2)
  val IndexFieldManifest = implicitly[Manifest[Long]]
  val CommandField = new TField("command", TType.STRING, 3)
  val CommandFieldManifest = implicitly[Manifest[ByteBuffer]]

  /**
   * Field information in declaration order.
   */
  lazy val fieldInfos: scala.List[ThriftStructFieldInfo] = scala.List[ThriftStructFieldInfo](
    new ThriftStructFieldInfo(
      TermField,
      false,
      TermFieldManifest,
      None,
      None,
      immutable$Map(
      ),
      immutable$Map(
      )
    ),
    new ThriftStructFieldInfo(
      IndexField,
      false,
      IndexFieldManifest,
      None,
      None,
      immutable$Map(
      ),
      immutable$Map(
      )
    ),
    new ThriftStructFieldInfo(
      CommandField,
      false,
      CommandFieldManifest,
      None,
      None,
      immutable$Map(
      ),
      immutable$Map(
      )
    )
  )

  lazy val structAnnotations: immutable$Map[String, String] =
    immutable$Map[String, String](
    )

  /**
   * Checks that all required fields are non-null.
   */
  def validate(_item: LogEntryST) {
    if (_item.command == null) throw new TProtocolException("Required field command cannot be null")
  }

  override def encode(_item: LogEntryST, _oproto: TProtocol) {
    _item.write(_oproto)
  }

  override def decode(_iprot: TProtocol): LogEntryST = {
    var term: Int = 0
    var _got_term = false
    var index: Long = 0L
    var _got_index = false
    var command: ByteBuffer = null
    var _got_command = false
    var _passthroughFields: Builder[(Short, TFieldBlob), immutable$Map[Short, TFieldBlob]] = null
    var _done = false

    _iprot.readStructBegin()
    while (!_done) {
      val _field = _iprot.readFieldBegin()
      if (_field.`type` == TType.STOP) {
        _done = true
      } else {
        _field.id match {
          case 1 =>
            _field.`type` match {
              case TType.I32 => {
                term = readTermValue(_iprot)
                _got_term = true
              }
              case _actualType =>
                val _expectedType = TType.I32
            
                throw new TProtocolException(
                  "Received wrong type for field 'term' (expected=%s, actual=%s).".format(
                    ttypeToHuman(_expectedType),
                    ttypeToHuman(_actualType)
                  )
                )
            }
          case 2 =>
            _field.`type` match {
              case TType.I64 => {
                index = readIndexValue(_iprot)
                _got_index = true
              }
              case _actualType =>
                val _expectedType = TType.I64
            
                throw new TProtocolException(
                  "Received wrong type for field 'index' (expected=%s, actual=%s).".format(
                    ttypeToHuman(_expectedType),
                    ttypeToHuman(_actualType)
                  )
                )
            }
          case 3 =>
            _field.`type` match {
              case TType.STRING => {
                command = readCommandValue(_iprot)
                _got_command = true
              }
              case _actualType =>
                val _expectedType = TType.STRING
            
                throw new TProtocolException(
                  "Received wrong type for field 'command' (expected=%s, actual=%s).".format(
                    ttypeToHuman(_expectedType),
                    ttypeToHuman(_actualType)
                  )
                )
            }
          case _ =>
            if (_passthroughFields == null)
              _passthroughFields = immutable$Map.newBuilder[Short, TFieldBlob]
            _passthroughFields += (_field.id -> TFieldBlob.read(_field, _iprot))
        }
        _iprot.readFieldEnd()
      }
    }
    _iprot.readStructEnd()

    if (!_got_term) throw new TProtocolException("Required field 'LogEntryST' was not found in serialized data for struct LogEntryST")
    if (!_got_index) throw new TProtocolException("Required field 'LogEntryST' was not found in serialized data for struct LogEntryST")
    if (!_got_command) throw new TProtocolException("Required field 'LogEntryST' was not found in serialized data for struct LogEntryST")
    new Immutable(
      term,
      index,
      command,
      if (_passthroughFields == null)
        NoPassthroughFields
      else
        _passthroughFields.result()
    )
  }

  def apply(
    term: Int,
    index: Long,
    command: ByteBuffer
  ): LogEntryST =
    new Immutable(
      term,
      index,
      command
    )

  def unapply(_item: LogEntryST): Option[scala.Product3[Int, Long, ByteBuffer]] = Some(_item)


  private def readTermValue(_iprot: TProtocol): Int = {
    _iprot.readI32()
  }

  private def writeTermField(term_item: Int, _oprot: TProtocol) {
    _oprot.writeFieldBegin(TermField)
    writeTermValue(term_item, _oprot)
    _oprot.writeFieldEnd()
  }

  private def writeTermValue(term_item: Int, _oprot: TProtocol) {
    _oprot.writeI32(term_item)
  }

  private def readIndexValue(_iprot: TProtocol): Long = {
    _iprot.readI64()
  }

  private def writeIndexField(index_item: Long, _oprot: TProtocol) {
    _oprot.writeFieldBegin(IndexField)
    writeIndexValue(index_item, _oprot)
    _oprot.writeFieldEnd()
  }

  private def writeIndexValue(index_item: Long, _oprot: TProtocol) {
    _oprot.writeI64(index_item)
  }

  private def readCommandValue(_iprot: TProtocol): ByteBuffer = {
    _iprot.readBinary()
  }

  private def writeCommandField(command_item: ByteBuffer, _oprot: TProtocol) {
    _oprot.writeFieldBegin(CommandField)
    writeCommandValue(command_item, _oprot)
    _oprot.writeFieldEnd()
  }

  private def writeCommandValue(command_item: ByteBuffer, _oprot: TProtocol) {
    _oprot.writeBinary(command_item)
  }



  private def ttypeToHuman(byte: Byte) = {
    // from https://github.com/apache/thrift/blob/master/lib/java/src/org/apache/thrift/protocol/TType.java
    byte match {
      case TType.STOP   => "STOP"
      case TType.VOID   => "VOID"
      case TType.BOOL   => "BOOL"
      case TType.BYTE   => "BYTE"
      case TType.DOUBLE => "DOUBLE"
      case TType.I16    => "I16"
      case TType.I32    => "I32"
      case TType.I64    => "I64"
      case TType.STRING => "STRING"
      case TType.STRUCT => "STRUCT"
      case TType.MAP    => "MAP"
      case TType.SET    => "SET"
      case TType.LIST   => "LIST"
      case TType.ENUM   => "ENUM"
      case _            => "UNKNOWN"
    }
  }

  object Immutable extends ThriftStructCodec3[LogEntryST] {
    override def encode(_item: LogEntryST, _oproto: TProtocol) { _item.write(_oproto) }
    override def decode(_iprot: TProtocol): LogEntryST = LogEntryST.decode(_iprot)
  }

  /**
   * The default read-only implementation of LogEntryST.  You typically should not need to
   * directly reference this class; instead, use the LogEntryST.apply method to construct
   * new instances.
   */
  class Immutable(
    val term: Int,
    val index: Long,
    val command: ByteBuffer,
    override val _passthroughFields: immutable$Map[Short, TFieldBlob]
  ) extends LogEntryST {
    def this(
      term: Int,
      index: Long,
      command: ByteBuffer
    ) = this(
      term,
      index,
      command,
      Map.empty
    )
  }

  /**
   * This Proxy trait allows you to extend the LogEntryST trait with additional state or
   * behavior and implement the read-only methods from LogEntryST using an underlying
   * instance.
   */
  trait Proxy extends LogEntryST {
    protected def _underlying_LogEntryST: LogEntryST
    override def term: Int = _underlying_LogEntryST.term
    override def index: Long = _underlying_LogEntryST.index
    override def command: ByteBuffer = _underlying_LogEntryST.command
    override def _passthroughFields = _underlying_LogEntryST._passthroughFields
  }
}

trait LogEntryST
  extends ThriftStruct
  with scala.Product3[Int, Long, ByteBuffer]
  with java.io.Serializable
{
  import LogEntryST._

  def term: Int
  def index: Long
  def command: ByteBuffer

  def _passthroughFields: immutable$Map[Short, TFieldBlob] = immutable$Map.empty

  def _1 = term
  def _2 = index
  def _3 = command

  /**
   * Gets a field value encoded as a binary blob using TCompactProtocol.  If the specified field
   * is present in the passthrough map, that value is returend.  Otherwise, if the specified field
   * is known and not optional and set to None, then the field is serialized and returned.
   */
  def getFieldBlob(_fieldId: Short): Option[TFieldBlob] = {
    lazy val _buff = new TMemoryBuffer(32)
    lazy val _oprot = new TCompactProtocol(_buff)
    _passthroughFields.get(_fieldId) orElse {
      val _fieldOpt: Option[TField] =
        _fieldId match {
          case 1 =>
            if (true) {
              writeTermValue(term, _oprot)
              Some(LogEntryST.TermField)
            } else {
              None
            }
          case 2 =>
            if (true) {
              writeIndexValue(index, _oprot)
              Some(LogEntryST.IndexField)
            } else {
              None
            }
          case 3 =>
            if (command ne null) {
              writeCommandValue(command, _oprot)
              Some(LogEntryST.CommandField)
            } else {
              None
            }
          case _ => None
        }
      _fieldOpt match {
        case Some(_field) =>
          val _data = Arrays.copyOfRange(_buff.getArray, 0, _buff.length)
          Some(TFieldBlob(_field, _data))
        case None =>
          None
      }
    }
  }

  /**
   * Collects TCompactProtocol-encoded field values according to `getFieldBlob` into a map.
   */
  def getFieldBlobs(ids: TraversableOnce[Short]): immutable$Map[Short, TFieldBlob] =
    (ids flatMap { id => getFieldBlob(id) map { id -> _ } }).toMap

  /**
   * Sets a field using a TCompactProtocol-encoded binary blob.  If the field is a known
   * field, the blob is decoded and the field is set to the decoded value.  If the field
   * is unknown and passthrough fields are enabled, then the blob will be stored in
   * _passthroughFields.
   */
  def setField(_blob: TFieldBlob): LogEntryST = {
    var term: Int = this.term
    var index: Long = this.index
    var command: ByteBuffer = this.command
    var _passthroughFields = this._passthroughFields
    _blob.id match {
      case 1 =>
        term = readTermValue(_blob.read)
      case 2 =>
        index = readIndexValue(_blob.read)
      case 3 =>
        command = readCommandValue(_blob.read)
      case _ => _passthroughFields += (_blob.id -> _blob)
    }
    new Immutable(
      term,
      index,
      command,
      _passthroughFields
    )
  }

  /**
   * If the specified field is optional, it is set to None.  Otherwise, if the field is
   * known, it is reverted to its default value; if the field is unknown, it is subtracked
   * from the passthroughFields map, if present.
   */
  def unsetField(_fieldId: Short): LogEntryST = {
    var term: Int = this.term
    var index: Long = this.index
    var command: ByteBuffer = this.command

    _fieldId match {
      case 1 =>
        term = 0
      case 2 =>
        index = 0L
      case 3 =>
        command = null
      case _ =>
    }
    new Immutable(
      term,
      index,
      command,
      _passthroughFields - _fieldId
    )
  }

  /**
   * If the specified field is optional, it is set to None.  Otherwise, if the field is
   * known, it is reverted to its default value; if the field is unknown, it is subtracked
   * from the passthroughFields map, if present.
   */
  def unsetTerm: LogEntryST = unsetField(1)

  def unsetIndex: LogEntryST = unsetField(2)

  def unsetCommand: LogEntryST = unsetField(3)


  override def write(_oprot: TProtocol) {
    LogEntryST.validate(this)
    _oprot.writeStructBegin(Struct)
    writeTermField(term, _oprot)
    writeIndexField(index, _oprot)
    if (command ne null) writeCommandField(command, _oprot)
    _passthroughFields.values foreach { _.write(_oprot) }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    term: Int = this.term,
    index: Long = this.index,
    command: ByteBuffer = this.command,
    _passthroughFields: immutable$Map[Short, TFieldBlob] = this._passthroughFields
  ): LogEntryST =
    new Immutable(
      term,
      index,
      command,
      _passthroughFields
    )

  override def canEqual(other: Any): Boolean = other.isInstanceOf[LogEntryST]

  override def equals(other: Any): Boolean =
    _root_.scala.runtime.ScalaRunTime._equals(this, other) &&
      _passthroughFields == other.asInstanceOf[LogEntryST]._passthroughFields

  override def hashCode: Int = _root_.scala.runtime.ScalaRunTime._hashCode(this)

  override def toString: String = _root_.scala.runtime.ScalaRunTime._toString(this)


  override def productArity: Int = 3

  override def productElement(n: Int): Any = n match {
    case 0 => this.term
    case 1 => this.index
    case 2 => this.command
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix: String = "LogEntryST"
}