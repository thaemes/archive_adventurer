package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.nlu.*
import furhatos.app.base_search_agent.nlu.StateTracker
import furhatos.app.base_search_agent.nlu.doNotKnow
import furhatos.app.base_search_agent.useRanking
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.Response


/*

OVERAL WAAR WE Keywords opslaan moet het List<ThesaurusKeyword>,
OVERAL waar video, List<Video>

* */


var state = StateTracker()
const val suggestThreshold = 1
const val resultThreshold = 4



fun conversationalSimplified(): State = state(Init) {
    onEntry {
        println("### Entered conversationalSimplified()")

        if (state.keywordsCurrent.size == 0) {
            //raise(call(cl.customAsk("Waar zal ik naar zoeken?")) as Response<*>)//, conversationalSimplified() )
            random(
                { call(cl.customAsk("Waar zal ik naar zoeken?")) },
                { call(cl.customAsk("Waar zullen we naar opzoek?")) },
                {call(cl.customAsk("Welk onderwerp zoek je naar?"))}
            )
        }

        else if (state.keywordsCurrent.size >= resultThreshold) goto(simpleResult())

        else if (!state.madeProgress()) {
            state.suggestionCounter++
        }

        if (state.suggestionCounter >= suggestThreshold) goto(simpleSuggest())
        else {
            random(
                { call(cl.customAsk("Waar moet het verder over gaan?")) }
            )
        }
    }


    onResponse<doNotKnow> {
        call(cl.customResponse(it.text))
        furhat.gesture(Gestures.Smile)
        call(cl.customSay("oh, oke!"))
        goto(simpleSuggest())
    }

    this.onResponse<provideOptions> {
        call(cl.customResponse(it.text))
        goto(simpleSuggest())
    }

    this.onResponse {
        call(cl.customResponse(it.text))

        val newKeywords = call(extractMatchServ(it.text.lowercase(), false))
        println("   the new keywords I found; ${newKeywords}")

        if (newKeywords == null) {
            call(cl.customSay("Ik verstond ${it.text}. Daar hebben we geen videos over."))
        } else {
            state.processNewKeywords(newKeywords.toString())
            state.updateResults()
            if (state.resultSetCurrent.isEmpty()) {
                call(cl.customSay("Voor deze combinatie onderwerpen bestaat helaas geen video. Ik zoek verder zonder het woord ${newKeywords}"))
                state.revertKeywords()
            }
        }
        reentry()
    }
}

fun simpleSuggest(): State = state(Init) {
    onEntry {
        println("### Entered simpleSuggest()")

        if (state.keywordsCurrent.isEmpty()) {
            println("here we needed a rando suggest")
        }

        if (state.madeProgress() || state.suggestionPossibilities.isEmpty()) {
            println("   getting new suggestionzz")
            call(retrieveSuggestionKeywords())
        }

        if (state.suggestionPossibilities.size == state.suggestedBefore.size) {
            call(cl.customSay("Oke ik heb geen suggesties meer. Laten we kijken welke videos er zijn."))
            goto(simpleResult())
        }

        println("    Suggestion List before ranking: ${state.suggestionPossibilities.map{it.label}}")
        if(useRanking) {
            call(retrieveSuggestionKeywords())
            state.updatePreferredSuggestions()
        }
        println("    Suggestion List after ranking: ${state.suggestionPossibilities.map{it.label}}")
        var suggestionKeywords = state.suggestionPossibilities
        suggestionKeywords.removeAll(state.suggestedBefore)
        suggestionKeywords = suggestionKeywords.take(3).toMutableList()
        state.suggestedLastTurn = suggestionKeywords
        state.suggestedBefore.addAll(suggestionKeywords)

        println("   suggested last ${state.suggestedLastTurn.map { it.label }} \n   suggested before ${state.suggestedBefore.map{it.label}}\n   suggestionPossibiliteis: ${state.suggestionPossibilities.map{it.label }} ")

        if(suggestionKeywords.size == 0) {goto(simpleResult())}
        call(cl.customSay("De videos gaan verder over ${concatStrings(suggestionKeywords.mapNotNull { it.label })}"))
        call(cl.customAsk("Zit daar iets leuks tussen?"))
    }

    onResponse<Ja> {
        call(cl.customResponse(it.text))
        call(cl.customAsk("Oke, welk onderwerp vond je interessant?"))
        goto(handleSimpleSuggestResponse(it.text))
    }

    onResponse<Nee> {
        call(cl.customResponse(it.text))
        call(cl.customSay("Oh oke"))
        //raise(it.secondaryIntent)
        reentry()
    }

    onResponse {
        call(cl.customResponse(it.text))
        goto(handleSimpleSuggestResponse(it.text))
    }

    onExit {
        state.suggestionCounter = 0
    }
}

fun retrieveSuggestionKeywords(): State = state(Init) {
    onEntry {
        state.suggestionPossibilities = getPotentialSuggestions(state.keywordsCurrent)!!.toMutableList()
        terminate()
    }
}

fun handleSimpleSuggestResponse(userResponse: String): State = state(Init) {
    onEntry {
        println("    Response for suggestresponse handler: $userResponse")
        val match = findClosestMatch(state.suggestedLastTurn.mapNotNull { it.label }, userResponse)
        val matchingFromSuggest = state.suggestedLastTurn.find { it.label == match }
        //val gtaa = matchingFromSuggest?.gtaa
        //println("   Match: $match, and then it's gtaa: ${gtaa}, via the element: $matchingFromSuggest")
        if (matchingFromSuggest != null) state.addKeyword(matchingFromSuggest)
        state.updateResults()
        goto(conversationalSimplified())
    }
}

fun simpleResult(): State = state(Init) {
    onEntry {
        println("### Entered simpleResult()")
        call(cl.customSay("Hier is het filmpje!")) //een filmpje over ${currentSet.getHumanReadableLabels()}"))
        val vid = state.getRandomVideo()
        if (vid == null) {state.keywordsCurrent.take(1)} // always return something
        state.updateResults()
        call(watchVideo(state.getRandomVideo()?.link))
    }
}

