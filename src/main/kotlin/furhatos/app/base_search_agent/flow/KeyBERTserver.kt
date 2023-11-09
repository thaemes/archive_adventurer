package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.KEYBERTIP
import furhatos.app.base_search_agent.KEYBERTPORT
import furhatos.app.base_search_agent.KEYBERT_THRESHOLD
import org.json.JSONArray
import java.io.*
import java.net.ConnectException
import java.net.Socket

class KeyBERTserver {

    private val processBuilder = ProcessBuilder(
        "python3", "assets/KeybertServer/kb_serv.py"
    )
    private lateinit var process: Process
    private lateinit var socket: Socket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: PrintWriter
    private lateinit var reader: BufferedReader

    fun startKeyBERTServer() {
        //println("*** starting KeyBERT server ")
        if (!::process.isInitialized) {
            process = processBuilder.start()
            println("*** Started KeyBERT server")
        } else if (!process.isAlive) {
            process = processBuilder.start()
        } else {
            println("*** Seems like KeyBert process is already running")
        }
    }

    fun connectKeyBERTServer() {
        println("*** KeyBert: trying to create socket")
        try {
            socket = Socket(KEYBERTIP, KEYBERTPORT)
            println("*** Created socket for KeyBERT")
        } catch (e: ConnectException) {
            println("*** Connect exception: could not connect to socket, is there a KeyBERT server running?")
            return
        } catch (e: java.lang.Exception) {
            println("*** Some exception occured while trying to set up socket to the KeyBERT server: ${e.message}")
            return
        }
        inputStream = socket.getInputStream()
        outputStream = PrintWriter(socket.getOutputStream(), true)
        reader = BufferedReader(InputStreamReader(inputStream))
    }

    fun keyBERTExtract(inp: String?): String? {
        return try {
            outputStream.println("${inp}\n")
            println("*** Sent the words to KeyBERT")
            val inp = JSONArray(reader.readLine())
            println("*** KeyBert found: " + inp.getJSONArray(0).getString(0))

            inp.getJSONArray(0).getString(0).toLowerCase()

        } catch (e: IOException) {
            println("*** Error occured while trying to extract with KeyBERT, maybe the socket is not connected? ${e.message}")
            null
        } catch (e: java.lang.Exception) {
            println("*** Some other exception occured, maybe there is not even a connection to any KeyBERT server made yet?")
            null
        }
    }

    fun keyBERTExtractMulti(incoming: String?): List<String>? {
        return try {
            val stringList = mutableListOf<String>()
            var in_l = incoming
            if (incoming != null) {
                if (incoming.contains("bijzondere voertuigen")) {
                    stringList.add("bijzondere voertuigen")
                    in_l = incoming.replace(Regex("bijzondere voertuigen"), "")
                }
                if (incoming.contains("bijzondere vliegtuigen")) {
                    stringList.add("bijzondere vliegtuigen")
                    in_l = incoming.replace(Regex("bijzondere vliegtuigen"), "")
                }
                if (incoming.contains("jonge dieren")) {
                    stringList.add("jonge dieren")
                    in_l = incoming.replace(Regex("jonge dieren"), "")
                }
                if (incoming.contains("loven en bieden")){
                    stringList.add("loven en bieden")
                    in_l = incoming.replace(Regex("loven en bieden"), "")
                }
            }
            outputStream.println("${in_l}\n")
            println("*** Sent the words: ${incoming} to multi KeyBERT")
            val inp = JSONArray(reader.readLine())

            for (i in 0 until inp.length()) {
                val innerArray = inp.getJSONArray(i)
                val string = innerArray.getString(0).toLowerCase()
                if (string.toLowerCase().contains("naar")) continue
                if (string.toLowerCase().contains("mij")) continue
                if (string.toLowerCase().equals("en")) continue
                //if (string.toLowerCase().equals("een")) continue
                if (string.toLowerCase().equals("je")) continue
                if (string.toLowerCase().contains("luchtvaart")) continue
                if (string.toLowerCase().contains("filmpje") || string.toLowerCase().contains("filmpjes")) continue
                if (string.toLowerCase().contains("heb") || string.toLowerCase().contains("hebben")) continue
                if (string.toLowerCase().contains("opzoek") || string.toLowerCase().contains("opzoeken")) continue
                if (string.toLowerCase().contains("zoek") || string.toLowerCase().contains("zoeken")) continue
                if (string.toLowerCase().contains("robot") || string.toLowerCase().contains("robots")) continue
                if (string.toLowerCase().contains("jaskier")) {
                    stringList.add("skiën")
                    continue
                }
                if (string.toLowerCase().contains("leeuw")) {
                    stringList.add("leeuwen")
                    println("leeuw hardoced")
                    continue
                }


                val number = innerArray.getDouble(1)
                if (number > KEYBERT_THRESHOLD && !(string.toLowerCase().equals("filmpjes"))) {
                    stringList.add(string)
                }
            }
            println("*** KeyBert found: ${stringList}")
            stringList
        } catch (e: IOException) {
            println("*** Error occured while trying to extract with KeyBERT, maybe the socket is not connected? ${e.message}")
            null
        } catch (e: java.lang.Exception) {
            println("*** Some other exception occured, maybe there is not even a connection to any KeyBERT server made yet?")
            null
        }
    }

    fun disconnectKeyBERTServer() {
        if (!::socket.isInitialized) {
            println("*** Socket was never initialized")
        } else if (socket.isClosed) {
            println("*** Socket was already closed")
            return
        } else {
            outputStream.println("***")
            outputStream.close()
            inputStream.close()
            reader.close()
            socket.close()
        }
        if (!::process.isInitialized) {
            println("*** Seems like KeyBERT was never run from here. Did you run it separately?")
        } else if (process.isAlive) {
            process.destroy()
        } else {
            println("*** Process was already destroyed")
        }
        println("*** closed and disconnected from KeyBERT server")
    }

}