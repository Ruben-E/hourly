package nl.openvalue.http

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import nl.openvalue.data.{Employee, Project}
import nl.openvalue.data.EmployeeProtocol._
import nl.openvalue.data.ProjectProtocol.GetProjects

import scala.concurrent.Future
import scala.concurrent.duration._

class Repository(employeeActor: ActorRef, projectActor: ActorRef) {
  implicit val timeout: Timeout = 1 minute

  def getEmployee(id: String): Future[Option[Employee]] = {
    (employeeActor ? GetEmployee(id)).mapTo[Option[Employee]]
  }

  def getEmployees: Future[List[Employee]] = {
    (employeeActor ? GetEmployees).mapTo[List[Employee]]
  }

  def getProjects: Future[List[Project]] = {
    (projectActor ? GetProjects).mapTo[List[Project]]
  }
}
