package furhatos.app.base_search_agent

import furhatos.event.Event

// Variables and events
const val PORT = 9989 // GUI Port
const val SPEECH_DONE = "SpeechDone"

const val KEYBERT_THRESHOLD = 0.4

// Keybert server data
const val KEYBERTPORT = 12222
const val KEYBERTIP = "localhost"


const val MATCHING_SERVER_IP = "127.0.0.1"
const val MATCHING_SERVER_PORT = 12223

// BEELD & GELUID Config
const val bg_kg_url = "https://cat.apis.beeldengeluid.nl/sparql/"

// Event used to pass data to GUI
class DataDelivery(
    val video : List<String>
) : Event()



class MyCustomEvent(
    var param1 : String? = null
) : Event()

// Declare event with a custom name - "MyOtherCustomEvent"
class MyOtherCustomEvent(
    var param1 : String? = null
) : Event("MyOtherCustomEvent")

// Send the event (inside a state)

