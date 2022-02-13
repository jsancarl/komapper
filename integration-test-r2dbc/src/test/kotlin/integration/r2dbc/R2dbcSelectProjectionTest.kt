package integration.r2dbc

import integration.core.Address
import integration.core.address
import integration.core.department
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcSelectProjectionTest(private val db: R2dbcDatabase) {

    @Test
    fun selectSingleColumn() = inTransaction(db) {
        val a = Meta.address
        val streetList = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
        }
        assertEquals(listOf("STREET 1", "STREET 2"), streetList)
    }

    @Test
    fun selectSingleColumn_first() = inTransaction(db) {
        val a = Meta.address
        val value = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
                .first()
        }
        assertEquals("STREET 1", value)
    }

    @Test
    fun selectSingleColumn_null() = inTransaction(db) {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e)
                .where {
                    e.managerId.isNull()
                }
                .select(e.managerId)
        }
        assertTrue(list.all { it == null })
    }

    @Test
    fun selectPairColumns() = inTransaction(db) {
        val a = Meta.address
        val pairList = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(listOf(1 to "STREET 1", 2 to "STREET 2"), pairList)
    }

    @Test
    fun selectTripleColumns() = inTransaction(db) {
        val a = Meta.address
        val tripleList = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            tripleList
        )
    }

    @Test
    fun selectRecord() = inTransaction(db) {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, concat(a.street, " test"))
        }
        assertEquals(2, list.size)
        val record0 = list[0]
        assertEquals(1, record0[a.addressId])
        assertEquals("STREET 1", record0[a.street])
        assertEquals(1, record0[a.version])
        assertEquals("STREET 1 test", record0[concat(a.street, " test")])
        val record1 = list[1]
        assertEquals(2, record1[a.addressId])
        assertEquals("STREET 2", record1[a.street])
        assertEquals(1, record1[a.version])
        assertEquals("STREET 2 test", record1[concat(a.street, " test")])
    }

    @Test
    fun selectEntity() = inTransaction(db) {
        val a = Meta.address
        val e = Meta.employee
        val list: List<Address> = db.runQuery {
            QueryDsl.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun selectPairColumns_scalar() = inTransaction(db) {
        val d = Meta.department
        val e = Meta.employee
        val subquery = QueryDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val list = db.runQuery {
            QueryDsl.from(d)
                .orderBy(d.departmentId)
                .select(d.departmentName, subquery)
        }
        assertEquals(4, list.size)
        assertEquals("ACCOUNTING" to 3L, list[0])
        assertEquals("RESEARCH" to 5L, list[1])
        assertEquals("SALES" to 6L, list[2])
        assertEquals("OPERATIONS" to 0L, list[3])
    }

    @Test
    fun selectSingleNotNullColumn() = inTransaction(db) {
        val a = Meta.address
        val streetList: List<String> = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectNotNull(a.street)
        }
        assertEquals(listOf("STREET 1", "STREET 2"), streetList)
    }

    @Test
    fun selectPairNotNullColumns() = inTransaction(db) {
        val a = Meta.address
        val pairList: List<Pair<Int, String>> = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street)
        }
        assertEquals(listOf(1 to "STREET 1", 2 to "STREET 2"), pairList)
    }

    @Test
    fun selectTripleNotNullColumns() = inTransaction(db) {
        val a = Meta.address
        val tripleList: List<Triple<Int, String, Int>> = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            tripleList
        )
    }

    @Test
    fun selectSingleNotNullColumn_error() = inTransaction(db) {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.managerId)
            }
            Unit
        }
        println(ex.message)
    }

    @Test
    fun selectPairNotNullColumn_error() = inTransaction(db) {
        val e = Meta.employee
        val ex1 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.managerId, e.employeeId)
            }
            Unit
        }
        println(ex1.message)

        val ex2 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.employeeId, e.managerId)
            }
            Unit
        }
        println(ex2.message)
    }

    @Test
    fun selectTripleNotNullColumn_error() = inTransaction(db) {
        val e = Meta.employee
        val ex1 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.managerId, e.employeeId, e.addressId)
            }
            Unit
        }
        println(ex1.message)

        val ex2 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.employeeId, e.managerId, e.addressId)
            }
            Unit
        }
        println(ex2.message)

        val ex3 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.employeeId, e.addressId, e.managerId)
            }
            Unit
        }
        println(ex3.message)
    }
}