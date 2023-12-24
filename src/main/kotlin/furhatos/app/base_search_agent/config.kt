package furhatos.app.base_search_agent

import furhatos.event.Event

// Variables and events
const val PORT = 1313 // GUI Port
const val SPEECH_DONE = "SpeechDone"

const val KEYBERT_THRESHOLD = 0.4

// Keybert server data
const val KEYBERTPORT = 12222
const val KEYBERTIP = "localhost"


const val MATCHING_SERVER_IP = "10.150.24.206"//"10.150.25.1"//"10.150.24.119" //"127.0.0.1" //"10.150.24.170"//"10.150.24.129"
const val MATCHING_SERVER_PORT = 12223

// BEELD & GELUID Config
const val bg_kg_url = "https://cat.apis.beeldengeluid.nl/sparql/"

const val filePath = ""

// Event used to pass data to GUI
class DataDelivery(
    val buttons : List<String>?,
    val inputFields: List<String>?,
    val messagesLog: List<String>?,
    val videoUrl: List<String>?
) : Event()

