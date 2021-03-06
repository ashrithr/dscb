package com.am.ds.akka.chatserver

/**
 * Set of convenient parsing methods
 * @author ashrith 
 */
object Parsing {
  /**
   * converts integer to 8 bit string
   */
  def toBinary(i: Int, digits: Int = 8) =
    String.format("%" + digits + "s", i.toBinaryString).replace(' ', '0')

  /**
   * Makes string from array of bytes
   */
  def byteArrayToString(byteArray: Array[Byte]): String =
    byteArray.toList.map(x => x.toChar).mkString

  def intToByteArray(x: Int): Array[Byte] = {
    val binaryStr = x.toBinaryString
    val pad = "0" * (32 - binaryStr.length)
    val fullBinStr = pad + binaryStr

    splitToStringsOfLen(fullBinStr, 8).map(x => Integer.parseInt(x, 2).toByte).toArray
  }

  def splitToStringsOfLen(str: String, len: Int): List[String] = {
    def rec(str: String, acc: List[String]): List[String] = {
      str match {
        case "" => acc
        case string =>
          val tpl = string.splitAt(string.length - len)
          rec(tpl._1, tpl._2 :: acc)
      }
    }
    rec(str, List())
  }
}
