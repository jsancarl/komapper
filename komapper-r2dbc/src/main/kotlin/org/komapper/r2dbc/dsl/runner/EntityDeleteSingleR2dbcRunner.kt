package org.komapper.r2dbc.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.EntityDeleteOptions
import org.komapper.core.dsl.runner.EntityDeleteSingleRunner
import org.komapper.r2dbc.R2dbcDatabaseConfig

internal class EntityDeleteSingleR2dbcRunner<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    context: EntityDeleteContext<ENTITY, ID, META>,
    options: EntityDeleteOptions,
    private val entity: ENTITY
) : R2dbcRunner<Unit> {

    private val runner: EntityDeleteSingleRunner<ENTITY, ID, META> =
        EntityDeleteSingleRunner(context, options, entity)

    private val support: EntityDeleteR2dbcRunnerSupport<ENTITY, ID, META> = EntityDeleteR2dbcRunnerSupport(context, options)

    override suspend fun run(config: R2dbcDatabaseConfig) {
        val (count) = delete(config)
        postDelete(count)
    }

    private suspend fun delete(config: R2dbcDatabaseConfig): Pair<Int, LongArray> {
        val statement = runner.buildStatement(config)
        return support.delete(config) { it.executeUpdate(statement) }
    }

    private fun postDelete(count: Int) {
        support.postDelete(count)
    }

    override fun dryRun(config: DatabaseConfig): Statement {
        return runner.dryRun(config)
    }
}