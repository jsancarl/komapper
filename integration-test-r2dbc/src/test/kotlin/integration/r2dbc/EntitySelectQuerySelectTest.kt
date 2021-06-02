package integration.r2dbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl

@ExtendWith(Env::class)
class EntitySelectQuerySelectTest(private val db: R2dbcDatabase) {

    @Test
    fun single() = inTransaction(db) {
        val a = Address.meta
        val street = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq 1 }
                .asSqlQuery().select(a.street).first()
        }
        assertEquals("STREET 1", street)
    }

    @Test
    fun pair() = inTransaction(db) {
        val a = Address.meta
        val (id, street) = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId eq 1 }
                .asSqlQuery().select(a.addressId, a.street).first()
        }
        assertEquals(1, id)
        assertEquals("STREET 1", street)
    }
}