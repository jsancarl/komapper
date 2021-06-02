package integration.r2dbc

import integration.r2dbc.setting.Dbms
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.UniqueConstraintException
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.R2dbcEntityDsl

@ExtendWith(Env::class)
class EntityInsertMultipleQueryTest(private val db: R2dbcDatabase) {

    @Test
    fun test() = inTransaction(db) {
        val a = Address.meta
        val addressList = listOf(
            Address(16, "STREET 16", 0),
            Address(17, "STREET 17", 0),
            Address(18, "STREET 18", 0)
        )
        val ids = db.runQuery { R2dbcEntityDsl.insert(a).multiple(addressList) }.map { it.addressId }
        val list = db.runQuery {
            R2dbcEntityDsl.from(a).where { a.addressId inList ids }
        }.toList()
        assertEquals(addressList, list)
    }

    // TODO: the combination with returnGeneratedValues and rowsUpdated doesn't work in PostgreSQL 
    @Run(unless = [Dbms.POSTGRESQL, Dbms.MYSQL])
    @Test
    fun identity() = inTransaction(db) {
        val i = IdentityStrategy.meta
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val results1 = db.runQuery { R2dbcEntityDsl.insert(i).multiple(strategies) }
        val results2 = db.runQuery { R2dbcEntityDsl.from(i).orderBy(i.id) }.toList()
        assertEquals(results1, results2)
        Assertions.assertTrue(results1.all { it.id != null })
    }

    @Test
    fun createdAt_updatedAt() = inTransaction(db) {
        val p = Person.meta
        val personList = listOf(
            Person(1, "A"),
            Person(2, "B"),
            Person(3, "C")
        )
        val ids = db.runQuery { R2dbcEntityDsl.insert(p).multiple(personList) }.map { it.personId }
        val list = db.runQuery { R2dbcEntityDsl.from(p).where { p.personId inList ids } }.toList()
        for (person in list) {
            assertNotNull(person.createdAt)
            assertNotNull(person.updatedAt)
        }
    }

    @Test
    fun uniqueConstraintException() = inTransaction(db) {
        val a = Address.meta
        assertThrows<UniqueConstraintException> {
            db.runQuery {
                R2dbcEntityDsl.insert(
                    a
                ).multiple(
                    listOf(
                        Address(16, "STREET 16", 0),
                        Address(17, "STREET 17", 0),
                        Address(18, "STREET 1", 0)
                    )
                )
            }.let { }
        }
    }

    @Test
    fun onDuplicateKeyUpdate() = inTransaction(db) {
        val d = Department.meta
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate().multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            R2dbcEntityDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyUpdateWithKeys() = inTransaction(db) {
        val d = Department.meta
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate(d.departmentNo).multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            R2dbcEntityDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "KYOTO", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyUpdate_set() = inTransaction(db) {
        val d = Department.meta
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            R2dbcEntityDsl.insert(d).onDuplicateKeyUpdate().set { excluded ->
                d.departmentName set excluded.departmentName
            }.multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            R2dbcEntityDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyUpdateWithKey_set() = inTransaction(db) {
        val d = Department.meta
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query =
            R2dbcEntityDsl.insert(d)
                .onDuplicateKeyUpdate(d.departmentNo)
                .set { excluded ->
                    d.departmentName set excluded.departmentName
                }.multiple(listOf(department1, department2))
        db.runQuery { query }
        val list = db.runQuery {
            R2dbcEntityDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            listOf("DEVELOPMENT" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyIgnore() = inTransaction(db) {
        val d = Department.meta
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(1, 60, "DEVELOPMENT", "KYOTO", 1)
        val query = R2dbcEntityDsl.insert(d).onDuplicateKeyIgnore().multiple(listOf(department1, department2))
        val count = db.runQuery { query }
        assertEquals(1, count)
        val list = db.runQuery {
            R2dbcEntityDsl.from(d).where { d.departmentId inList listOf(1, 5) }.orderBy(d.departmentId)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun onDuplicateKeyIgnoreWithKeys() = inTransaction(db) {
        val d = Department.meta
        val department1 = Department(5, 50, "PLANNING", "TOKYO", 1)
        val department2 = Department(10, 10, "DEVELOPMENT", "KYOTO", 1)
        val query = R2dbcEntityDsl.insert(d)
            .onDuplicateKeyIgnore(d.departmentNo)
            .multiple(listOf(department1, department2))
        val count = db.runQuery { query }
        assertEquals(1, count)
        val list = db.runQuery {
            R2dbcEntityDsl.from(d).where { d.departmentNo inList listOf(10, 50) }.orderBy(d.departmentNo)
        }.toList()
        assertEquals(2, list.size)
        assertEquals(
            listOf("ACCOUNTING" to "NEW YORK", "PLANNING" to "TOKYO"),
            list.map { it.departmentName to it.location }
        )
    }

    @Test
    fun identity_onDuplicateKeyUpdate() = inTransaction(db) {
        val i = IdentityStrategy.meta
        val strategies = listOf(
            IdentityStrategy(null, "AAA"),
            IdentityStrategy(null, "BBB"),
            IdentityStrategy(null, "CCC")
        )
        val query = R2dbcEntityDsl.insert(i).onDuplicateKeyUpdate().multiple(strategies)
        val count = db.runQuery { query }
        assertEquals(3, count)
    }
}