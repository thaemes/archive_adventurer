package furhatos.app.base_search_agent.nlu

import RankingServer
import furhatos.app.base_search_agent.flow.filterOnRelatedTerms
import furhatos.app.base_search_agent.flow.rankingServ
import furhatos.app.base_search_agent.flow.searchLinkedAPIMulti
import furhatos.app.base_search_agent.useRanking
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import org.json.JSONObject

class StateTracker {

    var keywordsCurrent: MutableList<ThesaurusKeyword> = mutableListOf()
    private var keywordsLast: MutableList<ThesaurusKeyword> = mutableListOf()
    private var resultSetCurrentJSON: JSONObject? = null
    var resultSetCurrent: List<Video?> = listOf()
    var suggestionCounter = 0

    var suggestedBefore: MutableList<ThesaurusKeyword> = mutableListOf()
    var suggestedLastTurn: MutableList<ThesaurusKeyword> = mutableListOf()
    var suggestionPossibilities: MutableList<ThesaurusKeyword> = mutableListOf()
    //var potentialSuggestionsNeedFlush = false

    fun addKeyword(inputKw: ThesaurusKeyword) {
        if (!keywordsCurrent.contains(inputKw)) {
            this.keywordsCurrent.add(inputKw)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun processNewKeywords(jsonData: String) {
        try {
            val thesaurusKeywords = Json.decodeFromString<List<ThesaurusKeyword>>(jsonData)
            val mostSimilarWord =
                thesaurusKeywords.maxByOrNull { it.similarityScore } // maybe change to longest keyword!
            mostSimilarWord?.let { this.addKeyword(it) }
            println("Added new keyword: ${mostSimilarWord?.label}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSetSize(): Int {
        return resultSetCurrent.size
    }

    fun getLabels(): List<String?> {
        val li: MutableList<String?> = mutableListOf()
        for (i in 0 until keywordsCurrent.size) li.add(keywordsCurrent[i].label?.lowercase())
        return li.toList()
    }

    fun getGTAAs(): List<String?> {
        val li: MutableList<String?> = mutableListOf()
        for (i in 0 until keywordsCurrent.size) li.add(keywordsCurrent[i].gtaa.toString())
        return li.toList()
    }

    fun getHumanReadableLabels(): String? {
        return concatStrings(this.getLabels())
    }

    fun updateResults() {
        println("new videos retrieved with current results: ${this.getLabels()}")
        val results = searchLinkedAPIMulti(this.getGTAAs())
        val resultsArray = results?.getJSONObject("results")?.getJSONArray("bindings")
        val videoList = mutableListOf<Video>()
        //if (resultsArray == null) return null

        for (i in 0 until resultsArray?.length()!!) {
            val resultObj = resultsArray.getJSONObject(i)
            val title = resultObj.getJSONObject("title").getString("value").toLowerCase()
            val contentUrl = resultObj.getJSONObject("content_url").getString("value")

            val video = Video()
            video.title = title
            video.link = contentUrl
            videoList.add(video)
        }
        resultSetCurrent = videoList
    }

    fun updatePreferredSuggestions() {
        if (useRanking) {
            val request = createJsonRequest(this.keywordsCurrent[0], this.suggestionPossibilities)
            val results = getRankingResults(rankingServ, request)
//            results?.forEach {
//                println("Label: ${it.label}, GTAA: ${it.gtaa}, Similarity Score: ${it.similarityScore}")
//            }
            this.suggestionPossibilities = results as MutableList<ThesaurusKeyword>
        }
    }

    fun getRandomVideo(): Video? {
        return this.resultSetCurrent.let {
            if (it.isNotEmpty()) it.random() else null
        }
    }

    fun madeProgress(): Boolean {
        if (this.keywordsLast == this.keywordsCurrent) return true
        return false
    }

    fun revertKeywords() {
        this.keywordsCurrent = this.keywordsLast.toList().toMutableList()
        //this.updateResultsWithCurrentKeywords()
    }

    fun resetState() {
        this.keywordsCurrent = mutableListOf()
        this.keywordsLast = mutableListOf()
        this.resultSetCurrentJSON = null
        this.suggestedBefore = mutableListOf()
        this.suggestionPossibilities = mutableListOf()
        this.suggestedLastTurn = mutableListOf()
        this.suggestionCounter = 0
        println("Reset conversational state")
    }

}

@Serializable
data class KeywordsRequest(
    @SerialName("main_keyword") val mainKeyword: ThesaurusKeyword,
    @SerialName("other_keywords") val otherKeywords: List<ThesaurusKeyword>
)

fun createJsonRequest(mainKeyword: ThesaurusKeyword, otherKeywords: List<ThesaurusKeyword>): String {
    val request = KeywordsRequest(mainKeyword, otherKeywords)
    return Json.encodeToString(request)
}


//... your JSON request string ...
fun deserializeResponse(jsonResponse: String): List<ThesaurusKeyword> {
    return Json.decodeFromString(jsonResponse)
}

fun getRankingResults(rankingServer: RankingServer, request: String): List<ThesaurusKeyword>? {
    rankingServer.sendRequest(request)
    val response = rankingServer.receiveResponse() ?: return null
    //println("desireal: " + deserializeResponse(response))
    return deserializeResponse(response)

}