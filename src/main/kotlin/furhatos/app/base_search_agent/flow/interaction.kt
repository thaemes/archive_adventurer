package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.DataDelivery
import furhatos.app.base_search_agent.PORT
import furhatos.app.base_search_agent.SPEECH_DONE
import furhatos.app.base_search_agent.nlu.ThesaurusKeyword
import furhatos.event.senses.SenseSkillGUIConnected
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.Voice
import furhatos.skills.HostedGUI
import furhatos.util.Gender
import furhatos.util.Language
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.text.SimpleDateFormat
import java.util.*


val GUI = HostedGUI("Gui", "assets/gui2b", PORT)
val CLICK_BUTTON = "ClickButton"

val kbserv = KeyBERTserver()
val matchServ = MatchingServer()


// Starting state, before our GUI has connected.
val NoGUI: State = state(null) {
    onEntry {
        users.setSimpleEngagementPolicy(1.5,1)
    }
    onEvent<SenseSkillGUIConnected> {
        goto(GUIConnected)
    }

    //onButton("test inheritance"){}

}

val GUIConnected = state(NoGUI) {
    onEntry {
        // Pass data to GUI
        println("A GUI connected")
        if(!matchServ.isConnected) call(connectMatchServ())
        goto(Init)
    }

    onEvent(CLICK_BUTTON) {
        println("Rec'd annotated logs at furhat side: " + it.get("data"))
        writeAnnotatedLog(it.get("data").toString())
//        println("about to send ## ")
//        currentSet.reset() // added 19 Dec
//        call(cl.reset())   // added 19 Dec
//        send(DataDelivery(buttons = null, inputFields = null, messagesLog = null, videoUrl = null))
//        println("sent empty to GUI")
    }
}


val Init: State = state(GUIConnected) {
    onEntry {
        furhat.setInputLanguage(Language.DUTCH)
        val v = Voice(
            gender = Gender.MALE, language = Language.DUTCH,
            rate = 1.0
            //rate = 1.65
        )
        furhat.setVoice(v)
        furhat.param.noSpeechTimeout = 12000
        furhat.param.endSilTimeout = 1200
        //furhat.attendAll()

        println("init executed")
        dialogLogger.startSession()
    }

    onButton("blank") {
        furhat.character = "blank"
    }

    onUserEnter {
        furhat.attend(it)
        println("user entered")
    }

//    onUserLeave {
//        //furhat.attendNobody()
//        //reentry()
//    }

//    onReentry {
//    }

//    onButton("Start Conversational!", color = Color.Green) {
//        goto(conversationalPrompt())
//    }

    onButton("try simplified"){
        goto(conversationalSimplified())
    }

    onButton("force LANG", color = Color.Green, section = Section.RIGHT) {
        furhat.setInputLanguage(Language.DUTCH)
    }
    onButton("Start snappy") {
        goto(conversationalPromptSnap())
    }

//    onButton("blank face") {

//    }

    onButton("Skip next utterance") {
        furhat.say("", abort = true)
    }

    onButton("Stop attending") {
        furhat.attendNobody()
    }

    onButton("Reset current set conversational", color = Color.Green) {
        currentSet.reset()
        call(cl.reset())
        state.resetState()
        dialogLogger.endSession()
        dialogLogger.startSession()
    }

    onButton("Start logger", color = Color.Yellow) {
        dialogLogger.startSession()
    }

    onButton("Stop logger") {
        dialogLogger.endSession()
    }

    onButton("Connect matching server") {
        call(connectMatchServ())
        //call(extractMatchServ("tijger"))
    }

    onButton("Test matching server") {
        var res = call(extractMatchServ("bananen", false))
        println(res)
    }

    onButton("Dump log", color=Color.Yellow) {
        println(cl.getLog())
        send(DataDelivery(buttons = null, inputFields = null, messagesLog = listOf(cl.getLog()), videoUrl = null))
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

        send(
            DataDelivery(
                buttons = null,
                inputFields = null,
                messagesLog = listOf(messagesJson.toString()),
                videoUrl = null
            )
        )
        println("sent: " + messagesJson)
    }

    onButton("Trigger videoMode") {
        send(
            DataDelivery(
                buttons = null,
                inputFields = null,
                messagesLog = null,
                videoUrl = listOf("https://www.openbeelden.nl/files/12/36/1236793.WEEKNUMMER322-HRE0001DB16_2601000_2704960.mp4")
            )
        )
    }

    onButton("trigger suggestion") {
        goto(askSuggestSnap())
    }

    onButton("get SPARQL new") {
        state.addKeyword(ThesaurusKeyword("28650", "onthullingen", 1.0))
        state.addKeyword(ThesaurusKeyword("27753", "monumenten", 1.0))
        call(simpleSuggest())
    }

//    onButton("test levenstein") {
//        val words = listOf("tentoonstellingen", "jonge dieren", "keuringen")
//        val input = "Ja die ene over de jonge dieren"
//        val closestMatch = findClosestMatch(words, input)
//        println("Closest match: $closestMatch")
//    }
}


fun watchVideo(link: String?) = state(Init) {
    onEntry {
        if (link == null) {
            println("%%% received empty video link!")
            currentSet.stepBack()
            goto(quickResult())
        } else {
            println("%%% Rec'd. video link: $link")
            send(DataDelivery(buttons = null, inputFields = null, messagesLog = null, videoUrl = listOf(link)))
            send(SPEECH_DONE)
        }
        terminate()
    }
}

fun writeAnnotatedLog(input: String)  {

    try {
        // Create a File object with the given file path
        val file = File(generateFilename())

        // Create a FileWriter and BufferedWriter to write to the file
        val fileWriter = FileWriter(file)
        val bufferedWriter = BufferedWriter(fileWriter)

        // Write the JSON string to the file
        bufferedWriter.write(input)

        // Close the BufferedWriter to flush and save the changes
        bufferedWriter.close()

        println("JSON data has been written to $file")
    } catch (e: Exception) {
        println("Error writing JSON data to file: ${e.message}")
    }

}

fun generateFilename(): String {
    val currentDate = Date()

    println("generate file name in action")
    // Get the current day of the month and month in the desired format
    val dayMonthFormat = SimpleDateFormat("dd-MM-yyyy")
    val dayMonth = dayMonthFormat.format(currentDate)

    // Get the current time in the desired format
    val timeFormat = SimpleDateFormat("HH-mm-ss")
    val time = timeFormat.format(currentDate)

    // Generate a random 4-digit string
    val random = Random()
    val randomString = String.format("%04d", random.nextInt(10000))

    // Combine the components to create the filename
    val filename = "output/$dayMonth@$time#$randomString.json"

    return filename
}