import furhatos.app.base_search_agent.RANKING_SERVER_IP
import furhatos.app.base_search_agent.RANKING_SERVER_PORT
import furhatos.app.base_search_agent.nlu.ThesaurusKeyword
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.*
import java.net.Socket

class RankingServer {
    lateinit var socket: Socket
    lateinit var writer: BufferedWriter
    lateinit var reader: BufferedReader
    var isConnected = false

    fun connect() {
        try {
            socket = Socket(RANKING_SERVER_IP, RANKING_SERVER_PORT)
            writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            isConnected = true
            println("Connected to Ranking server")
        } catch (e: Exception) {
            println("ERROR connecting to Ranking server")
            e.printStackTrace()
        }
    }

    fun sendRequest(request: String) {
        println("    JSON REQUEST: "+request)
        if (!isConnected) {
            println("Not connected to Ranking server")
            return
        }

        try {
            writer.write(request)
            writer.newLine()
            writer.flush()
        } catch (e: IOException) {
            println("ERROR sending request to Ranking server")
            e.printStackTrace()
        }
    }

    fun receiveResponse(): String? {
        if (!isConnected) {
            println("Not connected to Ranking server")
            return null
        }

        return try {
            reader.readLine()
        } catch (e: IOException) {
            println("ERROR receiving response from Ranking server")
            e.printStackTrace()
            null
        }
    }

    fun close() {
        if (!isConnected) {
            println("Ranking server connection already closed.")
            return
        }

        try {
            writer.close()
            reader.close()
            socket.close()
            isConnected = false
            println("Closed connection to Ranking server")
        } catch (e: IOException) {
            println("ERROR closing Ranking server connection")
            e.printStackTrace()
        }
    }
}

