package org.komapper.core.spi

import org.komapper.core.Dialect
import org.komapper.core.TemplateStatementBuilder

interface TemplateStatementBuilderFactory {
    fun create(dialect: Dialect, cache: Boolean = false): TemplateStatementBuilder
}