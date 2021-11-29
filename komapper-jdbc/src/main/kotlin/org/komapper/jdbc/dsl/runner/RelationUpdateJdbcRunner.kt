package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.runner.RelationUpdateRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class RelationUpdateJdbcRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationUpdateContext<ENTITY, ID, META>,
) : JdbcRunner<Int> {

    private val runner: RelationUpdateRunner<ENTITY, ID, META> = RelationUpdateRunner(context)

    override fun run(config: JdbcDatabaseConfig): Int {
        val clock = config.clockProvider.now()
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        val statement = runner.buildStatement(config, updatedAtAssignment)
        val executor = JdbcExecutor(config, context.options)
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}