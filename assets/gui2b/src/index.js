import FurhatGUI from 'furhat-gui'
let reactions = {};
let age = null;

let currentlyEditingMessageId = null;
let isEmojiPickerOpen = false;
//const emojiList = ['😀', '😂', '😊', '😎', '😜', '😍', '😘', '🥳', '👍', '👎'];
const emojiList = ['🤩', '😁', '😃', '😀', '🙂'];
let furhat = null;
let agePanelVisible = false;

console.log("I loaded")


{
    FurhatGUI()
    .then(connection => {
        furhat = connection;
        furhat.subscribe('furhatos.app.base_search_agent.DataDelivery', (data) => {
            // Activates any time i received somethíng from the FH side! DataDelivery Bby
            console.log(data)
            if (data.messagesLog) {
                displayChatReactMode(data.messagesLog);
            }
            else if (data.videoUrl) {
                displayVideoMode(data.videoUrl)
            }
            else if (data.buttons) { }
            else {
                document.getElementById('chatContainer').innerHTML = '';
                document.getElementById('videoContainer').innerHTML = '';
                if (document.getElementById("agePanelContainer")) { document.getElementById("agePanelContainer").innerHTML = ''; }
                if (document.getElementById("readyButton")) { document.getElementById("readyButtonContainer").innerHTML = ''; }
                console.log("got empty message");
                //displayStartButton();
            }
        })

    })
    .catch(console.error)
}

document.addEventListener('DOMContentLoaded', (event) => {
    // Your code here will run once the DOM is fully loaded
    displayStartButton();
});


function displayStartButton() {
    document.body.style.backgroundColor = "#f4f4f4";
    if (document.getElementById("agePanelContainer")) { document.getElementById("agePanelContainer").innerHTML = ''; }
    if (document.getElementById("readyButton")) { document.getElementById("readyButtonContainer").innerHTML = '';  } 
    document.getElementById('videoContainer').innerHTML = '';
    document.getElementById('chatContainer').innerHTML = '';


    const startButton = document.createElement('button');
    startButton.id = 'startButton';
    startButton.innerText = 'Start!';
    document.getElementById('flowButtonContainer').append(startButton);
    //document.innerHTML = startButton;

    startButton.addEventListener('click', function() {
        // Send data to furhat
        furhat.send({
            event_name: "ClickButton",
            data: 'startButton'
        });

        // Reduce the brightness of the button
        this.style.filter = "brightness(50%)";
    });
}


function displayVideoMode(input) {
    //if(input.)
    document.body.style.backgroundColor = "black";
    document.getElementById('chatContainer').innerHTML = '';
    document.getElementById('videoContainer').innerHTML = '';
    if (document.getElementById("agePanelContainer")) { document.getElementById("agePanelContainer").innerHTML = ''; }
    if (document.getElementById("readyButton")) { document.getElementById("readyButtonContainer").innerHTML = ''; }
    if (document.getElementById("flowButtonContainer")) { document.getElementById("flowButtonContainer").innerHTML = ''; }

    // Create a new video element
    if (document.getElementById("vid")) { document.getElementById("vid").outerHTML = ''; console.log("old vid removed") }
    const video = document.createElement('video');

    // Set the video's source
    video.src = input;
    video.controls = true;
    video.autoplay = true;
    video.id = "vid";

    // Set some optional attributes, like width and height
    video.width = 640;   // You can adjust this value
    video.height = 480;  // You can adjust this value
    document.getElementById('videoContainer').append(video);

    const vidDoneButton = document.createElement('button');
    vidDoneButton.id = "vidDoneButton";
    vidDoneButton.innerText = "Ik ben klaar met kijken"
    document.getElementById('videoContainer').append(vidDoneButton);

    vidDoneButton.addEventListener('click', () =>
        furhat.send({
            event_name: "ClickButton",
            data: 'vidDoneButton'
        }));

}

