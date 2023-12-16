package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.CustomLogger
import furhatos.app.base_search_agent.nlu.*
import furhatos.app.base_search_agent.nlu.Number
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import furhatos.gestures.Gestures
import org.json.JSONObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
//import furhatos.app.base_search_agent.nlu.customLogging

var currentSet = KeywordCollection()
var cl = CustomLogger()


fun conversationalPrompt(): State = state(Init) {
    onEntry {
        if (currentSet.kws.size == 0) call(cl.customAsk("waar zal ik naar zoeken?"))//furhat.ask("waar zal ik naar zoeken?")
        else if (currentSet?.getSetSize() != 0 && currentSet.getSetSize()!! <= 3) goto(conversationalResult())
        else if (currentSet.cameFromSuggestion) {
            call(cl.customAsk("Zit daar een onderwerp tussen dat je interessant lijkt? Zo ja welke?"))
            //currentSet.cameFromSuggestion = false
        } else {
            if (currentSet.kws == currentSet.kws_prev) {
                currentSet.suggestionCounter++
                if (currentSet.suggestionCounter >= 1) goto(askSuggest())
                println(currentSet.suggestionCounter)
            }
            call(cl.customAsk("wat lijken je verder interessanten onderwerpen in een filmpje over ${currentSet.getHumanReadableLabels()}?"))
        }
    }

    this.onResponse<doNotKnow> {
        call(cl.addLog(who = "kid", it.text))
        println("came back!!!!!!!!!!!!!")
        furhat.gesture(Gestures.Smile)
        cl.customSay("oh, oke!")
        goto(askSuggest())
    }

    this.onResponse<provideOptions>{
        goto(askSuggest())
    }

    this.onResponse<Nee> {
        cl.customSay("oh jammer")
        if (currentSet.cameFromSuggestion) {
            currentSet.suggestionCounter = 2
        }
        goto(conversationalPrompt())
    }

    this.onResponse {
        currentSet.kws_prev = currentSet.kws//.toList().toMutableList()

        var newKWs = matchServ.extract(it.text.lowercase())
        currentSet.loadDataFromJson(newKWs)


        if (currentSet.kws.size == 0) {
            cl.customSay("ik verstond <break time=\"0.5s\"/> ${it.text}")
            cl.customSay("Daar zitten geen onderwerpen in die ik kenn")
            if(currentSet.cameFromSuggestion) goto(askSuggest(same = true))
            else goto(conversationalPrompt())
        }

        if (!currentSet.kws.all { it == null }) {
            currentSet.retrieveResultsWithCurrentKws()
        }

        val len = currentSet.getSetSize()
        println("@@@ Found ${len} video results")
        if (len == 0) goto(conversationalResult())
        else if (len!! <= 3) goto(conversationalResult())
        cl.customSay("ik heb ${getQuantifyWord(len)} filmpjes over ${currentSet.getHumanReadableLabels()}")
        goto(conversationalPrompt())
    }

    onExit {
        currentSet.cameFromSuggestion = false
    }
}


fun askSuggest(same : Boolean = false): State = state(Init) {
    onEntry {
        val t: JSONObject?
        val ext: MutableList<String>? = mutableListOf()
        var sortedList : List<String>

        if (!same) {
            t = getLinkedTopicsMulti(currentSet.getGTAAs())
            ext?.addAll(extractLinkedTopicsList(t)!!)
            ext?.removeAll(currentSet.getLabels().toSet())
            ext?.removeAll(currentSet.suggestedBefore.toSet())

            val groupedByCount = ext?.groupingBy { it }?.eachCount()
            sortedList =
                groupedByCount?.toList()?.sortedByDescending { (_, count) -> count }?.map { it.first }?.take(3)
                    ?: emptyList()
            val sortedMap = groupedByCount?.toList()?.sortedByDescending { (_, count) -> count }?.toMap()

            println("\n*** The most frequently occurring topics with their counts are (subset):")
            if (sortedMap != null) {
                sortedMap.entries.take(5).forEach { (word, count) -> println("   $word: $count") }
            }
            currentSet.suggestedBefore.addAll(sortedList)
            currentSet.suggestedLastTurn.clear()
            currentSet.suggestedLastTurn.addAll(sortedList)
            println("\n")
        }
        else {
            println(currentSet.suggestedLastTurn.toString())
            sortedList = currentSet.suggestedLastTurn
        }

        cl.customSay(
             "Ik heb wel een suggestie. "//<break time=\"0.3s\"/>"
            +"De filmpjes over ${currentSet.getHumanReadableLabels()}, gaan verder over bijvoorbeeld ${
                concatStrings(sortedList)
            }"
        )
        currentSet.cameFromSuggestion = true
        goto(conversationalPrompt())
    }
}

