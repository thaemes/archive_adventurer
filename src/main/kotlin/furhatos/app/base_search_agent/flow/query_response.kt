package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.nlu.*
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onResponse
import furhatos.flow.kotlin.state
import java.awt.Color
import kotlin.random.Random
import furhatos.app.base_search_agent.nlu.ThesaurusKeyword

var awake = false

val StartQueryResponse: State = state(Init) {
    onEntry {
        println("&&& In startqueryresponse state")
        furhat.ledStrip.solid(Color(0,0,0))
        furhat.listen(timeout = 20000)
        reentry()
    }

    onResponse<WakeWord>{
        furhat.ledStrip.solid(java.awt.Color.GREEN)
    }
}

fun SearchQueryResponse() = state(Init) {
    onEntry {
        furhat.listen()
    }

/*    onTime(delay = 20000){
        goto(StartQueryResponse)
    }
*/

    onResponse {
        furhat.ledStrip.solid(java.awt.Color.CYAN)
        var newKW = kbserv.keyBERTExtractMulti(it.text.toLowerCase())
        var gtaa = extractGTAAMulti2(getGTAAMulti2(newKW))
        var col = KeywordCollection()

        gtaa?.forEach { item ->
            if (item != null) col.kws.add(item)
        }

        if (!col.kws.all { it == null }) {
            col.retrieveResultsWithCurrentKws()
        }


        val numberOfVids = col.getSetSize()

        if (!(numberOfVids == null || numberOfVids == 0)) { //|| gtaa==null)) {
            val rand = Random.nextInt(numberOfVids)
            call(watchVideo(col.getSetVideos()?.get(rand)?.link))
            furhat.say(
                "Ik heb een filmpje gevonden over ${col.getHumanReadableLabels()}, het heet, ${col.getSetVideos()?.get(rand)?.title}.",
                async = true
            )
            awake = false
            goto(StartQueryResponse)
        } else {
            furhat.say("ik heb helaas niks gevonden")
            awake = false
            goto(StartQueryResponse)
        }
    }
}