function displayChatReactMode(input) {
    document.body.style.backgroundColor = "#f4f4f4";
    if (document.getElementById("flowButtonContainer")) { document.getElementById("flowButtonContainer").innerHTML = ''; }
    document.getElementById('videoContainer').innerHTML = '';
    if (!document.getElementById('chatContainer')) { document }
    console.log("ran displaychatreactmode");
    reactions = {};
    addMessagesToDOM(input)

    if (!document.getElementById("readyButton")) {
        // Create a container for the button and separator
        var readyButtonContainer = document.createElement("div");
        readyButtonContainer.id = "readyButtonContainer"; // Set an ID or class for styling if needed

        // Create a new button element
        var readyButton = document.createElement("button");
        readyButton.id = "readyButton"; // Set attributes for the button
        readyButton.innerText = "Ik ben klaar!"; // You can change the button text here

        // Create a separator
        var separator = document.createElement("hr");
        separator.className = "separator";

        // Append the separator and the button to the container
        readyButtonContainer.appendChild(separator);
        readyButtonContainer.appendChild(readyButton);

        // Append the container to the body of the document
        // You can change 'document.body' to append it to a different element
        document.body.appendChild(readyButtonContainer);

        // Add a click event listener to the button
        readyButton.addEventListener('click', () => displayAgePanel()); //exportReactions());
    }
}

function addMessagesToDOM(messagesJson) {
    console.log(messagesJson)
    var parsedData = JSON.parse(messagesJson)
    console.log(parsedData.messages[0].text)

    parsedData.messages.forEach(message => {
        // Create divs and other elements
        let bubbleWrapper = document.createElement('div');
        bubbleWrapper.className = 'bubbleWrapper';
        let inlineContainer = document.createElement('div');

        if (message.who == "robot") {
            inlineContainer.className = 'inlineContainer';

            let icon = document.createElement('img');
            icon.className = 'inlineIcon';
            icon.src = 'src/rb.png'; // Adjust the source as needed

            let bubble = document.createElement('div');
            bubble.className = 'otherBubble other';
            bubble.id = message.id;
            bubble.textContent = message.text;
            bubble.addEventListener('click', () => openEmojiPicker(message.id));

            let reaction = document.createElement('div');
            reaction.id = message.emojiId;
            reaction.className = 'reaction';

            let emojiOverlay = document.createElement('div');
            emojiOverlay.className = 'emojiOverlay';

            // Append child elements
            reaction.appendChild(emojiOverlay);
            bubble.appendChild(reaction);
            inlineContainer.appendChild(icon);
            inlineContainer.appendChild(bubble);
            bubbleWrapper.appendChild(inlineContainer);
        }

        if (message.who == "kid") {
            inlineContainer.className = 'inlineContainer own';
            let icon = document.createElement('img');
            icon.className = 'inlineIcon';
            icon.src = 'src/jij.png'; // Adjust the source as needed

            let bubble = document.createElement('div');
            bubble.className = 'ownBubble own';
            bubble.id = message.id;
            bubble.textContent = message.text;

            inlineContainer.appendChild(icon);
            inlineContainer.appendChild(bubble);
            bubbleWrapper.appendChild(inlineContainer);
        }

        reactions[message.id] = {
            reaction: message.emojiId,
            text: '',
            utterance: message.text, // Store the text it was based on
            startTime: message.startTime, // Add a startTime property if needed
            who: message.who// Add other properties as needed
        };

        // Append to document
        // Assuming bubbleWrapper is already defined and you want to append it to a div with the id 'chat'
        var chatDiv = document.getElementById('chatContainer');
        chatDiv.appendChild(bubbleWrapper);
        updateSingleReaction(message.id);
    });
}

