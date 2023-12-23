package furhatos.app.base_search_agent.nlu

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ThesaurusKeyword(
    @SerialName("gtaa") var gtaa: String?,
    @SerialName("label") var label: String?,
    @SerialName("similarity_score") var similarityScore: Double//?
)

//class Keyword(
//    var gtaa: String,
//    var label: String
//)