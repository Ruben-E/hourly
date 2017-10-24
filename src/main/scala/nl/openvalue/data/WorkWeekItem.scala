package nl.openvalue.data

import java.util.UUID

import akka.actor.{ActorLogging, Status}
import akka.persistence.PersistentActor
import nl.openvalue.data.WorkWeekItemProtocol.{GetWorkWeekItemsForWorkWeek, RegisterWorkWeekItem, WorkWeekItemRegistered}

import scala.util.{Success, Try}

case class WorkWeekItem(id: String, monday: Int, tuesday: Int, wednesday: Int, thursday: Int, friday: Int, saturday: Int, sunday: Int, workWeekId: String, projectId: String) // TODO: Made projectId optional

object WorkWeekItem {
  def register(cmd: RegisterWorkWeekItem): WorkWeekItem = {
    val id = UUID.randomUUID().toString
    WorkWeekItem(id, cmd.monday, cmd.tuesday, cmd.wednesday, cmd.thursday, cmd.friday, cmd.saturday, cmd.sunday, cmd.workWeekId, cmd.projectId)
  }
}

object WorkWeekItemProtocol {

  case class RegisterWorkWeekItem(monday: Int, tuesday: Int, wednesday: Int, thursday: Int, friday: Int, saturday: Int, sunday: Int, workWeekId: String, projectId: String)

  case class GetWorkWeekItemsForWorkWeek(workWeekId: String)

  case class WorkWeekItemRegistered(id: String, day: Int, tuesday: Int, wednesday: Int, thursday: Int, friday: Int, saturday: Int, sunday: Int, workWeekId: String, projectId: String)

}

final case class WorkWeekItemState(workWeekItems: Map[String, WorkWeekItem] = Map.empty) {
  def update(w: WorkWeekItem) = copy(workWeekItems = workWeekItems + (w.id -> w))

  def getByWorkWeekId(workWeekId: String) = workWeekItems.values.filter(_.workWeekId == workWeekId).toList
}

class WorkWeekItemProcessor extends PersistentActor with ActorLogging {
  override def persistenceId = "work-week-item-persistence"

  var state = WorkWeekItemState()

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case cmd: RegisterWorkWeekItem =>
      registerWorkWeekItem(cmd).fold(
        f => sender ! Status.Failure(f),
        w => persist(WorkWeekItemRegistered(w.id, w.monday, w.tuesday, w.wednesday, w.thursday, w.friday, w.saturday, w.sunday, w.workWeekId, w.projectId)) { event =>
          context.system.eventStream.publish(event)
          sender ! Status.Success(w)
        }
      )
    case GetWorkWeekItemsForWorkWeek(workWeekId) =>
      sender ! state.getByWorkWeekId(workWeekId)
  }

  def updateState(workWeekItem: WorkWeekItem): Unit = {
    state = state.update(workWeekItem)
  }

  def registerWorkWeekItem(cmd: RegisterWorkWeekItem): Try[WorkWeekItem] = {
    val workWeekItem = WorkWeekItem.register(cmd) // TODO: Check if an work week with that id already exists
    updateState(workWeekItem)
    Success(workWeekItem)
  }
}
