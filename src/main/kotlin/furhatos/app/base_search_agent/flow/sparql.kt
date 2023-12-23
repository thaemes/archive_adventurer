const val pre: String = """
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX skosxl: <http://www.w3.org/2008/05/skos-xl#>
PREFIX sdo: <https://schema.org/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX gtaa: <http://data.beeldengeluid.nl/gtaa/>
    """

const val post: String = """
LIMIT 400    
"""

fun sparqlNarrower(new: String, old: String): String {
    return """
        PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX gtaa: <http://data.beeldengeluid.nl/gtaa/>

    ASK {
        gtaa:$new skos:narrower* gtaa:$old
    }
    """
}

fun sparqlRelated(): String {
    return """
ASK {
    gtaa:28142 skos:related gtaa:28581
}
        """
}

fun sparqlGTAARegex(input: String?): String {
    return pre + """
        SELECT *
        WHERE {
        ?uri skos:prefLabel ?prefLabel FILTER regex(?prefLabel, "^${input}") .
            OPTIONAL {
                    ?uri skos:altLabel ?alt_label .
                      }  OPTIONAL {
                          ?uri skos:scopeNote ?scope_note .
                            }
                              OPTIONAL {
                                  ?uri skos:inScheme ?concept_scheme
                              }
        }                    
        """ + post
}


fun sparqlFilterVideos(inputList: List<String?>): String {
    var valuesClause = ""
    var inlineClause = ""

    for ((index, value) in inputList.withIndex()) {
        valuesClause += "VALUES ?var$index {gtaa:$value}\n"
        inlineClause += "sdo:about ?var$index ;"
    }

    return pre + """
SELECT DISTINCT ?item ?title ?content_url ?encoding
WHERE {
  ${valuesClause} 
  
  ?item rdf:type sdo:Clip ;
  ${inlineClause}
        sdo:name ?title ;
        sdo:associatedMedia [
      		sdo:encodingFormat ?encoding ;
      		sdo:contentUrl ?content_url
    	]
}      
        """ + post
}

fun sparqlQueryLinkedTopicsMulti(input: List<String?>): String {
    var valuesClause = ""
    var inlineClause = ""
    var filterClause = ""

    for ((index, value) in input.withIndex()) {
        valuesClause += "VALUES ?var$index {gtaa:$value}\n"
        inlineClause += "sdo:about ?var$index ;"
        filterClause += "?var$index != ?linked_abouts &&"
    }
    filterClause = filterClause.removeSuffix("&&")

    val q = pre + """
SELECT ?linked_abouts_label 
WHERE {
  ${valuesClause}
    ?item rdf:type sdo:Clip;
        ${inlineClause}
        (sdo:about) ?linked_abouts ;
        sdo:name ?title ;
        sdo:associatedMedia [
      		sdo:encodingFormat ?encoding ;
      		sdo:contentUrl ?content_url
    	] .
   	?linked_abouts skosxl:prefLabel/skosxl:literalForm ?linked_abouts_label

  FILTER(${filterClause})
}
""" + post
   //println("Query from sparqlQueryLinkedTopicsMulti: $q")
    return q
}


fun sparqlGTAA(input: String?): String {
    return pre + """
    SELECT ?uri
    WHERE {
      ?uri skosxl:prefLabel/skosxl:literalForm "${input}"@nl ;
        skos:inScheme gtaa:Onderwerpen .
    }  
""" + post
}


fun sparqlPossibleSuggestions(input: List<String>): String {
    var valuesClause = ""
    var inlineClause = ""
    var filterClause = ""

    for ((index, value) in input.withIndex()) {
        valuesClause += "VALUES ?var$index {gtaa:$value}\n"
        inlineClause += "sdo:about ?var$index ;"
        filterClause += "?var$index != ?linked_abouts &&"
    }
    filterClause = filterClause.removeSuffix("&&")

    val q = pre + """
SELECT ?linked_abouts_label ?gtaa
WHERE {
  ${valuesClause}
  ?item rdf:type sdo:Clip;
      ${inlineClause}
      (sdo:about) ?linked_abouts ;
      sdo:name ?title ;
      sdo:associatedMedia [
      		sdo:encodingFormat ?encoding ;
      		sdo:contentUrl ?content_url
    	] .
  ?linked_abouts skosxl:prefLabel/skosxl:literalForm ?linked_abouts_label .
  ?linked_abouts skos:inScheme gtaa:Onderwerpen .
  FILTER (${filterClause})
  BIND(STRAFTER(STR(?linked_abouts), STR(gtaa:)) AS ?gtaa)
}
""" + post

    return q
}