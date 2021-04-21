package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.JdbcExecutor
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlUpdateStatementBuilder
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.QueryOptionConfigurator
import org.komapper.core.dsl.option.SqlUpdateOption
import org.komapper.core.dsl.scope.SetDeclaration
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.dsl.scope.WhereScope

interface SqlUpdateQuery<ENTITY : Any> : Query<Int> {
    fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQuery<ENTITY>
    fun where(declaration: WhereDeclaration): SqlUpdateQuery<ENTITY>
    fun option(configurator: QueryOptionConfigurator<SqlUpdateOption>): SqlUpdateQuery<ENTITY>
}

internal data class SqlUpdateQueryImpl<ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: SqlUpdateContext<ENTITY, ID, META>,
    private val option: SqlUpdateOption = SqlUpdateOption()
) : SqlUpdateQuery<ENTITY> {

    override fun set(declaration: SetDeclaration<ENTITY>): SqlUpdateQueryImpl<ENTITY, ID, META> {
        val scope = SetScope<ENTITY>().apply(declaration)
        val newContext = context.copy(set = context.set + scope)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlUpdateQueryImpl<ENTITY, ID, META> {
        val scope = WhereScope().apply(declaration)
        val newContext = context.copy(where = context.where + scope)
        return copy(context = newContext)
    }

    override fun option(configurator: QueryOptionConfigurator<SqlUpdateOption>): SqlUpdateQueryImpl<ENTITY, ID, META> {
        return copy(option = configurator.apply(option))
    }

    override fun run(config: DatabaseConfig): Int {
        if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
            error("Empty where clause is not allowed.")
        }
        val statement = buildStatement(config)
        val executor = JdbcExecutor(config, option.asJdbcOption())
        val (count) = executor.executeUpdate(statement)
        return count
    }

    override fun dryRun(config: DatabaseConfig): String {
        return buildStatement(config).sql
    }

    private fun buildStatement(config: DatabaseConfig): Statement {
        val builder = SqlUpdateStatementBuilder(config.dialect, context)
        return builder.build()
    }
}

interface SqlUpdateQueryBuilder<T : Any> {
    fun set(declaration: SetDeclaration<T>): SqlUpdateQuery<T>
}

internal class SqlUpdateQueryBuilderImpl<T : Any>(
    private val query: SqlUpdateQuery<T>
) : SqlUpdateQueryBuilder<T> {
    override fun set(declaration: SetDeclaration<T>): SqlUpdateQuery<T> {
        return query.set(declaration)
    }
}
