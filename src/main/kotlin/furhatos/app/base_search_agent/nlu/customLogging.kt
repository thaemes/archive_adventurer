package furhatos.app.base_search_agent

import furhatos.app.base_search_agent.flow.Init
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CustomLogger {
    private var messagesLogArray = JSONArray()
    private var counter = 1


    fun reset(): State = state(Init) {
        onEntry {
            messagesLogArray = JSONArray()
            counter = 1
            terminate()
        }
    }

    fun customSay(text: String): State = state(Init) {
        onEntry {
            call(addLog("robot", text))
            furhat.say(text)
            terminate()
        }
    }

    fun customListen(prompt: String): State = state(Init) {
        onEntry {
            call(addLog(who = "robot", prompt))
            furhat.listen()
            terminate()
        }
    }

    fun customAsk(prompt: String): State = state(Init) {
        onEntry {
            call(addLog(who = "robot", text = prompt))
            furhat.ask(prompt)
            terminate()
        }
    }

    fun customResponse(text: String): State = state(Init) {
        onEntry {
            call(addLog("kid", text))
            terminate()
        }

    }

    fun addLog(who: String, text: String): State = state(Init) {
        onEntry {
            val currentTime = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formattedTime = currentTime.format(formatter)

            val message = JSONObject().apply {
                put("who", who)
                put("id", counter)
                put("text", text)
                put("emojiId", "")
                put("startTime", formattedTime)
            }
            println("about to add: " + message.toString())
            messagesLogArray.put(message)
            counter++
            terminate()
        }
    }

    fun getLog(): String {
        val messagesJson = JSONObject().apply {
            put("messages", messagesLogArray)
        }
        return messagesJson.toString()
    }

}



