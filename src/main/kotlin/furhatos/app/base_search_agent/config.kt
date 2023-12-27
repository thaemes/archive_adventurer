package furhatos.app.base_search_agent

import furhatos.event.Event

// Variables and events
const val PORT = 1313 // GUI Port
const val SPEECH_DONE = "SpeechDone"

const val useRanking = true
const val useSerial  = false


// New Matching server
const val MATCHING_SERVER_IP = "localhost"//"10.150.24.71"//"10.150.24.206"//"10.150.25.1"//"10.150.24.119" //"127.0.0.1" //"10.150.24.170"//"10.150.24.129"
const val MATCHING_SERVER_PORT = 12223

const val RANKING_SERVER_IP = "localhost"
const val RANKING_SERVER_PORT = 12224

// Serial comms
const val serialPortName = "dev/tty.usbmodem17285601"

// BEELD & GELUID Config
const val bg_kg_url = "https://cat.apis.beeldengeluid.nl/sparql/"

const val filePath = ""


const val KEYBERT_THRESHOLD = 0.4

// Keybert server data
const val KEYBERTPORT = 12222
const val KEYBERTIP = "localhost"

// Event used to pass data to GUI
class DataDelivery(
    val buttons : List<String>?,
    val inputFields: List<String>?,
    val messagesLog: List<String>?,
    val videoUrl: List<String>?
) : Event()

