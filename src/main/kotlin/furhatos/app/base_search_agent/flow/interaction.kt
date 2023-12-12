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


val GUI = HostedGUI("ExampleGUI", "assets/exampleGui", PORT)
val kbserv = KeyBERTserver()
val matchServ = MatchingServer()


val Init: State = state(null) {
    onEntry {
        val v = Voice(
            gender = Gender.MALE, language = Language.DUTCH,
            rate = 1.0
            //rate = 1.65
        )
        //furhat.setVoice(v)
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
    }

    onButton("Start logger", color = Color.Yellow) {
        dialogLogger.startSession()
    }
    onButton("Stop logger") {
        dialogLogger.endSession()
    }

    onButton("Test new matching server") {
        matchServ.connect()
        matchServ.extract("pony park slagharen")
//        matchServ.close()
    }
}


val NoGUI: State = state(null) {
    onEvent<SenseSkillGUIConnected> {
        println("*** a gui connected")
        goto(GUIConnected)
    }
}

val GUIConnected: State = state(NoGUI) {
    println("GUI has connected")
}

fun watchVideo(link: String?) = state(Init) {
    onEntry {
        if (link == null) {
            println("%%% received empty video link!")
            exit()
        } else {
            println("%%% Rec'd. video link: $link")
            send(DataDelivery(video = listOf(link)))
            send(SPEECH_DONE)
        }
        terminate()
    }

}
