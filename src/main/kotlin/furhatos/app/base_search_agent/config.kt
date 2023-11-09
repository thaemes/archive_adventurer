package furhatos.app.base_search_agent

import furhatos.event.Event

// Variables and events
const val PORT = 9989 // GUI Port
const val SPEECH_DONE = "SpeechDone"

const val KEYBERT_THRESHOLD = 0.4

// Keybert server data
const val KEYBERTPORT = 12223
const val KEYBERTIP = "localhost"


// BEELD & GELUID Config
const val bg_kg_url = "https://cat.apis.beeldengeluid.nl/sparql/"

// Event used to pass data to GUI
class DataDelivery(
    val video : List<String>
) : Event()








