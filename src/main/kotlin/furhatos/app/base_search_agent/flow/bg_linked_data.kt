package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.bg_kg_url
import furhatos.app.base_search_agent.nlu.ThesaurusKeyword
import furhatos.app.base_search_agent.nlu.pluralize
import khttp.post
import org.json.JSONObject
import sparqlFilterVideos
import sparqlGTAA
import sparqlGTAARegex
import sparqlNarrower
import sparqlQueryLinkedTopicsMulti
import java.net.UnknownHostException


fun isNarrower(new: String, old: String): Boolean? {
    val result: JSONObject
    var query = sparqlNarrower(new, old)

    val headers = mapOf("Accept" to "application/sparql-results+json;q=1.0")
    try {
        result = JSONObject(post(bg_kg_url, data = mapOf("query" to query), headers = headers).text)
    } catch (e: UnknownHostException) {
        println("... Unknown host exception for B&G Communica API")
        return null
    }
    println(result.toString())
    return result.getBoolean("boolean")
}


fun searchLinkedAPIMulti(gtaas: List<String?>): JSONObject? {
    val resultJson: JSONObject
    var query = sparqlFilterVideos(gtaas)

    //print("\n\nQuery:"+query+"\n\n")

    if (gtaas == null || gtaas.all { it == null || it?.isBlank()!! }) {
        println("NULL")
        return null
    }
    val headers = mapOf("Accept" to "application/sparql-results+json;q=1.0")
    try {
        resultJson = JSONObject(post(bg_kg_url, data = mapOf("query" to query), headers = headers).text)
    } catch (e: UnknownHostException) {
        println("... Unknown host exception for B&G Communica API")
        return null
    }
    println("... Got video results")
    return resultJson
}

fun getLinkedTopicsMulti(inp: List<String?>): JSONObject? {
    if (inp == null) {
        return null
    }
    val resultJson: JSONObject?
    val query = sparqlQueryLinkedTopicsMulti(inp)
    val headers = mapOf("Accept" to "application/sparql-results+json;q=1.0")
    try {
        resultJson = JSONObject(post(bg_kg_url, data = mapOf("query" to query), headers = headers).text)
    } catch (e: UnknownHostException) {
        println("... Unknown host exception for B&G Communica API")
        return null
    }
    return resultJson
}


fun extractLinkedTopicsList(res: JSONObject?, ranked: Boolean = false): List<String>? {
    if (res == null) return null
    val array = res.getJSONObject("results").getJSONArray("bindings")
    val linkedAboutsLabels = mutableListOf<String>()
    for (i in 0 until array.length()) {
        val element = array.getJSONObject(i).getJSONObject("linked_abouts_label")
        val label = element.getString("value")
        linkedAboutsLabels.add(label)
    }
    return linkedAboutsLabels
}

fun getGTAAMulti2(input: List<String>?): List<Pair<String?, JSONObject?>>? {
    val headers = mapOf("Accept" to "application/sparql-results+json;q=1.0")
    val results: MutableList<Pair<String, JSONObject?>> = mutableListOf()

    if (input != null) {
        for (i in 0 until input?.size!!) {
            if (input[i].toLowerCase() == "naar") {
                println("NAAAR")
                continue
            }
            val query = sparqlGTAA(pluralize(input[i]))
            try {
                val resultJson = JSONObject(post(bg_kg_url, data = mapOf("query" to query), headers = headers).text)
                results.add(Pair(input[i], resultJson))

            } catch (e: UnknownHostException) {
                println("... Unknown host exception for B&G linked data API")
            }
        }
    }
    return results
}

fun getGTAAPartial(input: String?): Pair<String?, JSONObject?> {
    val headers = mapOf("Accept" to "application/sparql-results+json;q=1.0")
    var results: Pair<String, JSONObject?> = Pair("", null)

    if (input != null) {
        val query = sparqlGTAARegex(input)
        println(query)
        println("... trying to send ${input}")
        try {
            val resultJson = JSONObject(post(bg_kg_url, data = mapOf("query" to query), headers = headers).text)
            results = Pair(input, resultJson)
            println(results.toString())

        } catch (e: UnknownHostException) {
            println("... Unknown host exception for B&G linked data API")
        }
    }
    return results
}


fun extractGTAAMulti2(res: List<Pair<String?, JSONObject?>>?): List<ThesaurusKeyword?>? {
    if (res == null) return null
    var results: MutableList<ThesaurusKeyword?> = mutableListOf()

    for (i in 0 until res.size) {
        try {
            val out = res[i].second?.getJSONObject("results")?.getJSONArray("bindings")?.getJSONObject(0)
                ?.getJSONObject("uri")
                ?.getString("value")?.substringAfterLast("/")
            println("... Succesfully extracted a GTAA: ${out}")
            println("... result:" + res[i].toString())
            results.add(ThesaurusKeyword(out, res[i].first, 0.0), )
        } catch (e: Exception) {
            println("... JSON extraction of one GTAA Failed")
        }
    }
    return results
}