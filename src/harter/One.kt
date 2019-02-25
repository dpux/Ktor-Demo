package com.example.harter

import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonClass
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay


fun main() {
    embeddedServer(Netty, 8080){
        verifyOne()
//        verifyTwo()
    }.start(false)
}

fun Application.verifyOne(){


    install(Authentication){
        basic(name = "myauth1") {    //name helps different route use different auth style
            realm = "Ktor Server"   //basic auth spec
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)     //UserIdPrincipal is not required - we can return any Principal or null
                } else {
                    Temp("haha")
                }
            }
        }
    }
    routing {
        get {
            call.respondText { getHelloText() }    //NOTE: you can call a suspend fun directly!
        }
        post("/verify") {
            call.respond("Verified response")
        }
        authenticate("myauth1") {
            get("/auth") {   //to authenticate a route, wrap in a auth block
                val authData = call.authentication.principal
                if(authData is Temp) call.respondText { authData.dd }
                else call.respondText { authData.toString() }    //prints UserIdPrincipal(name=dd)
            }
        }

    }
}

data class Temp(val dd: String): Principal

suspend fun getHelloText(): String {
    delay(3000)  //client/browser will show "waiting" for 3 secs - as if its a n/w delay
    return "Deepak"
}

suspend fun getResponse(verifyRequest: VerifyRequest): VerifyResponse? = coroutineScope {

    val id = async {

    }
    id.await()
    VerifyResponse("")
}

fun Application.verifyTwo(){

    install(CallLogging)   //see resources/logbback.xml for format
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

        //curl -H "Content-Type: application/json" -d @/tmp/request.json http://localhost:8080/verify2   (wont work w/o -H)
        post("/verify2") {
//            print("got request")   //not needed if CallLogging feature is installed
            val request = call.receive<VerifyRequest>()
//            print(request)
            call.respond(request)   //return same data back for now- but after ser/dser on our side
        }
    }
}

//@JsonClass(generateAdapter = true)    NOTE: this is optional - and helps avoid reflection. commented out to prove it json parsing works regardless
data class VerifyResponse(val status: String)

data class VerifyRequest(val userId: String)