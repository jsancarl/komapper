package integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.jdbc.Database
import org.komapper.jdbc.dsl.SchemaDsl

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: Database) {

    private val metamodels = listOf(Aaa.meta, Bbb.meta, Ccc.meta, CompositeKeyAddress.meta)

    @Test
    fun create() {
        db.runQuery {
            SchemaDsl.create(metamodels)
        }
    }

    @Test
    fun drop() {
        db.runQuery {
            SchemaDsl.create(metamodels)
        }
        db.runQuery {
            SchemaDsl.drop(metamodels)
        }
    }
}