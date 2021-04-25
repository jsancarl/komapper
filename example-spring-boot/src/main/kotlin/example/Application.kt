package example

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.core.Database
import org.komapper.core.dsl.EntityQuery
import org.komapper.core.dsl.desc
import org.komapper.core.dsl.runQuery
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
@Transactional
class Application(private val database: Database) {

    @RequestMapping("/")
    fun list(): List<Message> {
        val m = Message.alias
        val query = EntityQuery.from(m).orderBy(m.id.desc())
        return database.runQuery { query }
    }

    @RequestMapping(value = ["/"], params = ["text"])
    fun add(@RequestParam text: String): Message {
        val message = Message(text = text)
        val query = EntityQuery.insert(Message.alias, message)
        val id = database.runQuery { query }
        return message.copy(id = id)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

@KmEntity
data class Message(
    @KmId @KmAutoIncrement val id: Int? = null,
    val text: String
) {
    companion object
}