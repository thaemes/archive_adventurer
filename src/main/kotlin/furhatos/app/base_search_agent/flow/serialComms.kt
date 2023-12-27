package furhatos.app.base_search_agent.flow

import furhatos.app.base_search_agent.serialPortName
import furhatos.app.base_search_agent.flow.Init
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPort
import furhatos.flow.kotlin.Furhat
import furhatos.flow.kotlin.State
import furhatos.flow.kotlin.state

fun setupSerialPort(): SerialPort? {
    val baudRate = 9600 // Replace with the correct baud rate

    val serialPort = SerialPort.getCommPort(serialPortName)
    serialPort.baudRate = baudRate
    serialPort.openPort()

    println("   Serial connection is: " + serialPort.isOpen)
    return if (serialPort.isOpen) serialPort else null
}

fun readSerialData(serialPort: SerialPort): State = state(Init) {
    onEntry {
        val buffer = ByteArray(1024) // Adjust as needed

        while (true) {
            if (serialPort.bytesAvailable() > 0) {
                val bytesRead = serialPort.readBytes(buffer, buffer.size)
                val data = String(buffer, 0, bytesRead).trim()
                //println("Got serial input: $data")
                if (data != "") {
                    when (data.toInt()) {
                        0 -> call(cl.addEmojiToLastMessage("🙂"))  // Smile
                        1 -> call(cl.addEmojiToLastMessage("😀"))  // Grinning Face
                        2 -> call(cl.addEmojiToLastMessage("\uD83D\uDE03"))  // Beaming Face with Smiling Eyes
                        3 -> call(cl.addEmojiToLastMessage("\uD83D\uDE01"))  // Grinning Face
                        4 -> call(cl.addEmojiToLastMessage("\uD83E\uDD29"))  // Grinning Face with Big Eyes
                        else -> println("   Invalid value from Serial")
                    }
                }
            }
            Thread.sleep(100) // Adjust as needed
        }
    }
}

/*
🙂
😀
😃
😁
🤩

 */