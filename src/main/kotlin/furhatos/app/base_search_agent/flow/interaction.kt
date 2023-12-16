package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.DataDelivery
import furhatos.app.base_search_agent.PORT
import furhatos.app.base_search_agent.SPEECH_DONE
import furhatos.app.base_search_agent.nlu.Number
import furhatos.event.senses.SenseSkillGUIConnected
import furhatos.flow.kotlin.*
import furhatos.flow.kotlin.voice.Voice
import furhatos.skills.HostedGUI
import furhatos.util.Gender
import furhatos.util.Language


//val GUI = HostedGUI("ExampleGUI", "assets/exampleGui", PORT)
val kbserv = KeyBERTserver()

val Init: State = state(null) {
    onEntry {
        val v = Voice(
            gender = Gender.MALE, language = Language.DUTCH,
            rate = 1.0
            //rate = 1.65
        )
        //furhat.setVoice(v)
        furhat.setInputLanguage(Language.DUTCH)
        furhat.param.noSpeechTimeout = 12000
        furhat.param.endSilTimeout = 2000
        kbserv.startKeyBERTServer()
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
    onButton("Stop logger"){
        dialogLogger.endSession()
    }

    onButton("start PROCESSBUILDER KeyBERT server", color = Color.Red, section = Section.RIGHT) {
        kbserv.startKeyBERTServer()
    }
    onButton("connect to KeyBERT server", color = Color.Red, section = Section.RIGHT) {
        kbserv.connectKeyBERTServer()
    }

    onButton("test KeyBERT server connection", color = Color.Red, section = Section.RIGHT) {
        val a = kbserv.keyBERTExtract("ik leef")
        println(a.toString())
        if(a!= null) furhat.say("Ik ben verbonden!")
    }

    onButton("close KeyBERT socket and server", color = Color.Red, section = Section.RIGHT) {
        kbserv.disconnectKeyBERTServer()
    }

    /*onButton("test Narrower", color = Color.Yellow) {
        println(isNarrower("28912", "28684"))
    }

    onButton("test double matching") {
        println(extractGTAAMulti2(listOf(getGTAAPartial("verkeerspoli")))?.get(0)?.GTAA)
    }
    */
}

/*
val numtester: State = state(Init) {
    onEntry {
        furhat.listen()
    }
    onResponse<Number> {
        println(it.intent.value)
    }
}
*/


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
