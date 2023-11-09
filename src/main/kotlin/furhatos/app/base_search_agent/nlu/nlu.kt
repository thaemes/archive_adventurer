package furhatos.app.base_search_agent.nlu

import furhatos.nlu.ComplexEnumEntity
import furhatos.nlu.EnumEntity
import furhatos.nlu.Intent
import furhatos.nlu.ListEntity
import furhatos.util.Language
import simplenlg.framework.InflectedWordElement
import simplenlg.framework.LexicalCategory
import simplenlg.framework.NLGFactory
import simplenlg.framework.WordElement
import simplenlg.lexicon.dutch.XMLLexicon
import simplenlg.realiser.Realiser
import kotlin.ranges.contains


val xmllex: XMLLexicon = simplenlg.lexicon.dutch.XMLLexicon()
val realiser = Realiser()
val nlgFactory = NLGFactory()


fun pluralize(input: String?): String? {
    if (input == null) return null
    if (input.toLowerCase().equals("wintersporten") || input.toLowerCase().equals("wintersport")) {
        println("### Wintersport hardcode reached")
        return "wintersport"
    }
    if (input.toLowerCase().equals("sport")) return "sport"
    if (input.toLowerCase().equals("koffie")) return "koffie"
    if (input.toLowerCase().equals("jacht")) return "jacht"
    if (input.toLowerCase().equals("musici")) return "musici"
    if (input.toLowerCase().equals("bijzondere vliegtuigen")) return "bijzondere vliegtuigen"
    if (input.toLowerCase().contains("chocola")){
        return "chocolade"
    }
    if(input.toLowerCase().equals("kaas")) {
        return "kaas"
    }
    if(input.toLowerCase().equals("paardensport")) {
        return "paardensport"
    }
    if(input.toLowerCase().equals("dierenverzorging")) {
        return "dierenverzorging"
    }
    if (input.toLowerCase().equals("skiën") || input.toLowerCase().equals("ski")) {
        println("### skiën hardcode reached")
        return "skiën"
    }
    if (input.toLowerCase().equals("mode")){
        return "mode"
    }
    if (input.toLowerCase().equals("kleding")){
        return "kleding"
    }
    if (input.toLowerCase().equals("wateroverlast")){
        return "wateroverlast"
    }
    if (input.toLowerCase().equals("militaire")) {
            return "militairen"
    }
    if (input.toLowerCase().equals("muziek")) {
        return input
    }
    if (input.endsWith("en") || input.endsWith("ën") || input.endsWith("s")) {
        println("### I assumed the word $input was already plural")
        return input
    }
    if(input.endsWith("y")) return input

    val a: WordElement = xmllex.getWord(input, LexicalCategory.NOUN)
    val b = InflectedWordElement(a)
    if (!a.isPlural) b.isPlural = true
    println("### pluralized ${input} to ${realiser.realise(b).realisation}")
    return realiser.realise(b).realisation
}

class doNotKnow : ComplexEnumEntity() {
    override fun getEnum(lang:Language): List<String>{
        return listOf(
            "weet ik niet",
            "ik weet niet",
            "weet niet"
        )
    }
}

class Nee : Intent() {
    override fun getExamples(Lang: Language): List<String> {
        return listOf(
            "nee", "niet", "nah", "neen", "nope"
        )
    }

}

class provideOptions() : ComplexEnumEntity() {
    override fun getEnum(lang:Language): List<String>{
        return listOf(
            "wat heb je?",
            "welke heb je?",
            "welke onderwerpen zijn er?",
            "welke onderwerpen heb je?"
        )
    }
}


fun getQuantifyWord(number: Int?): String {
    return when (number) {
        in 0..5 -> "een paar"
        in 6..20 -> "veel"
        else -> "heel veel"
    }
}


/// Numbers do not work correctly yet
class Number : EnumEntity() {
    override fun getEnum(Lang: Language): List<String> {
        return listOf(
            "1: een, eerste",
            "2: twee, tweede",
            "3: drie, derde",
            "4: vier, vierde",
            "5: vijf, vijfde",
            "6: zes, zesde",
            "7: zeven, zevende",
            "8: acht, achtste",
            "9: negen, negende",
            "10: tien, tiende",
            "-1: laatste",
            "-2: enerlaatste, een-na-laatste"
        )
    }
}



class Ja : Intent() {
    override fun getExamples(Lang: Language): List<String> {
        return listOf(
            "ja",
            "jawoor",
            "jahoor",
            "ja hoor",
            "jawel",
            "jazeker",
            "yeah",
            "is goed",
            "goed",
            "prima",
            "perfect",
            "natuurlijk",
            "yep"
        )
    }
}


class WakeWord : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "hey robot",
            "irobot",
            "iRobot",
            "hey furhat",
            "hey Ronald",
            "hero",
            "robot",
            "robots",
            "hey Robert",
            "heroal",
            "Google",
            "Hey Robot",
            "furhat",
            "ronald"
        )
    }
}


fun concatStrings(strings: List<String?>?): String? {
    var out: String = ""
    if (strings == null) return null
    for (i in 0 until strings.size) {
        out += strings[i]
        if (i == strings.size - 2) {
            out += ", en "
        } else if (i < strings.size - 2) {
            out += ", "
        }
    }
    return out
}
