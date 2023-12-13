package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.MATCHING_SERVER_IP
import furhatos.app.base_search_agent.MATCHING_SERVER_PORT
import java.io.*
import java.net.Socket


class MatchingServer {

    private lateinit var socket: Socket
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader

    fun connect() {
        try {
            socket = Socket(MATCHING_SERVER_IP, MATCHING_SERVER_PORT)
            writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun extract(incoming: String?): String {
        println("Attempting extraction")

        try {
            // Send data to the server
            writer.write(incoming)
            writer.newLine()  // Add a newline character
            writer.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }

        try {
            // Read response from the server
            val rec = reader.readLine() ?: ""
            println("received from server: "+rec)
            if (rec.isNotEmpty()) {
                println("Received response: $rec")
                return rec
            } else {
                println("Received an empty response")
                return ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }



    fun close() {
        try {
            writer.close()
            reader.close()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}