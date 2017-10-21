package nl.openvalue.http

import nl.openvalue.data.{Employee, Project}
import sangria.schema._

object SchemaDefinition {

  val ID = Argument("id", StringType)

  val Employee = ObjectType(
    "Employee", fields[Repository, Employee](
      Field("id", StringType, resolve = _.value.id),
      Field("firstName", StringType, resolve = _.value.firstName),
      Field("lastName", StringType, resolve = _.value.lastName),
      Field("emailAddress", StringType, resolve = _.value.emailAddress)
    )
  )

  val Project = ObjectType(
    "Project", fields[Repository, Project](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("employees", ListType(Employee), resolve = ctx => {
        val a = ctx.value.employeeIds.toList.map(ctx.ctx.getEmployee).flatten
      })
    )
  )

  val Query = ObjectType(
    "Query", fields[Repository, Unit](
      Field("employees", ListType(Employee), resolve = ctx => ctx.ctx.getEmployees),
      Field("employee", OptionType(Employee), arguments = ID :: Nil, resolve = ctx => ctx.ctx.getEmployee(ctx.arg(ID)))
    ))

  val DefaultSchema = Schema(Query)
}