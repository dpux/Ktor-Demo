package com.example

import com.ryanharter.ktor.moshi.moshi
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
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty


//config options:https://ktor.io/servers/configuration.html
fun main() {
   val env = applicationEngineEnvironment {
       module {
           router()
       }

       //can have multiple connectors
       //private api
       connector {
           host = "127.0.0.1"
           port = 9090
       }

       //public api
       connector {
           host = "0.0.0.0"
           port = 8080
       }

       /*sslConnector(
           keyStore = keyStore,
           keyAlias = "mykey",
           keyStorePassword = { "changeit".toCharArray() },
           privateKeyPassword = { "changeit".toCharArray() }) {
           port = 9091
           keyStorePath = keyStoreFile.absoluteFile
       }*/
   }


    //other engines available are (look for imports for each in gradle):
//    For the server: Netty, Jetty, Tomcat, CIO, TestEngine - or write a custom one
//    For the client: ApacheEngine, JettyHttp2Engine, CIOEngine, TestHttpClientEngine
    embeddedServer(Netty, configure = {
        // Size of the queue to store [ApplicationCall] instances that cannot be immediately processed
        requestQueueLimit = 16
        // Do not create separate call event group and reuse worker group for processing calls
        shareWorkGroup = false
        // User-provided function to configure Netty's [ServerBootstrap]
        configureBootstrap = {
            // ...
        }
        // Size of the event group for accepting connections
        connectionGroupSize = parallelism / 2 + 1
        // Size of the event group for processing connections,
        // parsing messages and doing engine's internal work
        workerGroupSize = parallelism / 2 + 1
        // Size of the event group for running application code
        callGroupSize = parallelism
        // Timeout in seconds for sending responses to client
        responseWriteTimeoutSeconds = 10
    }) {
        // ...
    }.start(true)

}

fun Application.router(){     //without application context, method is useless, hence its an ext fn
    routing {
        get("/") {
            if (call.request.local.port == 9090) {
                call.respondText("Connected to public api")
            } else {
                call.respondText("Connected to private api")
            }
        }
    }
}
