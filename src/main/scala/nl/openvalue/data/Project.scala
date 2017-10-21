package nl.openvalue.data

import java.util.UUID

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import nl.openvalue.data.ProjectProtocol._

import scala.util.{Success, Try}

case class Project(id: String, name: String, employeeIds: Set[String] = Set.empty)

object Project {
  def create(cmd: CreateProject): Project = {
    val id = UUID.randomUUID().toString
    Project(id, cmd.name)
  }
}

object ProjectProtocol {

  case class CreateProject(name: String)

  case class GetProject(id: String)

  case object GetProjects

  case class ProjectCreated(id: String, name: String)

}

final case class ProjectState(projects: Map[String, Project] = Map.empty) {
  def update(project: Project) = copy(projects = projects + (project.id -> project))

  def get(id: String) = projects.get(id)

  def getAll = projects.values.toList
}

class ProjectProcessor extends PersistentActor with ActorLogging {
  override def persistenceId = "project-persistence"

  var state = ProjectState()

  override def receiveRecover: Receive = {
    case _ =>
  }

  override def receiveCommand: Receive = {
    case cmd: CreateProject =>
      createProject(cmd).fold(
        f => sender ! f,
        p => persist(ProjectCreated(p.id, p.name)) { event =>
          context.system.eventStream.publish(event)
        }
      )
    case GetProject(id) => sender ! state.get(id)
    case GetProjects => sender ! state.getAll
  }

  def updateState(project: Project): Unit = {
    state = state.update(project)
  }

  def createProject(cmd: CreateProject): Try[Project] = {
    val project = Project.create(cmd)
    updateState(project)
    log.info(s"Project [$project] created ")
    Success(project)
  }
}
