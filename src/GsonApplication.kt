package com.example

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.text.DateFormat
import java.time.LocalDate


data class Model(val name: String, val items: List<Item>, val date: LocalDate = LocalDate.of(2018, 4, 13))
data class Item(val key: String, val value: String)

val model = Model("root", listOf(Item("A", "Apache"), Item("B", "Bing")))

fun main() {
    embeddedServer(Netty, port = 8080){
        gsonWork()
    }.run { start(wait = true) }
}

fun Application.gsonWork() {
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)   //see resources/logbback.xml for format
    install(ContentNegotiation) {
        gson {
            setDateFormat(DateFormat.LONG)
            setPrettyPrinting()
        }
    }


    routing {
        get("/v1") {
            call.respond(model)
        }

        get("/v1/item/{key}") {
            model.items.firstOrNull { it.key == call.parameters["key"] }?.let { call.respond(it) } ?: call.respond(
                HttpStatusCode.NotFound
            )

        }
    }

}