package furhatos.app.base_search_agent.flow

//import furhatos.app.base_search_agent.nlu.*
import furhatos.app.base_search_agent.nlu.StateTracker
import furhatos.flow.kotlin.*

//import furhatos.gestures.Gestures

var state = StateTracker()

fun conversationalSimplified(): State = state(Init) {
    onEntry {
        if (state.keywordsCurrent.size == 0) {
            random(
                { call(cl.customAsk("Waar zal ik naar zoeken?")) },
                { call(cl.customAsk("Welk onderwerp zullen we naar zoeken?")) }
            )
        } else if (state.keywordsCurrent.size >= 2) goto(simpleResult())
        else if (!state.madeProgress()) {
                currentSet.suggestionCounter++
                if (currentSet.suggestionCounter >= 1) goto(askSuggestSnap())
            }
        else {
            random(
                { call(cl.customAsk("Waar moet het verder over gaan?")) }
            )
        }
    }
}

fun simpleSuggest(): State = state(Init){
    onEntry{
        var potentialAdditionalKeywords = extractLinkedTopicsList(getLinkedTopicsMulti(currentSet.getGTAAs()))



    }
}

fun handleSimpleSuggestResponse(){}

fun simpleResult():State = state(Init) {
    onEntry {
        call(cl.customSay("Hier is het filmpje!.")); //een filmpje over ${currentSet.getHumanReadableLabels()}"))
        call(watchVideo(state.getRandomVideo()?.link))
    }
}