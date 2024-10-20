package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.query.ProjectionType
import org.komapper.core.dsl.runner.PropertyMappingException
import org.komapper.r2dbc.R2dbcDataOperator

internal class R2dbcEntityMapper(strategy: ProjectionType, dataOperator: R2dbcDataOperator, row: Row) {
    private val valueExtractor = when (strategy) {
        ProjectionType.INDEX -> R2dbcIndexedValueExtractor(dataOperator, row)
        ProjectionType.NAME -> R2dbcNamedValueExtractor(dataOperator, row)
    }

    fun <E : Any> execute(metamodel: EntityMetamodel<E, *, *>, columns: List<ColumnExpression<*, *>> = emptyList(), forceMapping: Boolean = false): Map<PropertyMetamodel<*, *, *>, Any?> {
        val valueMap = mutableMapOf<PropertyMetamodel<*, *, *>, Any?>()
        val properties = metamodel.properties()
        for ((p, c) in properties.zip(columns.ifEmpty { properties })) {
            val value = try {
                valueExtractor.execute(c)
            } catch (e: Exception) {
                throw PropertyMappingException(metamodel.klass(), p.name, e)
            }
            valueMap[p] = value
        }
        return valueMap
    }
}
