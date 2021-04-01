package org.komapper.core.config

/**
 * @property batchSize the batch size. This value is used for batch commands.
 * @property fetchSize the fetch size. See [java.sql.PreparedStatement.setFetchSize].
 * @property maxRows the max rows. See [java.sql.PreparedStatement.setMaxRows].
 * @property queryTimeoutSeconds the query timeout. See [java.sql.PreparedStatement.setQueryTimeout].
 */
data class JdbcConfig(
    val batchSize: Int? = null,
    val fetchSize: Int? = null,
    val maxRows: Int? = null,
    val queryTimeoutSeconds: Int? = null
) {
    infix operator fun plus(other: JdbcConfig): JdbcConfig {
        return JdbcConfig(
            other.fetchSize ?: this.fetchSize,
            other.maxRows ?: this.maxRows,
            other.queryTimeoutSeconds ?: this.queryTimeoutSeconds
        )
    }
}
