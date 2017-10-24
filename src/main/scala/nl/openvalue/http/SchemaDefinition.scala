package nl.openvalue.http

import nl.openvalue.data.{Employee, Project}
import sangria.schema._

object SchemaDefinition {

  val ID = Argument("id", StringType)
  val ProjectId = Argument("projectId", StringType)
  val EmployeeId = Argument("employeeId", StringType)
  val Name = Argument("name", StringType)
  val FirstName = Argument("firstName", StringType)
  val LastName = Argument("lastName", StringType)
  val EmailAddress = Argument("emailAddress", StringType)

  val Employee = ObjectType(
    "Employee", fields[Repository, Employee](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = ctx => s"${ctx.value.firstName} ${ctx.value.lastName}"),
      Field("firstName", StringType, resolve = _.value.firstName),
      Field("lastName", StringType, resolve = _.value.lastName),
      Field("emailAddress", StringType, resolve = _.value.emailAddress)
    )
  )

  val Project = ObjectType(
    "Project", fields[Repository, Project](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("employees", ListType(Employee), resolve = ctx => ctx.ctx.getEmployees(ctx.value.employeeIds.toList))
    )
  )

  val Query = ObjectType(
    "Query", fields[Repository, Unit](
      Field("employees", ListType(Employee), resolve = ctx => ctx.ctx.getEmployees),
      Field("employee", OptionType(Employee), arguments = ID :: Nil, resolve = ctx => ctx.ctx.getEmployee(ctx.arg(ID))),

      Field("projects", ListType(Project), resolve = ctx => ctx.ctx.getProjects)
    ))

  val Mutation = ObjectType("Mutation", fields[Repository, Unit](
    Field("addEmployee", Employee, arguments = FirstName :: LastName :: EmailAddress :: Nil, resolve = ctx ⇒ ctx.ctx.hireEmployee(ctx.arg(EmailAddress), ctx.arg(FirstName), ctx.arg(LastName))),
    Field("addProject", Project, arguments = Name :: Nil, resolve = ctx ⇒ ctx.ctx.createProject(ctx.arg(Name))),
    Field("assignEmployeeToProject", Project, arguments = ProjectId :: EmployeeId :: Nil, resolve = ctx ⇒ ctx.ctx.assignEmployeeToProject(ctx.arg(ProjectId), ctx.arg(EmployeeId))),
  ))


  val DefaultSchema = Schema(Query, Some(Mutation))
}