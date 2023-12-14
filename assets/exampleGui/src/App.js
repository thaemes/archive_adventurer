import React, {Component} from 'react'
import FurhatGUI from 'furhat-gui'
import { Grid, Row, Col } from 'react-bootstrap'

import Video from './Video'

class App extends Component {

    constructor(props) {
        super(props)
        this.state = {
          "speaking": false,
          "video": []
        }
        this.furhat = null
    }

    setupSubscriptions() {
        // Our DataDelivery event is getting no custom name and hence gets it's full class name as event name.
        this.furhat.subscribe('furhatos.app.base_search_agent.DataDelivery', (data) => {
            this.setState({
                ...this.state,
                    video: data.video
            })
            
        })

        // This event contains to data so we defined it inline in the flow
        this.furhat.subscribe('SpeechDone', () => {
            this.setState({
                ...this.state,
                speaking: false
            })
        })
    }

    componentDidMount() {
        FurhatGUI()
            .then(connection => {
                this.furhat = connection
                this.setupSubscriptions()
            })
            .catch(console.error)
    }

    clickButton = (button) => {
        this.setState({
            ...this.state,
            speaking: true
        })
        this.furhat.send({
          event_name: "ClickButton",
          data: button
        })
    }

    variableSet = (variable, value) => {
        this.setState({
            ...this.state,
            speaking: true
        })
        this.furhat.send({
          event_name: "VariableSet",
          data: {
            variable,
            value
          }
        })
    }

    render() {
        return (
           
            <div id="main">                
                <h1>Zoeken met Furhat</h1>
                { this.state.video.map((label) =>
				      <Video label={ label } /> ) 
                }
            </div>
          
          )
      }
  
  }

export default App;
