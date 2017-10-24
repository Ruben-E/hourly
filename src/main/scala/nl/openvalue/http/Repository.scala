package nl.openvalue.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import nl.openvalue.data.{Employee, Project, WorkWeek, WorkWeekItem}
import nl.openvalue.data.EmployeeProtocol._
import nl.openvalue.data.ProjectProtocol.{AssignEmployeeToProject, CreateProject, GetProject, GetProjects}
import nl.openvalue.data.WorkWeekItemProtocol.GetWorkWeekItemsForWorkWeek
import nl.openvalue.data.WorkWeekProtocol.GetWorkWeeksForEmployee

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class Repository(employeeActor: ActorRef, projectActor: ActorRef, workWeekActor: ActorRef, workWeekItemActor: ActorRef)(implicit context: ExecutionContext) {
  implicit val timeout: Timeout = 1 minute

  def getEmployee(id: String): Future[Option[Employee]] = {
    (employeeActor ? GetEmployee(id)).mapTo[Option[Employee]]
  }

  def getEmployees: Future[List[Employee]] = {
    (employeeActor ? GetEmployees).mapTo[List[Employee]]
  }

  def getEmployees(ids: List[String]): Future[List[Employee]] = {
    val employeesFuture = Future.sequence(ids.map(id => (employeeActor ? GetEmployee(id)).mapTo[Option[Employee]]))
    employeesFuture.map(_.filter(_.isDefined).map(_.get))
  }

  def getProjects: Future[List[Project]] = {
    (projectActor ? GetProjects).mapTo[List[Project]]
  }

  def getProject(id: String): Future[Option[Project]] = {
    (projectActor ? GetProject(id)).mapTo[Option[Project]]
  }

  def hireEmployee(emailAddress: String, firstName: String, lastName: String): Future[Employee] = {
    (employeeActor ? HireEmployee(firstName, lastName, emailAddress)).mapTo[Employee]
  }

  def createProject(name: String): Future[Project] = {
    (projectActor ? CreateProject(name)).mapTo[Project]
  }

  def assignEmployeeToProject(projectId: String, employeeId: String): Future[Project] = {
    (projectActor ? AssignEmployeeToProject(projectId, employeeId)).mapTo[Project]
  }

  def getWorkWeeks(employeeId: String): Future[List[WorkWeek]] = {
    (workWeekActor ? GetWorkWeeksForEmployee(employeeId)).mapTo[List[WorkWeek]]
  }

  def getWorkWeekItems(workWeekId: String): Future[List[WorkWeekItem]] = {
    (workWeekItemActor ? GetWorkWeekItemsForWorkWeek(workWeekId)).mapTo[List[WorkWeekItem]]
  }
}
