package nl.openvalue

import akka.actor.{ActorSystem, Props}
import nl.openvalue.data.{Employee, EmployeeProcessor, Project, ProjectProcessor}
import nl.openvalue.data.EmployeeProtocol.HireEmployee
import nl.openvalue.data.ProjectProtocol.{AssignEmployeeToProject, CreateProject}
import nl.openvalue.http.ServerActor
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

object Main extends App {
  val system = ActorSystem("hourly")
  implicit val timeout: Timeout = 1 minute
  import system._

  val employeeActor = system.actorOf(Props(new EmployeeProcessor()))
  val projectActor = system.actorOf(Props(new ProjectProcessor()))
  val serverActor = system.actorOf(Props(new ServerActor(employeeActor, projectActor)))

  val ruben = (employeeActor ? HireEmployee("Ruben", "Ernst", "ruben@openvalue.nl")).mapTo[Employee]
  val roy = (employeeActor ? HireEmployee("Roy", "Wasse", "roy@openvalue.nl")).mapTo[Employee]
  val malmberg = (projectActor ? CreateProject("Malmberg")).mapTo[Project]
  val viod = (projectActor ? CreateProject("VIOD")).mapTo[Project]

  val result = for {
    e <- ruben
    p <- malmberg
  } yield (e, p)

  result.foreach {
    case (e, p) =>
      projectActor ! AssignEmployeeToProject(p.id, e.id)
  }
}
