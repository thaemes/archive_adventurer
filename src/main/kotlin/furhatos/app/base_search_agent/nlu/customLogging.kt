package furhatos.app.base_search_agent

import furhatos.app.base_search_agent.flow.Init
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CustomLogger {
    val messagesLogArray = JSONArray()
    private var counter = 1
    fun customSay(input: String): State = state(Init) {
        onEntry {
            addLog("robot", input)
            furhat.say(input)
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
            println("IN THE FUCKING CUSTOM ASSSK")
            furhat.ask(prompt)
            terminate()
        }
    }

    fun addLog(who: String, text: String): State = state(Init) {
        onEntry {
            val currentTime = LocalTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formattedTime = currentTime.format(formatter)
            println("IN THE FUCKING CUSTOM ADDLOG")
            val message = JSONObject().apply {
                put("who", who)
                put("id", counter)
                put("text", text)
                put("emojiId", "\uD83D\uDE0D")
                put("startTime", formattedTime)
            }
            println("about to add: "+ message.toString())
            messagesLogArray.put(message)
            counter++
            terminate()
        }
    }

    fun getLog() : String {
       val messagesJson = JSONObject().apply {
           put("messages", messagesLogArray)
       }
        return messagesJson.toString()
    }



}



