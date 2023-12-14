import React, {Component} from "react"
import ReactPlayer from "react-player"


class Video extends Component {
    constructor(props) {
	super(props)
}

render(){

	let { label } = this.props


    return (
        <div>
        <p>Video resultaat:</p>
        <ReactPlayer
          url= { label }
          controls = {true}
          playing= { true }
          muted = {true}
          width = { 800 }
          height = { 540 }
          //muted = {false}
        />
      </div>

    )

}
}
export default Video
