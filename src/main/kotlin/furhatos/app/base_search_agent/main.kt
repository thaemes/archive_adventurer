package furhatos.app.base_search_agent

import furhatos.app.base_search_agent.flow.*
import furhatos.flow.kotlin.Flow
import furhatos.flow.kotlin.dialogLogger
import furhatos.skills.Skill

class Base_search_agentSkill : Skill() {
    override fun start() {
        Flow().run(NoGUI)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
    Runtime.getRuntime().addShutdownHook(Thread {
        // Code to be executed on shutdown
        println("Program exiting...")
        matchServ.close()
        dialogLogger.endSession()
        //kbserv.disconnectKeyBERTServer()
    })

}
