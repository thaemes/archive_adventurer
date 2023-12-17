package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.MATCHING_SERVER_IP
import furhatos.app.base_search_agent.MATCHING_SERVER_PORT
import furhatos.flow.kotlin.Flow
import furhatos.flow.kotlin.Furhat
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.state
import java.io.*
import java.net.Socket


class MatchingServer {
    lateinit var socket: Socket
    lateinit var writer: BufferedWriter
    lateinit var reader: BufferedReader

    fun close() {
        try {
            matchServ.writer.close()
            matchServ.reader.close()
            matchServ.socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun connectMatchServ(): State = state(Init) {
    onEntry {
        try {
            matchServ.socket = Socket(MATCHING_SERVER_IP, MATCHING_SERVER_PORT)
            matchServ.writer = BufferedWriter(OutputStreamWriter(matchServ.socket.getOutputStream()))
            matchServ.reader = BufferedReader(InputStreamReader(matchServ.socket.getInputStream()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun extractMatchServ(incoming: String?): State = state(Init) {
    onEntry {
        println("Attempting extraction")

        try {
            // Send data to the server
            matchServ.writer.write(incoming)
            matchServ.writer.newLine()  // Add a newline character
            matchServ.writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            terminate("".toString())
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
                    call(slowMatchingResponse())
                    // Wait for the second response with the matches
                    val secondResponse = matchServ.reader.readLine() ?: ""
                    println("Received second response: $secondResponse")
                    terminate(secondResponse.toString())
                }
                terminate(rec.toString())
            } else {
                println("Received an empty response")
                terminate("".toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            terminate("".toString())
        }
    }
}

fun closeMatchServ(): State = state(Init) {
    onEntry {
        try {
            matchServ.writer.close()
            matchServ.reader.close()
            matchServ.socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

