package org.komapper.jdbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.runner.SchemaCreateRunner
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcExecutor

internal class JdbcSchemaCreateRunner(
    private val context: SchemaContext,
) : JdbcRunner<Unit> {

    private val runner = SchemaCreateRunner(context)

    override fun run(config: JdbcDatabaseConfig) {
        val statements = runner.buildStatements(config)
        val executor = JdbcExecutor(config, context.options)
        executor.execute(statements) {
            if (!config.dialect.isTableExistsError(it) &&
                !config.dialect.isSequenceExistsError(it)
            ) {
                throw it
            }
        }
    }

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        return runner.dryRun(config)
    }
}