package com.am.ds.raft

import com.am.ds.raft.example.{Get, Put, KVStore}
import com.am.ds.raft.util.Logging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException
import org.scalatest._

/**
 * Description goes here
 * @author ashrith 
 */
@RunWith(classOf[JUnitRunner])
class RaftIntegrationTest extends FlatSpec with Matchers with Logging {

  val Key1 = "key1"
  val Value1 = "value1"

  val Member1Address = "localhost:9091"
  val Member2Address = "localhost:9092"
  val Member3Address = "localhost:9093"
  val Member4Address = "localhost:9094"

  "A single member cluster" should "elect a Leader" in {
    val raft = RaftBuilder().listenAddress(Member1Address).dataDir(someTmpDir)
                  .stateMachine(new KVStore()).bootstrap(true).build
    raft start

    raft.isLeader should be

    raft stop
  }

  it should "read committed writes" in {
    val raft = RaftBuilder().listenAddress(Member1Address).dataDir(someTmpDir)
      .stateMachine(new KVStore()).bootstrap(true).build
    raft start

    await(raft.write(Put(Key1, Value1)))

    val readValue = await(raft.read(Get(Key1)))

    readValue should be(Value1)

    raft stop
  }

  it should "compact a log & reload snapshot" in {
    val dir = someTmpDir

    val raft = RaftBuilder().listenAddress(Member1Address).dataDir(dir)
      .compactionThreshold(5 + 1) //5 writes + 1 NoOp
      .stateMachine(new KVStore()).bootstrap(true).build
    raft start

    await(raft.write(Put("key1", "value1")))
    await(raft.write(Put("key2", "value2")))
    await(raft.write(Put("key3", "value3")))
    await(raft.write(Put("key4", "value4")))
    await(raft.write(Put("key5", "value5")))

    //log should be compacted at this point

    await(raft.write(Put("key6", "value6")))

    waitSomeTimeForElection

    raft stop

    val raftRestarted = rebuild(raft)

    raftRestarted.start

    await(raftRestarted.read(Get("key1"))) should be("value1")
    await(raftRestarted.read(Get("key2"))) should be("value2")
    await(raftRestarted.read(Get("key3"))) should be("value3")
    await(raftRestarted.read(Get("key4"))) should be("value4")
    await(raftRestarted.read(Get("key5"))) should be("value5")

    raftRestarted.stop
  }

  it should "restore latest cluster configuration from Log" in {
    val dir = someTmpDir

    val raft = RaftBuilder().listenAddress(Member1Address).dataDir(dir)
      .stateMachine(new KVStore()).bootstrap(true).build
    raft start

    //It is expected to timeout since Member2 is not up and the configuration must to committed under the new configuration (member1 and member2)
    //TODO: What if two subsequent JointConfiguration ???
    intercept[TimeoutException] {
      await(raft.addMember(Member2Address))
    }

    raft stop

    val raftRestarted = rebuild(raft)

    val members = raftRestarted.getMembers

    members should contain(Member2Address)
  }

  it should "restore latest cluster configuration from Snapshot" in {
    val dir = someTmpDir

    val raft = RaftBuilder().listenAddress(Member1Address).dataDir(dir)
      .compactionThreshold(2 + 1) //1 writes + 1 NoOp
      .stateMachine(new KVStore()).bootstrap(true).build
    raft start

    //It is expected to timeout since 9092 is not up and the configuration need to committed under the new configuration (9091 and 9092)
    //TODO: What if two subsequent EnterJointConsensus ???
    intercept[TimeoutException] {
      await(raft.addMember(Member2Address))
    }

    //This will force the Snapshot. Again, it is expected to timeout.
    intercept[TimeoutException] {
      await(raft.write(Put(Key1, Value1)))
    }

    raft.stop

    val raftRestarted = rebuild(raft)

    val members = raftRestarted.getMembers

    members should contain(Member2Address)
  }

  "A 3 member cluster" should "elect a single Leader" in withThreeMemberCluster { members =>
    val leader = members leader
    val followers = members followers

    leader should not be null
    followers.length should be(2)
  }

  it should "failover Leader" in withThreeMemberCluster { members =>
    val originalLeader = members leader
    val followers = members followers

    originalLeader stop

    waitSomeTimeForElection

    //a leader must be elected from the followers
    val newLeader = followers leader

    newLeader should not be null
    newLeader should not be originalLeader
  }

  it should "read committed writes" in withThreeMemberCluster { members =>

    val leader = members leader

    await(leader.write(Put(Key1, Value1)))

    members foreach { member =>
      await(member.read(Get(Key1))) should be(Value1)
    }

  }

  it should "forward writes to the Leader" in withThreeMemberCluster { members =>

    val someFollower = (members followers) head

    //this write is forwarded to the Leader
    await(someFollower.write(Put(Key1, Value1)))

    members foreach { member =>
      await(member.read[String](Get(Key1))) should be(Value1)
    }
  }

  it should "maintain quorum when 1 member goes down" in withThreeMemberCluster { members =>

    val someFollower = (members followers) head

    //a member goes down
    someFollower.stop

    val leader = members leader

    //leader still have quorum. this write is going to be committed
    await(leader.write(Put(Key1, Value1)))

    (members diff Seq(someFollower)) foreach { member =>
      await(member.read(Get(Key1))) should be(Value1)
    }
  }

  it should "loose quorum when 2 members goes down" in withThreeMemberCluster { members =>

    val leader = members leader

    //all the followers goes down
    (members followers) foreach { _.stop }

    //leader no longer have quorum. this write is going to be rejected
    intercept[TimeoutException] {
      await(leader.write(Put(Key1, Value1)))
    }
  }

