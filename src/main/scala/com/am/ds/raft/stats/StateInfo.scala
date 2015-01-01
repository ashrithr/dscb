package com.am.ds.raft.stats

/**
 * Description goes here
 * @author ashrith 
 */
class StateInfo

case class LeaderInfo(leaderUpTime: String, followers: Map[String, FollowerInfo]) extends StateInfo

case class NonLeaderInfo(following: String) extends StateInfo

case class FollowerInfo(lsatHeartbeatACK: String, matchIndex: Int, nextIndex: Int)
