package com.example.harter

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonClass
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty


fun main() {
    embeddedServer(Netty, 8080){
        verifyTwo()
    }.start(false)
}

fun Application.verifyOne(){
    routing {
        get {
            call.respondText { "Hello" }
        }
        post("/verify") {
            call.respond("Verified response")
        }
    }
}


fun Application.verifyTwo(){


    //install global handler, as the name suggests it shows "Status pages" (is this limited to this endpoint?)
    install(StatusPages){
        exception<Throwable>{e ->
            call.respondText(e.localizedMessage, ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
    }

    install(ContentNegotiation){
        moshi()  //customize moshi in lambda optionally
    }

    routing {
        post("/verify") {
            call.respond(VerifyResponse("Good"))    //cant be parsed. in order to even show error, StatusPages feature is needed
        }
    }
}

//@JsonClass(generateAdapter = true)    NOTE: this is optional - and helps avoid reflection. commented out to prove it json parsing works regardless
data class VerifyResponse(val status: String)