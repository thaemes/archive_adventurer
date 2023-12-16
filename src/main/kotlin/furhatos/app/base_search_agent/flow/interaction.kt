package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.DataDelivery
import furhatos.app.base_search_agent.PORT
import furhatos.app.base_search_agent.SPEECH_DONE
import furhatos.event.senses.SenseSkillGUIConnected
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.Voice
import furhatos.skills.HostedGUI
import furhatos.util.Gender
import furhatos.util.Language
import org.json.JSONArray
import org.json.JSONObject
import furhatos.flow.kotlin.*
import furhatos.records.Record



val GUI2 = HostedGUI("Gui2", "assets/gui2b", 1313)
val VARIABLE_SET = "VariableSet"
val CLICK_BUTTON = "ClickButton"

val kbserv = KeyBERTserver()
val matchServ = MatchingServer()


// Starting state, before our GUI has connected.
val NoGUI: State = state(null) {
    onEvent<SenseSkillGUIConnected> {
        goto(GUIConnected)
    }
}

val GUIConnected = state(NoGUI) {
    onEntry {
        // Pass data to GUI
        println("A GUI connected")
        goto(Init)
    }
}


val Init: State = state(GUIConnected) {
    onEntry {
        val v = Voice(
            gender = Gender.MALE, language = Language.DUTCH,
            rate = 1.0
            //rate = 1.65
        )
        furhat.setVoice(v)
        matchServ.connect()
        furhat.setInputLanguage(Language.DUTCH)
        furhat.param.noSpeechTimeout = 12000
        furhat.param.endSilTimeout = 2000
    }
    onReentry {
    }

    onButton("Start Conversational!", color = Color.Green) {
        goto(conversationalPrompt())
    }

    onButton("Start Query-Response") {
        goto(SearchQueryResponse())
    }

    onButton("Skip next utterance") {
        furhat.say("", abort = true)
    }

    onButton("Stop attending") {
        furhat.attendNobody()
    }

    onButton("Reset current set conversational", color = Color.Green) {
        currentSet.reset()
        call(cl.reset())
    }

    onButton("Start logger", color = Color.Yellow) {
        dialogLogger.startSession()
    }
    onButton("Stop logger") {
        dialogLogger.endSession()
    }

    onButton("Test new matching server") {
        matchServ.extract("pony's")
    }

    onButton("Dump log") {
        println(cl.getLog())
        send(DataDelivery(buttons = null, inputFields = null, messagesLog = listOf(cl.getLog()), videoUrl = null ))
        println("sent!")
    }

    onButton("Trigger reactmode") {
        val messagesArray = JSONArray()

        // Example message
        val message1 = JSONObject().apply {
            put("id", "message1")
            put("text", "Hallo, ik zoeken?")
            put("emojiId", "reaction1")
            put("who", "robot")
        }
        val message2 = JSONObject().apply {
            put("id", "message3")
            put("text", "Ik zoeken weet niet man. ")
            put("emojiId", "reaction1")
            put("who", "kid")
        }

        val messagesJson = JSONObject().apply {
            put("messages", messagesArray)
        }

        // Add the message to the array
        messagesArray.put(message1)
        messagesArray.put(message2)


        send(DataDelivery(buttons = null, inputFields = null, messagesLog = listOf(messagesJson.toString()), videoUrl = null))
        println("sent: "+ messagesJson)
    }

    onButton("Trigger videoMode"){
        send(DataDelivery(buttons=null, inputFields = null, messagesLog = null, videoUrl= listOf("https://www.openbeelden.nl/files/12/36/1236793.WEEKNUMMER322-HRE0001DB16_2601000_2704960.mp4")))

    }
}


fun watchVideo(link: String?) = state(Init) {
    onEntry {
        if (link == null) {
            println("%%% received empty video link!")
            exit()
        } else {
            println("%%% Rec'd. video link: $link")
            send(DataDelivery(buttons= null, inputFields=null, messagesLog = null, videoUrl = listOf(link)))
            send(SPEECH_DONE)
        }
        terminate()
    }
}

