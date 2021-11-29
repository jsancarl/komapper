package org.komapper.core.dsl.query

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.DryRunResult
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.visitor.DefaultQueryVisitor
import org.komapper.core.dsl.visitor.QueryVisitor
import org.komapper.core.toDryRunResult

internal fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entity: ENTITY) {
    this.idProperties().forEach { p ->
        p.getter(entity) ?: error("The id value must not null. name=${p.name}")
    }
}

internal fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>
EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entities: List<ENTITY>) {
    entities.mapIndexed { index, entity ->
        this.idProperties().forEach { p ->
            p.getter(entity) ?: error("The id value must not null. index=$index, name=${p.name}")
        }
    }
}

fun Query<*>.dryRun(config: DatabaseConfig = DryRunDatabaseConfig): DryRunResult {
    val description = if (config is DryRunDatabaseConfig) {
        "This data was generated using DryRunDatabaseConfig. " +
            "To get more correct information, specify the actual DatabaseConfig instance."
    } else {
        ""
    }
    val runner = this.accept(DefaultQueryVisitor)
    val statement = try {
        runner.dryRun(config)
    } catch (throwable: Throwable) {
        return DryRunResult(
            sql = throwable.message ?: "",
            sqlWithArgs = throwable.message ?: "",
            throwable = throwable,
            description = description
        )
    }
    return statement.toDryRunResult(config.dialect, description)
}

fun <T, S> Query<T>.andThen(other: Query<S>): Query<S> = object : Query<S> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.andThenQuery(this@andThen, other)
    }
}

fun <T, S> Query<T>.map(transform: (T) -> S): Query<S> = object : Query<S> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.mapQuery(this@map, transform)
    }
}

fun <T, S> Query<T>.zip(other: Query<S>): Query<Pair<T, S>> = object : Query<Pair<T, S>> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.zipQuery(this@zip, other)
    }
}

fun <T, S> Query<T>.flatMap(transform: (T) -> Query<S>): Query<S> = object : Query<S> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.flatMapQuery(this@flatMap, transform)
    }
}

fun <T, S> Query<T>.flatZip(transform: (T) -> Query<S>): Query<Pair<T, S>> = object : Query<Pair<T, S>> {
    override fun <VISIT_RESULT> accept(visitor: QueryVisitor<VISIT_RESULT>): VISIT_RESULT {
        return visitor.flatZipQuery(this@flatZip, transform)
    }
}

fun <T> ListQuery<T>.first(): Query<T> = collect { it.first() }

fun <T> ListQuery<T>.firstOrNull(): Query<T?> = collect { it.firstOrNull() }