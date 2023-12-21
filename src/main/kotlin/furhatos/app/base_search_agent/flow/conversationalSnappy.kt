package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.nlu.*
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import org.json.JSONObject


fun conversationalPromptSnap(): State = state(Init) {
    onEntry {
        if (currentSet.kws.size == 0) {
            random(
                { call(cl.customAsk("Waar zal ik naar zoeken?")) },
                { call(cl.customAsk("Welk onderwerp zullen we naar zoeken?")) }
            )
        } else if (currentSet.kws.size >= 2) goto(quickResult())
        else if (currentSet.cameFromSuggestion) {
            random(
                { call(cl.customAsk("Zit daar een onderwerp tussen dat je interessant lijkt?")) },
                { call(cl.customAsk("Zit daar iets interessants tussen?")) }
            )
            //currentSet.cameFromSuggestion = false
        } else {
            if (currentSet.kws == currentSet.kws_prev) {
                currentSet.suggestionCounter++
                if (currentSet.suggestionCounter >= 1) goto(askSuggestSnap())
                println(currentSet.suggestionCounter)
            }
            random(
                //{ call(cl.customAsk("Wat lijken je verder interessanten onderwerpen in een filmpje over ${currentSet.getHumanReadableLabels()}?")) },
                { call(cl.customAsk("Waar moet het verder over gaan?")) }
            )
        }
    }

    this.onResponse<doNotKnow> {
        call(cl.customResponse(it.text))
        furhat.gesture(Gestures.Smile)
        call(cl.customSay("oh, oke!"))
        goto(askSuggestSnap())
    }

    this.onResponse<provideOptions> {
        call(cl.customResponse(it.text))
        goto(askSuggestSnap())
    }

    this.onResponse<Nee> {
        call(cl.customResponse(it.text))
        call(cl.customSay("Oh jammer"))
        if (currentSet.cameFromSuggestion) {
            currentSet.suggestionCounter = 2
        }
        goto(conversationalPromptSnap())
    }

    this.onResponse {
        call(cl.customResponse(it.text))
        currentSet.kws_prev = currentSet.kws
        var newKWs = call(extractMatchServ(it.text.lowercase(), false))
        println("** just got newKWs:$newKWs")
        currentSet.loadDataFromJson(newKWs.toString())

        if (currentSet.kws.size == 0) {
            call(cl.customSay("Ik verstond ${it.text}"))
            call(cl.customSay("Daar zitten geen onderwerpen in die ik kenn. "))
            if (currentSet.cameFromSuggestion) goto(askSuggestSnap(same = true))
            else goto(conversationalPromptSnap())
        }

        if(currentSet.cameFromSuggestion) {
            val match = findClosestMatch(currentSet.suggestedLastTurn , it.text.lowercase() )
            val newKw = call(extractMatchServ(match, true))
            currentSet.loadDataFromJson(newKw.toString())
        }

        if (!currentSet.kws.all { it == null }) {
            currentSet.retrieveResultsWithCurrentKws()
        }

        val len = currentSet.getSetSize()
        println("@@@ Found ${len} video results")
        if (len == 0) goto(quickResult())
        else if (len!! <= 3) goto(quickResult())
        random(
            //{ call(cl.customSay("Ik heb ${getQuantifyWord(len)} filmpjes gevonden over ${currentSet.getHumanReadableLabels()}")) },
            { call(cl.customSay("Ik heb ${getQuantifyWord(len)} filmpjes gevonden.")) }
        )
        goto(conversationalPromptSnap())
    }

    onExit {
        currentSet.cameFromSuggestion = false
    }
}

fun askSuggestSnap(same: Boolean = false): State = state(Init) {
    onEntry {
        val currentSetGTAAs: JSONObject?
        val potentialOtherGTAAs: MutableList<String>? = mutableListOf()
        var sortedList: List<String>

        if (currentSet.getGTAAs().isEmpty()) {
            println("triggered suggestion while empty gtaa list!!!")
            call(cl.customAsk("Wat denk je van tijgers, of architectuur, of bananen?"))
            currentSet.cameFromSuggestion = true
            currentSet.suggestedLastTurn = mutableListOf("tijgers", "architectuur", "bananen")
            goto(conversationalPromptSnap())
        }

        if (!same) {
            currentSetGTAAs = getLinkedTopicsMulti(currentSet.getGTAAs())
            potentialOtherGTAAs?.addAll(extractLinkedTopicsList(currentSetGTAAs)!!)
            potentialOtherGTAAs?.removeAll(currentSet.getLabels().toSet())
            potentialOtherGTAAs?.removeAll(currentSet.suggestedBefore.toSet())

            val groupedByCount = potentialOtherGTAAs?.groupingBy { it }?.eachCount()
            sortedList =
                groupedByCount?.toList()?.sortedByDescending { (_, count) -> count }?.map { it.first }?.take(3)
                    ?: emptyList()
            val sortedMap = groupedByCount?.toList()?.sortedByDescending { (_, count) -> count }?.toMap()

            println("\n*** The most frequently occurring topics with their counts are (subset):")
            if (sortedMap != null) {
                sortedMap.entries.take(5).forEach { (word, count) -> println("   $word: $count") }
                if (sortedMap.isEmpty()) {
                    //call(cl.customSay("Ik heb geen suggesties meer. Sorry."))
                    goto(quickResult())
                }
            }
            currentSet.suggestedBefore.addAll(sortedList)
            currentSet.suggestedLastTurn.clear()
            currentSet.suggestedLastTurn.addAll(sortedList)
            println("\n")
        } else {
            println(currentSet.suggestedLastTurn.toString())
            sortedList = currentSet.suggestedLastTurn
        }
        currentSet.suggestedLastTurn = sortedList.toMutableList()  /// DEZE ADDED
        call(cl.customSay("De filmpjes gaan ook over bijvoorbeeld ${concatStrings(sortedList)}"))
        currentSet.cameFromSuggestion = true
        goto(conversationalPromptSnap())
    }
}

