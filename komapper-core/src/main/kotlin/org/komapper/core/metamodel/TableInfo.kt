package org.komapper.core.metamodel

interface TableInfo {
    fun tableName(): String
    fun catalogName(): String
    fun schemaName(): String
}
