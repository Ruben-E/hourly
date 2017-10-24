package nl.openvalue.data

import akka.actor.{ActorLogging, Status}
import akka.persistence.PersistentActor
import nl.openvalue.data.WorkWeekProtocol.{GetWorkWeeksForEmployee, RegisterWorkWeek, WorkWeekRegistered}

import scala.util.{Success, Try}

case class WorkWeek(id: String, week: Int, year: Int, employeeId: String)

object WorkWeek {
  def register(cmd: RegisterWorkWeek): WorkWeek = {
    val id = s"${cmd.year}${cmd.week}-${cmd.employeeId}"
    WorkWeek(id, cmd.week, cmd.year, cmd.employeeId)
  }
}

object WorkWeekProtocol {

  case class RegisterWorkWeek(week: Int, year: Int, employeeId: String)

  case class GetWorkWeeksForEmployee(employeeId: String)

  case class WorkWeekRegistered(id: String, week: Int, year: Int, employeeId: String)

}

final case class WorkWeekState(workWeeks: Map[String, WorkWeek] = Map.empty) {
  def update(w: WorkWeek) = copy(workWeeks = workWeeks + (w.id -> w))

  def getByEmployeeId(employeeId: String) = workWeeks.values.filter(_.employeeId == employeeId).toList
}

class WorkWeekProcessor extends PersistentActor with ActorLogging {
  override def persistenceId = "work-week-persistence"

  var state = WorkWeekState()

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case cmd: RegisterWorkWeek =>
      registerWorkWeek(cmd).fold(
        f => sender ! Status.Failure(f),
        w => persist(WorkWeekRegistered(w.id, w.week, w.year, w.employeeId)) { event =>
          context.system.eventStream.publish(event)
          sender ! Status.Success(w)
        }
      )
    case GetWorkWeeksForEmployee(employeeId) =>
      sender ! state.getByEmployeeId(employeeId)
  }

  def updateState(ww: WorkWeek): Unit = {
    state = state.update(ww)
  }

  def registerWorkWeek(cmd: RegisterWorkWeek): Try[WorkWeek] = {
    val workWeek = WorkWeek.register(cmd) // TODO: Check if an work week with that id already exists
    updateState(workWeek)
    Success(workWeek)
  }
}
