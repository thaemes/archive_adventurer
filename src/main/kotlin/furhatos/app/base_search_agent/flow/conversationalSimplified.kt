package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.nlu.*
import furhatos.app.base_search_agent.CustomLogger
import furhatos.app.base_search_agent.nlu.StateTracker
import furhatos.app.base_search_agent.nlu.doNotKnow
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures


/*

OVERAL WAAR WE Keywords opslaan moet het List<ThesaurusKeyword>,
OVERAL waar video, List<Video>

* */


var state = StateTracker()

fun conversationalSimplified(): State = state(Init) {
    onEntry {
        println("### Entered conversationalSimplified()")
        if (state.keywordsCurrent.size == 0) {
            random(
                { call(cl.customAsk("Waar zal ik naar zoeken?")) },
                { call(cl.customAsk("Welk onderwerp zullen we naar zoeken?")) }
            )
        } else if (state.keywordsCurrent.size >= 2) goto(simpleResult())
        else if (!state.madeProgress()) {
                state.suggestionCounter++
                if (state.suggestionCounter >= 1) goto(simpleSuggest())
            }
        else {
            random(
                { call(cl.customAsk("Waar moet het verder over gaan?")) }
            )
        }
    }

    this.onResponse<doNotKnow> {
        call(cl.customResponse(it.text))
        furhat.gesture(Gestures.Smile)
        call(cl.customSay("oh, oke!"))
        goto(simpleSuggest())
    }

    this.onResponse<provideOptions> {
        call(cl.customResponse(it.text))
        goto(simpleSuggest())
    }

    this.onResponse<Nee> { //todo
    }

    this.onResponse {
        call(cl.customResponse(it.text))
        var newKeywords = call(extractMatchServ(it.text.lowercase(), false))
        state.processNewKeywords(newKeywords.toString())

        if(state.keywordsCurrent.size == 0) {
            call(cl.customSay("Ik verstond ${it.text}. Daar zitten geen onderwerpen in die ik kenn. "))
        } else {
            state.updateResults()
            state.resultSetCurrent
        }
        if(state.resultSetCurrent.size <=3 ) {
            goto(simpleResult())
        }
        else {
            reentry()
        }
    }
}

fun simpleSuggest(): State = state(Init){
    onEntry{
        println("### Entered simpleSuggest()")
        //var potentialAdditionalKeywords = extractGTAAMulti2(getPotentialSuggestions(state.keywordsCurrent))
//        val groupedByCount = potentialOtherGTAAs?.groupingBy { it }?.eachCount()
//        sortedList =
//            groupedByCount?.toList()?.sortedByDescending { (_, count) -> count }?.map { it.first }?.take(3)
//                ?: emptyList()
//        val sortedMap = groupedByCount?.toList()?.sortedByDescending { (_, count) -> count }?.toMap()

        //call(cl.customSay(potentialAdditionalKeywords.toString()))
    }
}

fun handleSimpleSuggestResponse(){}

fun simpleResult():State = state(Init) {
    onEntry {
        println("### Entered simpleResult()")
        call(cl.customSay("Hier is het filmpje!.")); //een filmpje over ${currentSet.getHumanReadableLabels()}"))
        call(watchVideo(state.getRandomVideo()?.link))
    }
}