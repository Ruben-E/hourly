package nl.openvalue

import akka.actor.{ActorSystem, Props}
import nl.openvalue.data._
import nl.openvalue.data.EmployeeProtocol.HireEmployee
import nl.openvalue.data.ProjectProtocol.{AssignEmployeeToProject, CreateProject}
import nl.openvalue.http.ServerActor
import akka.pattern.ask
import akka.util.Timeout
import nl.openvalue.data.WorkWeekItemProtocol.RegisterWorkWeekItem
import nl.openvalue.data.WorkWeekProtocol.RegisterWorkWeek

import scala.concurrent.duration._
import scala.util.Success

object Main extends App {
  val system = ActorSystem("hourly")
  implicit val timeout: Timeout = 1 minute
  import system._

  val employeeActor = system.actorOf(Props(new EmployeeProcessor()))
  val projectActor = system.actorOf(Props(new ProjectProcessor()))
  val workWeekActor = system.actorOf(Props(new WorkWeekProcessor()))
  val workWeekItemActor = system.actorOf(Props(new WorkWeekItemProcessor()))
  val serverActor = system.actorOf(Props(new ServerActor(employeeActor, projectActor, workWeekActor, workWeekItemActor)))

  val ruben = (employeeActor ? HireEmployee("Ruben", "Ernst", "ruben@openvalue.nl")).mapTo[Employee]
  val roy = (employeeActor ? HireEmployee("Roy", "Wasse", "roy@openvalue.nl")).mapTo[Employee]
  val malmberg = (projectActor ? CreateProject("Malmberg")).mapTo[Project]
  val viod = (projectActor ? CreateProject("VIOD")).mapTo[Project]

  val result = for {
    e <- ruben
    p <- malmberg
    ww <- (workWeekActor ? RegisterWorkWeek(10, 2017, e.id)).mapTo[WorkWeek]
  } yield (e, p, ww)

  result.foreach {
    case (e, p, ww) =>
      workWeekItemActor ! RegisterWorkWeekItem(8, 8, 8, 8, 8, 0, 0, ww.id, p.id)
      projectActor ! AssignEmployeeToProject(p.id, e.id)

  }
}
