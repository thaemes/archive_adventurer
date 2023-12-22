package furhatos.app.base_search_agent.nlu

import furhatos.app.base_search_agent.flow.searchLinkedAPIMulti
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class StateTracker {

    var keywordsCurrent: MutableList<ThesaurusKeyword> = mutableListOf()
    var keywordsLast: MutableList<ThesaurusKeyword> = mutableListOf()
    private var resultSetCurrentJSON: JSONObject? = null
    var resultSetCurrent: List<Video?> = listOf()
    var suggestionCounter = 0
    var suggestedBefore: MutableList<ThesaurusKeyword> = mutableListOf()
    var suggestedLastTurn: MutableList<ThesaurusKeyword> = mutableListOf()

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

    fun updateResults()  {
        var results = searchLinkedAPIMulti(this.getGTAAs())
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

    fun getRandomVideo(): Video? {
        return this.resultSetCurrent?.let {
            if (it.isNotEmpty()) it.random() else null
        }
    }

    fun madeProgress(): Boolean {
        if(this.keywordsLast == this.keywordsCurrent) return true
        return false
    }
    fun revertKeywords() {
        this.keywordsCurrent = this.keywordsLast.toList().toMutableList()
        //this.updateResultsWithCurrentKeywords()
    }

    fun resetState() {
        this.keywordsLast = mutableListOf()
        this.keywordsLast = mutableListOf()
        this.resultSetCurrentJSON = null
        this.suggestedBefore = mutableListOf()
        this.suggestedLastTurn = mutableListOf()
        this.suggestionCounter = 0
        println("Reset conversational state")
    }

}