function openEmojiPicker(messageId) {//, existingReaction = '') {
    // BLUR CODE
    // Set the currently editing message ID globally
    currentlyEditingMessageId = messageId;

    // Apply blur to other elements
    const allMessages = document.querySelectorAll('.otherBubble, .ownBubble, .inlineIcon, .inlineContainer.own, .readyButton');
    allMessages.forEach(message => {
        if (message.id != "" && message.id != messageId) {
            message.classList.add('blur');
        }
    });
    ///// END of blur CODE

    const readyButton = document.getElementById('readyButton');
    if (readyButton) {
        readyButton.style.display = 'none';
    }

    // Get the position of the selected message
    const selectedMessage = document.getElementById(messageId);
    if (!selectedMessage) {
        return; // Handle the case where the selected message is not found
    }

    const selectedMessageRect = selectedMessage.getBoundingClientRect();

    // Calculate the position for the emoji picker
    const emojiPicker = document.getElementById('emojiPicker');
    if (emojiPicker) {
        emojiPicker.style.display = 'block';
        emojiPicker.dataset.messageId = messageId;
        let topPosition = selectedMessageRect.top + 120; // Initial offset, adjust as needed
        let leftPosition = selectedMessageRect.right + 232; // Adjust the offset as needed

        // Ensure the emoji picker does not exceed the screen height
        const screenHeight = window.innerHeight;
        const pickerHeight = emojiPicker.offsetHeight;

        if (topPosition + pickerHeight > screenHeight) {
            topPosition = screenHeight - pickerHeight;
        }
        const minimumTopOffset = 20; // Adjust as needed
        topPosition = Math.max(minimumTopOffset, topPosition);


        // Set the final position of the emoji picker
        emojiPicker.style.top = `${Math.max(0, topPosition)}px`;
        emojiPicker.style.left = `${Math.max(0, leftPosition)}px`;
    }

    if (!reactions[messageId]) {
        reactions[messageId] = { reaction: '' };
    }

    const existingReaction = reactions[messageId].reaction;

    const emojiContainer = document.querySelector('.emojiContainer');
    emojiContainer.innerHTML = '';

    emojiList.forEach(emoji => {
        const emojiSpan = document.createElement('span');
        emojiSpan.classList.add('emoji');
        emojiSpan.textContent = emoji;
        emojiSpan.dataset.emoji = emoji;

        if (existingReaction === emoji) {
            emojiSpan.classList.add('selected');
        }

        emojiSpan.onclick = () => selectEmoji(emoji);
        emojiContainer.appendChild(emojiSpan);
    });

    const existingText = reactions[messageId].text || '';

    const reactionTextField = document.getElementById('additionalTextField');
    if (reactionTextField) {
        reactionTextField.value = existingText;
    }

    let rem = document.getElementById("removeButton")
    rem.addEventListener('click', () => deselectEmoji());

    let close = document.getElementById("closeButton")
    close.addEventListener('click', () => closeEmojiPicker());

    isEmojiPickerOpen = true;

}

function closeEmojiPicker() {
    const messageId = document.getElementById('emojiPicker').dataset.messageId;
    const reactionTextField = document.getElementById('additionalTextField');

    if (messageId && reactionTextField) {
        reactions[messageId].text = reactionTextField.value;
    }

    updateReactions();

    // Remove .editing class from the currently edited message
    const currentlyEditingMessage = document.getElementById(currentlyEditingMessageId);
    if (currentlyEditingMessage) {
        currentlyEditingMessage.classList.remove('editing');
    }

    // Reset the currently editing message ID globally
    currentlyEditingMessageId = null;

    // Remove blur from other elements
    const blurElements = document.querySelectorAll('.blur');
    blurElements.forEach(element => {
        element.classList.remove('blur');
    });
    // END OF BLUR CODE

    const readyButton = document.getElementById('readyButton');
    if (readyButton) {
        readyButton.disabled = false;
        readyButton.style.display = 'block';
    }

    const emojiPicker = document.getElementById('emojiPicker');
    emojiPicker.style.display = 'none';

    updateReactions();

    isEmojiPickerOpen = false;
}

function selectEmoji(emoji) {
    const messageId = document.getElementById('emojiPicker').dataset.messageId;
    const emojiContainer = document.querySelector('.emojiContainer');

    // Remove the 'selected' class from all emojis
    emojiContainer.querySelectorAll('.emoji').forEach(el => el.classList.remove('selected'));

    // Add the 'selected' class to the clicked emoji
    const selectedEmoji = emojiContainer.querySelector(`[data-emoji="${emoji}"]`);
    if (selectedEmoji) {
        selectedEmoji.classList.add('selected');
    }
    reactions[messageId].reaction = emoji; // new
    updateReactions();
}

