package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.EntityUpdateStatementBuilder
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.option.VersionOption

internal class EntityUpdateQuerySupport<ENTITY : Any>(
    private val context: EntityUpdateContext<ENTITY>,
    private val option: VersionOption
) {

    fun preUpdate(config: DatabaseConfig, entity: ENTITY): ENTITY {
        val clock = config.clockProvider.now()
        return context.entityMetamodel.updateUpdatedAt(entity, clock)
    }

    fun <T> update(config: DatabaseConfig, execute: (JdbcExecutor) -> T): T {
        val executor = JdbcExecutor(config, option.asJdbcOption())
        return execute(executor)
    }

    fun postUpdate(entity: ENTITY, count: Int, index: Int? = null): ENTITY {
        if (context.entityMetamodel.versionProperty() != null) {
            checkOptimisticLock(option, count, index)
        }
        return if (!option.ignoreVersion) {
            context.entityMetamodel.incrementVersion(entity)
        } else {
            entity
        }
    }

    fun buildStatement(config: DatabaseConfig, entity: ENTITY): Statement {
        val builder = EntityUpdateStatementBuilder(config.dialect, context, entity, option)
        return builder.build()
    }
}
