package com.am.ds.nameservices

import java.net.InetAddress
import javax.naming.NamingException
import javax.naming.directory.{Attribute, InitialDirContext}

/**
 * Simple name lookup/resolution implementation using javax.naming
 * @author ashrith 
 */
object DNS {
  def main(args: Array[String]) {
    println('ip, lookupIp("sync.cloudwick.com"))
    println('ip, lookupIp("www.google.com"))
    println('hostname, reverseDns("198.0.218.179"))
  }

  /**
   * Performs a forward dns lookup (hostname to ip)
   * @param host The hostname to lookup
   * @return List of possible ip addresses
   */
  def lookupIp(host: String): List[String] = {
    // Show the Internet Address as name/address
    val inetAddress = InetAddress.getByName(host)
    println(inetAddress.getHostName + " " + inetAddress.getHostAddress)

    val dirContext = new InitialDirContext

    val attributes = try {
      // get the initial directory context and the DNS records for inetAddress
      dirContext.getAttributes("dns://8.8.8.8/%s" format host, Array[String]("A", "CNAME"))
    } catch {
      case e: NamingException =>
        e.printStackTrace()
        return Nil
    }

    val list = {
      val attributeEnumeration = attributes.getAll
      var list = List[Attribute]()
      while (attributeEnumeration.hasMore)
        list = attributeEnumeration.next :: list
      attributeEnumeration.close()
      list.reverse
    }

    list map (x => x.getID -> x.get.toString) flatMap {
      case ("A", x) => List(x)
      case ("CNAME", x) => lookupIp(x)
      case (_, x) => Nil
    }
  }

  /**
   * Returns the hostname associated with the specified IP address
   * @param ip The address to reverse lookup
   * @return The hostname associated with the provided IP
   */
  def reverseDns(ip: String): String = {
    // build the reverse IP lookup form
    val reverseIP = ip.split("\\.").reverse.mkString(".") + ".in-addr.arpa"
    val attributes = try {
      new InitialDirContext getAttributes("dns:/%s" format reverseIP, Array[String]("PTR"))
    } catch {
      case _: NamingException => return ""
    }

    var hostname = attributes.get("PTR").get.toString
    if (hostname.charAt(hostname.length - 1) == '.')
      hostname = hostname.substring(0, hostname.length - 1)

    hostname
  }
}
