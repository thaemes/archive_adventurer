package furhatos.app.base_search_agent.nlu

import furhatos.app.base_search_agent.flow.currentSet
import furhatos.app.base_search_agent.flow.searchLinkedAPIMulti
import org.json.JSONObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class thesaurusKeyword(
    @SerialName("gtaa") var gtaa: String?,
    @SerialName("label") var label: String?,
    @SerialName("similarity_score") var similarityScore: Double
)


class KeywordCollection {
    var kws: MutableList<thesaurusKeyword> = mutableListOf()
    var kws_prev: MutableList<thesaurusKeyword> = mutableListOf()
    var set: JSONObject? = null
    var suggestionCounter = 0
    var cameFromSuggestion = false
    var suggestedBefore: MutableList<String> = mutableListOf()
    var suggestedLastTurn: MutableList<String> = mutableListOf()

    fun addKw(inK: thesaurusKeyword) {
        this.kws.add(inK)
    }

    fun loadDataFromJson(jsonData: String) {
        try {
            val thesaurusKeywords = Json.decodeFromString<List<thesaurusKeyword>>(jsonData)

            // Find the word with the highest similarity score
            val mostSimilarWord = thesaurusKeywords.maxByOrNull { it.similarityScore }
            // Add the most similar word to the 'kws' list
            mostSimilarWord?.let { kws.add(it) }

            // Assuming you want to add the data to the 'kws' list
            //kws.addAll(thesaurusKeywords)
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception as needed
        }
    }


    fun getSetSize(): Int {
        val resultsArray = this.set?.getJSONObject("results")?.getJSONArray("bindings")
        val titlesSet = HashSet<String>()
        if (resultsArray == null) return 0
        for (i in 0 until resultsArray.length()) {
            val resultObj = resultsArray.getJSONObject(i)

            val title = resultObj.getJSONObject("title").getString("value").toLowerCase()
            titlesSet.add(title)
        }
        return titlesSet.size
    }

    fun getLabels(): List<String?> {
        val li: MutableList<String?> = mutableListOf()
        for (i in 0 until kws.size) li.add(kws[i].label?.toLowerCase())
        return li.toList()
    }

    fun getPrevLabels(): List<String?> {
        val li: MutableList<String?> = mutableListOf()
        for (i in 0 until kws_prev.size) li.add(kws_prev[i].label?.toLowerCase())
        return li.toList()
    }

    fun getHumanReadableLabels(): String? {
        return concatStrings(this.getLabels())
    }

    fun getGTAAs(): List<String?> {
        val li: MutableList<String?> = mutableListOf()
        for (i in 0 until kws.size) li.add(kws[i].gtaa.toString())
        return li.toList()
    }

    fun retrieveResultsWithCurrentKws() {
        this.kws = this.kws.distinctBy { it.gtaa }.toMutableList()
        this.set = searchLinkedAPIMulti(this.getGTAAs())
    }

    fun getSetVideos(): List<Video?>? {
        val resultsArray = this.set?.getJSONObject("results")?.getJSONArray("bindings")
        val videoList = mutableListOf<Video>()
        val titlesSet = HashSet<String>()
        if (resultsArray == null) return null

        for (i in 0 until resultsArray.length()) {
            val resultObj = resultsArray.getJSONObject(i)
            val title = resultObj.getJSONObject("title").getString("value").toLowerCase()
            val contentUrl = resultObj.getJSONObject("content_url").getString("value")

            if (!titlesSet.contains(title)) {
                val video = Video()
                video.title = title
                video.link = contentUrl
                videoList.add(video)
                titlesSet.add(title)
            }
        }
        return videoList
    }

    fun stepBack() {
        this.kws = currentSet.kws_prev.toList().toMutableList()
        this.set = null
    }

    fun stepBackSuggested() {

    }

    fun reset() {
        this.kws = mutableListOf()
        this.kws_prev = mutableListOf()
        this.cameFromSuggestion = false
        this.set = null
        this.suggestedBefore = mutableListOf()
        this.suggestedLastTurn = mutableListOf()
        this.suggestionCounter = 0
        println("### current keywords reset. Ready for a new session!")
    }
}


