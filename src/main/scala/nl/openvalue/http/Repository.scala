package nl.openvalue.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import nl.openvalue.data.{Employee, Project}
import nl.openvalue.data.EmployeeProtocol._
import nl.openvalue.data.ProjectProtocol.{AssignEmployeeToProject, CreateProject, GetProjects}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class Repository(employeeActor: ActorRef, projectActor: ActorRef)(implicit context: ExecutionContext) {
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

  def hireEmployee(emailAddress: String, firstName: String, lastName: String): Future[Employee] = {
    (employeeActor ? HireEmployee(firstName, lastName, emailAddress)).mapTo[Employee]
  }

  def createProject(name: String): Future[Project] = {
    (projectActor ? CreateProject(name)).mapTo[Project]
  }

  def assignEmployeeToProject(projectId: String, employeeId: String): Future[Project] = {
    (projectActor ? AssignEmployeeToProject(projectId, employeeId)).mapTo[Project]
  }
}