fun conversationalResult(): State = state(Init) {
    onEntry {
        when (val numberVideos = currentSet.getSetSize()) {
            0, null -> {
                if (currentSet.getHumanReadableLabels() == null || currentSet.getHumanReadableLabels() == "") {
                    cl.customSay("ik heb helaas geen filmpjes gevonden")
                } else {
                    cl.customSay("ik heb helaas geen filmpjes gevonden over ${currentSet.getHumanReadableLabels()}")
                }
                currentSet.stepBack()
                cl.customSay("Ik doe even een stapje terug")
                currentSet.cameFromSuggestion = false
                goto(conversationalPrompt())
            }
            1 -> {
                cl.customSay("ik heb 1 filmpje over ${currentSet.getHumanReadableLabels()}")
                cl.customSay("het filmpje heet ${currentSet.getSetVideos()?.map { it?.title }}")
                goto(askToWatch())
            }
            else -> {
                cl.customSay("ik heb ${numberVideos} filmpjes over ${currentSet.getHumanReadableLabels()}.")
                cl.customSay("de filmpjes heten ${concatStrings(currentSet.getSetVideos()?.map { it?.title })}.")
                goto(askToWatch())
            }
        }
    }
}

fun askToWatch(): State = state(Init) {
    onEntry {
        if (currentSet.getSetSize() == 1) call(cl.customAsk("wil je die zien?"))
        call(cl.customAsk("welk filmpje wil je zien? "))
    }
    onResponse<Number> {
        cl.customSay("oke, leuk! ")
        val number: Int
        number = when (it.intent.value?.toInt()) {
            -2 -> (currentSet.getSetSize()?.toInt()?.minus(2)!!)
            -1 -> (currentSet.getSetSize()?.toInt()?.minus(1)!!)
            else -> it.intent.value?.toInt()!! - 1
        }
        println("### about to watch: " + currentSet.getSetVideos()?.get(number)?.title)
        call(watchVideo(it.intent.value?.let { it1 -> currentSet.getSetVideos()?.get(number)?.link }))
    }
    onResponse<Ja> {
        call(watchVideo(currentSet.getSetVideos()?.get(0)?.link))
    }

    onResponse<Nee> {
        cl.customSay("oh jammer. Ik doe een stapje terug")
        currentSet.stepBack()
        goto(conversationalPrompt())
    }

    onResponse {
        val wd = kbserv.keyBERTExtract(it.text.toLowerCase()).toString()
        val titleList = currentSet.getSetVideos()?.map { it?.title }

        titleList?.forEachIndexed { index, title ->
            if (title?.let { wd.toLowerCase() in it.toLowerCase() } == true) {
                cl.customSay("Oke leuk!")
                println("about to watch: " + currentSet.getSetVideos()?.get(index)?.title)
                call(watchVideo(currentSet.getSetVideos()?.get(index)?.link))
                goto(Init)
            }
            else {
                cl.customSay("ik heb je niet begrepen.")
                if(currentSet.getSetSize() == 1) goto(askToWatch())
                else goto(conversationalPrompt())
            }
        }
    }
}