function deselectEmoji() {
    const messageId = document.getElementById('emojiPicker').dataset.messageId;
    const emojiContainer = document.querySelector('.emojiContainer');
    const reactionTextField = document.getElementById('additionalTextField');
    // Remove the 'selected' class from all emojis
    emojiContainer.querySelectorAll('.emoji').forEach(el => el.classList.remove('selected'));

    // clear textfield
    reactionTextField.value = '';

    reactions[messageId] = {
        emoji: "", // Set the emoji to an empty string
        text: "",  // Set the text to an empty string
    };
    closeEmojiPicker();
}

function updateReactions() {
    const messageId = document.getElementById('emojiPicker').dataset.messageId;
    const message = document.getElementById(messageId);
    const emojiOverlay = message ? message.querySelector('.emojiOverlay') : null;

    if (emojiOverlay) {
        const { reaction } = reactions[messageId];

        if (reaction) {
            emojiOverlay.textContent = reaction;
            emojiOverlay.style.display = 'block';
        } else {
            emojiOverlay.textContent = '';
            emojiOverlay.style.display = 'none';
        }
    }
}

function updateSingleReaction(messageId) {
    const message = document.getElementById(messageId);
    const emojiOverlay = message ? message.querySelector('.emojiOverlay') : null;

    if (emojiOverlay) {
        const { reaction } = reactions[messageId];

        if (reaction) {
            emojiOverlay.textContent = reaction;
            emojiOverlay.style.display = 'block';
        } else {
            emojiOverlay.textContent = '';
            emojiOverlay.style.display = 'none';
        }
    }
}

function displayAgePanel() {
    // Check if the age panel is already visible, if so, do nothing
    if (agePanelVisible) {
        console.log("AGE PANEL ALREADY VISIBLE");
        return;
    }

    // Create an overlay container for the age panel
    const overlayContainer = document.createElement("div");
    overlayContainer.id = "overlayContainer";

    // Create a container for the age input, confirm button, and back button
    const agePanelContainer = document.createElement("div");
    agePanelContainer.id = "agePanelContainer";

    // Create a label for the age input
    const ageInputLabel = document.createElement("label");
    ageInputLabel.innerText = "Vul je leeftijd in:";
    agePanelContainer.appendChild(ageInputLabel);

    // Create an input field for age
    const ageInput = document.createElement("input");
    ageInput.type = "number";
    ageInput.id = "ageInput";
    agePanelContainer.appendChild(ageInput);

    // Create a confirm button
    const confirmButton = document.createElement("button");
    confirmButton.innerText = "Ok";
    confirmButton.id = "confirmButton";
    agePanelContainer.appendChild(confirmButton);

    // Create a back button
    const backButton = document.createElement("button");
    backButton.innerText = "Terug";
    backButton.id = "backButton";
    agePanelContainer.appendChild(backButton);

    // Append the age panel container to the overlay container
    overlayContainer.appendChild(agePanelContainer);

    // Append the overlay container to the body
    document.body.appendChild(overlayContainer);

    // Set agePanelVisible to true
    agePanelVisible = true;

    // Add event listener for the confirm button
    confirmButton.addEventListener('click', () => {
        var ageValue = document.getElementById("ageInput").value;
        if (!isNaN(ageValue) && ageValue >= 0) { /// NAN CHECK NOT WORKIGN IN FIREFOX
            // Age is valid, store it and hide the age panel
            age = ageValue;
            overlayContainer.style.display = 'none';
            agePanelVisible = false;
            agePanelContainer.innerHTML = '';
            exportReactions();

        } else {
            // Invalid age, show an error message (you can customize this)
            console.log('invalid age');
            alert("Gebruik een nummer");
        }
    });

    // Add event listener for the back button
    backButton.addEventListener('click', () => {
        // Hide the overlay
        overlayContainer.style.display = 'none';
        agePanelVisible = false;
    });
}

function exportReactions() {
    console.log("Confirm pressed", age);
    const jsonReactions = JSON.stringify({
        reactions: reactions,
        age: age // Include the age in the data
    });

    //console.log("LAATSTE AGE: ", age);

    furhat.send({
        event_name: "ClickButton",
        data: jsonReactions
    })
    console.log(jsonReactions);
    displayStartButton();
}


// toggle the blur class on the body
function addBlur() {
    document.body.classList.add('blur');
}

function removeBlur() {
    document.body.classList.remove('blur');
}

