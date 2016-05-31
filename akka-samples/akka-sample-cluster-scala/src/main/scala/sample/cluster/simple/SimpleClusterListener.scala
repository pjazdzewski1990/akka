package sample.cluster.simple

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.{Address, ActorLogging, Actor}

class SimpleClusterListener extends Actor with ActorLogging with ColorfulLogger {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe

    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[ClusterDomainEvent])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      writeGreen(s"Member is Up: ${member.address}")
    case UnreachableMember(member) =>
      writeYellow(s"Member detected as unreachable: ${member}")
    case MemberRemoved(member, previousStatus) =>
      writeRed(s"Member is Removed: ${member.address} after ${previousStatus}")
    case LeaderChanged(leader) =>
      writeCyan(s"Current leader: $leader")
    case _: MemberEvent => // ignore
  }
}

trait ColorfulLogger { self: ActorLogging =>
  val ANSI_RESET = "\u001B[0m"
  val ANSI_BLACK = "\u001B[30m"
  val ANSI_RED = "\u001B[31m"
  val ANSI_GREEN = "\u001B[32m"
  val ANSI_YELLOW = "\u001B[33m"
  val ANSI_BLUE = "\u001B[34m"
  val ANSI_PURPLE = "\u001B[35m"
  val ANSI_CYAN = "\u001B[36m"
  val ANSI_WHITE = "\u001B[37m"

  def writeGreen(s: String) = log.info(s"$ANSI_GREEN $s $ANSI_RESET")
  def writeRed(s: String) = log.info(s"$ANSI_RED $s $ANSI_RESET")
  def writeYellow(s: String) = log.info(s"$ANSI_YELLOW $s $ANSI_RESET")
  def writeCyan(s: String) = log.info(s"$ANSI_CYAN $s $ANSI_RESET")
}