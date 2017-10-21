package nl.openvalue.data

import java.util.UUID

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import nl.openvalue.data.EmployeeProtocol._

import scala.util.{Success, Try}

case class Employee(id: String, firstName: String, lastName: String, emailAddress: String)

object Employee {
  def hire(cmd: HireEmployee): Employee = {
    val id = UUID.randomUUID().toString
    Employee(id, cmd.firstName, cmd.lastName, cmd.emailAddress)
  }
}

object EmployeeProtocol {

  case class HireEmployee(firstName: String, lastName: String, emailAddress: String)

  case class GetEmployee(id: String)

  case object GetEmployees

  case class EmployeeHired(id: String, firstName: String, lastName: String, emailAddress: String)

}

final case class EmployeeState(employees: Map[String, Employee] = Map.empty) {
  def update(e: Employee) = copy(employees = employees + (e.id -> e))

  def get(id: String) = employees.get(id)

  def getAll = employees.values.toList
}

class EmployeeProcessor extends PersistentActor with ActorLogging {
  override def persistenceId = "employee-persistence"

  var state = EmployeeState()

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case cmd: HireEmployee =>
      hireEmployee(cmd).fold(
        f => sender ! f,
        e => persist(EmployeeHired(e.id, e.firstName, e.lastName, e.emailAddress)) { event =>
          context.system.eventStream.publish(event)
        }
      )
    case GetEmployee(id) => sender ! state.get(id)
    case GetEmployees => sender ! state.getAll
  }

  def updateState(emp: Employee): Unit = {
    state = state.update(emp)
  }

  def hireEmployee(cmd: HireEmployee): Try[Employee] = {
    val employee = Employee.hire(cmd)
    updateState(employee)
    log.info(s"Employee [$employee] hired ")
    Success(employee)
  }
}
