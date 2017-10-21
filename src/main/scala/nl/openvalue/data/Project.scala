package nl.openvalue.data

import java.util.UUID

import akka.actor.{ActorLogging, Status}
import akka.persistence.PersistentActor
import nl.openvalue.data.ProjectProtocol._

import scala.util.{Failure, Success, Try}

case class Project(id: String, name: String, employeeIds: Set[String] = Set.empty) {
  def assignEmployee(id: String): Project = {
    copy(employeeIds = employeeIds + id)
  }
}

object Project {
  def create(cmd: CreateProject): Project = {
    val id = UUID.randomUUID().toString
    Project(id, cmd.name)
  }
}

object ProjectProtocol {

  case class CreateProject(name: String)
  case class AssignEmployeeToProject(projectId: String, employeeId: String)

  case class GetProject(id: String)

  case object GetProjects

  case class ProjectCreated(id: String, name: String)
  case class EmployeeAssignedToProject(projectId: String, employeeId: String)

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
        f => sender ! Status.Failure(f),
        p => persist(ProjectCreated(p.id, p.name)) { event =>
          context.system.eventStream.publish(event)
          sender ! Status.Success(p)
        }
      )
    case cmd: AssignEmployeeToProject =>
      assignEmployeeToProject(cmd).fold(
        f => sender ! Status.Failure(f),
        p => persist(EmployeeAssignedToProject(p.id, cmd.employeeId)) { event =>
          context.system.eventStream.publish(event)
          sender ! Status.Success(p)
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

  def assignEmployeeToProject(cmd: AssignEmployeeToProject): Try[Project] = {
    val maybeProject = state.get(cmd.projectId).map(_.assignEmployee(cmd.employeeId))
    maybeProject match {
      case None => Failure(new IllegalStateException("Employee not found"))
      case Some(p) =>
        updateState(p)
        Success(p)
    }
  }
}
