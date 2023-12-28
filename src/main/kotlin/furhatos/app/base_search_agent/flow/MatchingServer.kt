package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.MATCHING_SERVER_IP
import furhatos.app.base_search_agent.MATCHING_SERVER_PORT
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.state
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException

class MatchingServer {
    lateinit var socket: Socket
    lateinit var writer: BufferedWriter
    lateinit var reader: BufferedReader
    var isConnected = false

    fun close() {
        if (!matchServ.isConnected) {
            println("Tried disconnecting from Match server, but was not connected.")
        } else {
            try {
                matchServ.writer.close()
                matchServ.reader.close()
                matchServ.socket.close()
                isConnected = false
                println("closed connection to matching server")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun connectMatchServ(): State = state(Init) {
    onEntry {
        try {
            matchServ.socket = Socket(MATCHING_SERVER_IP, MATCHING_SERVER_PORT)
            matchServ.writer = BufferedWriter(OutputStreamWriter(matchServ.socket.getOutputStream()))
            matchServ.reader = BufferedReader(InputStreamReader(matchServ.socket.getInputStream()))
            matchServ.isConnected = true
            println("Connected to Matching server")

        } catch (e: Exception) {
            println("ERROR connecting to Matching server")
            //e.printStackTrace()
        }
        terminate()
    }
}

fun extractMatchServ(incoming: String?, quiet: Boolean): State = state(Init) {
    onEntry {
        println("Attempting extraction on ${incoming}")

        if (!matchServ.isConnected) {
            println("Tried extraction, but the matchserv was not connected")
            terminate(null)
        }

        if (incoming == null) {
            println("Requesting matching serv. but the input incoming was null")
            terminate(null)
        }

        try {
            // Send data to the server
            matchServ.writer.write(incoming)
            matchServ.writer.newLine()  // Add a newline character
            matchServ.writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            terminate(null)
        }
        try {
            // Read response from the server
            var rec = matchServ.reader.readLine() ?: ""
            println("   Received from server: " + rec)

            if (rec.isNotEmpty()) {
                // Check if the response is "Initiating slow matching"
                if (rec.contains("!slow matching")) {
                    // Call your function for handling slow matching initiation
                    call(slowMatchingResponse(false, quiet))
                    // Wait for the second response with the matches
                    rec = matchServ.reader.readLine() ?: ""
                    println("   Received second response: $rec")
                    call(slowMatchingResponse(true, quiet))
                }
                if (rec.contains("!no match found")) {
                    terminate(null)
                }
                terminate(rec)
            } else {
                println("   Received an empty response")
                terminate(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            terminate(null)
        }
    }
}


fun flushMatchServer(): State = state(Init) {
    onEntry {
        try {
            matchServ.socket.soTimeout = 1000
            val rec = matchServ.reader.readLine() ?: ""
            println("   Flushing matching server: $rec")
        } catch (e: SocketTimeoutException) {
            println("Read timed out after 5 seconds")
            terminate()
        } catch (e: Exception) {
            e.printStackTrace()
            terminate()
        }
    }
}