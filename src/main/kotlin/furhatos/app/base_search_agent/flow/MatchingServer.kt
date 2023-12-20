package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.MATCHING_SERVER_IP
import furhatos.app.base_search_agent.MATCHING_SERVER_PORT
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.state
import java.io.*
import java.net.Socket

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
    }
}

fun extractMatchServ(incoming: String?): State = state(Init) {
    onEntry {
        println("Attempting extraction")
        if (!matchServ.isConnected) {
            println("Tried extraction, but the matchserv was not connected")
            terminate()
        }
        if (incoming == null) {
            println("matching serv. incoming was null")
            terminate()
        }
        try {
            // Send data to the server
            matchServ.writer.write(incoming)
            matchServ.writer.newLine()  // Add a newline character
            matchServ.writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            terminate("")
        }
        try {
            // Read response from the server
            val rec = matchServ.reader.readLine() ?: ""
            println("received from server: " + rec)
            if (rec.isNotEmpty()) {
                println("Received response: $rec")
                // Check if the response is "Initiating slow matching"
                if (rec.contains("!slow matching")) {
                    // Call your function for handling slow matching initiation
                    call(slowMatchingResponse(false))
                    // Wait for the second response with the matches
                    val secondResponse = matchServ.reader.readLine() ?: ""
                    println("Received second response: $secondResponse")
                    call(slowMatchingResponse(true))
                    terminate(secondResponse)
                }
                terminate(rec)
            } else {
                println("Received an empty response")
                terminate("")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            terminate("")
        }
    }
}