fun suggestionResponse(): State = state(Init) {
    onEntry {
        random(
            { call(cl.customAsk("Zit daar een onderwerp tussen dat je interessant lijkt?")) },
            { call(cl.customAsk("Zit daar iets interessants tussen?")) }
        )
    }
    onResponse {
       // findClosestMatch()
    }
}

fun quickResult(): State = state(Init) {
    onEntry {
        random(
            {call(cl.customSay("Hier is een filmpje!"))},
            {call(cl.customSay("Laten we kijken!"))}
        )
        call(watchVideo(currentSet.getRandomVideo()?.link))
    }
}


//fun conversationalResult(): State = state(Init) {
//    onEntry {
//        when (val numberVideos = currentSet.getSetSize()) {
//            0, null -> {
//                if (currentSet.getHumanReadableLabels() == null || currentSet.getHumanReadableLabels() == "") {
//                    call(cl.customSay("Ik heb helaas niks gevonden"))
//                } else {
//                    call(cl.customSay("Ik heb helaas geen filmpjes gevonden over ${currentSet.getHumanReadableLabels()}"))
//                }
//                currentSet.stepBack()
//                call(cl.customSay("Ik doe even een stapje terug."))
//                currentSet.cameFromSuggestion = false
//                goto(conversationalPrompt())
//            }
//
//            1 -> {
//                call(cl.customSay("Ik heb 1 filmpje over ${currentSet.getHumanReadableLabels()}"))
//                call(cl.customSay("Het filmpje heet ${currentSet.getSetVideos()?.map { it?.title }}"))
//                goto(askToWatch())
//            }
//
//            else -> {
//                call(cl.customSay("Ik heb ${numberVideos} filmpjes over ${currentSet.getHumanReadableLabels()}."))
//                call(cl.customSay("De filmpjes heten ${concatStrings(currentSet.getSetVideos()?.map { it?.title })}."))
//                goto(askToWatch())
//            }
//        }
//    }
//}

//fun askToWatch(): State = state(Init) {
//    onEntry {
//        if (currentSet.getSetSize() == 1) call(cl.customAsk("wil je die zien?"))
//        call(cl.customAsk("Welk filmpje wil je zien?"))
//    }
//
//    onResponse<Number> {
//        call(cl.customResponse(it.text))
//        call(cl.customSay("oke, leuk! "))
//        val number: Int = when (it.intent.value?.toInt()) {
//            -2 -> (currentSet.getSetSize()?.toInt()?.minus(2)!!)
//            -1 -> (currentSet.getSetSize()?.toInt()?.minus(1)!!)
//            else -> it.intent.value?.toInt()!! - 1
//        }
//        println("### about to watch: " + currentSet.getSetVideos()?.get(number)?.title)
//        call(watchVideo(it.intent.value?.let { it1 -> currentSet.getSetVideos()?.get(number)?.link }))
//    }
//
//    onResponse<Ja> {
//        call(cl.customResponse(it.text))
//        call(cl.customSay("Oke, leuk!"))
//        call(watchVideo(currentSet.getSetVideos()?.get(0)?.link))
//    }
//
//    onResponse<Nee> {
//        call(cl.customResponse(it.text))
//        call(cl.customSay("oh jammer. Ik doe een stapje terug"))
//        currentSet.stepBack()
//        goto(conversationalPrompt())
//    }
//
//    onResponse {
//        call(cl.customResponse(it.text))
//        call(cl.customAsk("Kun je het nummer van de video noemen?"))
//        reentry()
//    }
////        call(cl.customResponse(it.text))
////        val wd = kbserv.keyBERTExtract(it.text.toLowerCase()).toString()
////        val titleList = currentSet.getSetVideos()?.map { it?.title }
////
////        titleList?.forEachIndexed { index, title ->
////            if (title?.let { wd.toLowerCase() in it.toLowerCase() } == true) {
////                call(cl.customSay("Oke leuk!"))
////                println("about to watch: " + currentSet.getSetVideos()?.get(index)?.title)
////                call(watchVideo(currentSet.getSetVideos()?.get(index)?.link))
////                goto(Init)
////            } else {
////                call(cl.customSay("ik heb je niet begrepen."))
////                if (currentSet.getSetSize() == 1) goto(askToWatch())
////                else goto(conversationalPrompt())
////            }
////        }
////    }
//}
