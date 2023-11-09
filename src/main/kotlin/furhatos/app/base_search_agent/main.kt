package furhatos.app.base_search_agent

import furhatos.app.base_search_agent.flow.Init
import furhatos.app.base_search_agent.flow.kbserv
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill

class Base_search_agentSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
    Runtime.getRuntime().addShutdownHook(Thread {
        // Code to be executed on shutdown
        println("Program exiting...")
        kbserv.disconnectKeyBERTServer()
    })

}