  it should "replicate missing commands on restarted member" in {

    val member1 = RaftBuilder().listenAddress(Member1Address)
      .dataDir(someTmpDir).stateMachine(new KVStore()).bootstrap(true).build

    val member2 = RaftBuilder().listenAddress(Member2Address).members(Seq(Member1Address, Member3Address))
      .minElectionTimeout(1000).maxElectionTimeout(1000).dataDir(someTmpDir)
      .stateMachine(new KVStore()).build

    val member3 = RaftBuilder().listenAddress(Member3Address).members(Seq(Member2Address, Member1Address))
      .minElectionTimeout(2000).maxElectionTimeout(2000).dataDir(someTmpDir)
      .stateMachine(new KVStore()).build

    val members = Seq(member1, member2, member3)

    members foreach { _ start }

    try {

      val leader = members leader

      //member3 goes down
      member3.stop

      //still having a quorum. This write is committed.
      await(leader.write(Put(Key1, Value1)))

      //member3 is back
      val restartedMember3 = rebuild(member3)
      restartedMember3.start

      //wait some time (> heartbeatsInterval) for missing appendEntries to arrive
      waitSomeTimeForAppendEntries

      //read from its local state machine to check if missing appendEntries have been replicated
      val readValue = restartedMember3.readLocal(Get(Key1))

      readValue should be(Value1)
      restartedMember3.stop
    } finally {
      member1.stop
      member2.stop
    }
  }

  it should "add a new member" in withThreeMemberCluster { members =>

    val leader = members leader

    await(leader.write(Put(Key1, Value1)))

    //add member4 to the cluster
    await(leader.addMember(Member4Address))

    val member4 = RaftBuilder().listenAddress(Member4Address).members(Seq(Member2Address, Member1Address, Member3Address))
      .dataDir(someTmpDir).minElectionTimeout(2000).maxElectionTimeout(3000).stateMachine(new KVStore()).build
    //start member4
    member4.start

    //get value for k1. this is going to be forwarded to the Leader.
    val replicatedValue = await(member4.read(Get(Key1)))
    replicatedValue should be(Value1)

    //wait some time (> heartbeatsInterval) for missing appendEntries to arrive
    waitSomeTimeForAppendEntries

    //get value for Key1 from local
    val localValue = member4.readLocal(Get(Key1))

    localValue should be(replicatedValue)

    member4.stop
  }

  it should "overwrite uncommitted entries on an old Leader" in withThreeMemberCluster { members =>

    val leader = members leader

    val followers = (members followers)

    //stop the followers
    followers foreach { _.stop }

    //this two writes will timeout since no majority can be reached
    for (i <- (1 to 2)) {
      intercept[TimeoutException] {
        await(leader.write(Put(Key1, Value1)))
      }
    }
    //at this point the leader has two uncommitted entries

    //leader stops
    leader.stop

    //followers came back
    val rebuiltFollowers = followers map { rebuild(_) }
    rebuiltFollowers foreach { _.start }
    val livemembers = rebuiltFollowers

    waitSomeTimeForElection

    //a new leader is elected
    val newleader = livemembers leader

    //old leader came back
    val oldleader = rebuild(leader)
    oldleader.start

    waitSomeTimeForAppendEntries

    //those two uncommitted entries of the oldleader must be overridden and removed by the new Leader as part of appendEntries
    await(newleader.read(Get(Key1))) should be(null)

    oldleader.stop
    rebuiltFollowers foreach { _.stop }

  }

  implicit def membersSequence(members: Seq[Raft]): RaftSequence = {
    new RaftSequence(members)
  }

  class RaftSequence(members: Seq[Raft]) {

    def followers = members filterNot { _ isLeader }
    def leader = {
      val leaders = (members diff followers)
      val theLeader = leaders.head
      withClue("Not unique Leader") { leaders diff Seq(theLeader) should be('empty) }
      theLeader
    }

  }

  private def withThreeMemberCluster(test: Seq[Raft] => Any) = {
    //member1 has default election timeout (500ms - 700ms). It is intended to be the first to start an election and raise as the leader.
    val member1 = RaftBuilder().listenAddress(Member1Address)
      .dataDir(someTmpDir).bootstrap(true)
      .stateMachine(new KVStore()).build

    val member2 = RaftBuilder().listenAddress(Member2Address).members(Seq(Member1Address))
      //      .minElectionTimeout(1250).maxElectionTimeout(1500) //higher election timeout
      .dataDir(someTmpDir)
      .stateMachine(new KVStore()).build

    val member3 = RaftBuilder().listenAddress(Member3Address).members(Seq(Member2Address, Member1Address))
      //      .minElectionTimeout(1750).maxElectionTimeout(2000) //higher election timeout
      .dataDir(someTmpDir)
      .stateMachine(new KVStore()).build
    val members = Seq(member1, member2, member3)
    LOG.info(s"Starting all the members")
    member1 start

    member2 start

    member3 start

    waitSomeTimeForElection
    try {
      LOG.info(s"Running test...")
      test(members)
    } finally {
      LOG.info(s"Stopping all the members")
      members foreach { _ stop }
    }
  }

  private def rebuild(raft: Raft) = raft.builder.stateMachine(new KVStore).bootstrap(false).build

  private def waitSomeTimeForElection = Thread.sleep(2000)

  private def waitSomeTimeForAppendEntries = Thread.sleep(2000)

  private def someTmpDir: String = {
    "/tmp/" + System.currentTimeMillis()
  }

  private def await[T](future: Future[T]): T = {
    Await.result(future, 3 seconds)
  }

}