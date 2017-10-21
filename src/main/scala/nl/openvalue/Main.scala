package nl.openvalue

import akka.actor.{ActorSystem, Props}
import nl.openvalue.data.{EmployeeProcessor, ProjectProcessor}
import nl.openvalue.data.EmployeeProtocol.HireEmployee
import nl.openvalue.data.ProjectProtocol.CreateProject
import nl.openvalue.http.ServerActor

object Main extends App {
  val system = ActorSystem("hourly")

  val employeeActor = system.actorOf(Props(new EmployeeProcessor()))
  val projectActor = system.actorOf(Props(new ProjectProcessor()))
  val serverActor = system.actorOf(Props(new ServerActor(employeeActor, projectActor)))

  employeeActor ! HireEmployee("Ruben", "Ernst", "ruben@openvalue.nl")
  employeeActor ! HireEmployee("Roy", "Wasse", "roy@openvalue.nl")
  projectActor ! CreateProject("Malmberg")
  employeeActor ! CreateProject("VIOD")
}
