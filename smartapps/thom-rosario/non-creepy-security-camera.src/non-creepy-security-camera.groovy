/**
 *  Non-Creepy Security Camera
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *  Based on 
 *  "Foscam Mode Alarm" and "Foscam Presence Alarm"     Copyright 2014 skp19 and
 *  "Photo Burst When..."   							Copyright 2013 SmartThings
 *
 *  I wanted a non-creepy security camera that would avert it's eyes while I was home.  It also enables/disables the camera motion detection.
 *  https://youtu.be/jHsbwY4EPyA?t=25
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

definition(
    name: "Non-Creepy Security Camera",
    namespace: "Thom Rosario",
    author: "Thom Rosario",
    category: "Safety & Security",
    description: "Using the Foscam Universal Device Handler created by skp19, this smart app moves your camera to a preset position based on different events.",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/camera.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/camera@2x.png"
)

preferences {
	section("When this happens...") {
		input ("stMode", "mode", multiple: true, title:"This mode activates")
        input "presence", "capability.presenceSensor", title: "These people are present", required: false, multiple: true
	}
    section("Do these things...") {
		input ("newPosition", "number", title:"Where should I move?", required: true, defaultValue: "3")
		input ("origPosition", "number", title: "Where should I return to when I'm done?", required: true, defaultValue: "1")
		input("recipients", "contact", title: "Who should I notify?") {
            input "phone", "phone", title: "Send with text message (optional)",
                description: "Phone Number", required: false
			}
	}
    section("To this camera"){
		input ("camera", "capability.imageCapture", multiple: false, title:"Which camera?")
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	state.position = 0
	init()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	init()
}

def init() {
    subscribe(location, "mode", moveHandler)
	subscribe(presence, "presence", moveHandler)
	subscribe(presence, "presence", presenceHandler)
	log.debug "init:  Current mode = ${location.mode}, people = ${presence.collect{it.label + ': ' + it.currentPresence}} & position = ${state.position}"
}

def modeHandler(evt) {
	// handle mode changes
}

def presenceHandler(evt) {
	// handle presence mode changes
	//log.debug "presenceHandler:  people = ${presence.collect{it.label + ': ' + it.currentPresence}} and event = ${evt}"
}

def notificationHandler(msg) {
	// handle notifications
	if (location.contactBookEnabled && recipients) {
		log.debug "Contact Book enabled!"
	    sendNotificationToContacts(msg, recipients)
	} 
    else if (phone) { 
    	// check that the user did select a phone number
		log.debug "Contact Book not enabled."
	    sendSms(phone, msg)
	}
}

def moveHandler(evt) {
	camera?.ledAuto()
	def nobodyHome = presence.find{it.currentPresence == "present"} == null
	def somebodyHome = presence.find{it.currentPresence == "present"}
	def wrongPosition = true
	log.debug "moveHandler: Presence -- nobodyHome = ${nobodyHome} & somebodyHome = ${somebodyHome}"
    if (evt.value in stMode || somebodyHome) {
		wrongPosition = (state.position != newPosition)
		if (wrongPosition) {
	        log.debug "moveHandler:  moving to position $newPosition & disabling alarm"
	    	camera?.alarmOff()
			switch (newPosition) {
			    case "1":
			        camera?.preset1()
			        break
			    case "2":
			        camera?.preset2()
			        break
			    case "3":
			        camera?.preset3()
			        break
			    case "4":
			        camera?.preset4()
			        break
			    case "5":
			        camera?.preset5()
			        break
			    case "6":
			        camera?.preset6()
			        break
			    default:
			        camera?.preset1()
			} // end of switch	
			state.position = newPosition
			notificationHandler("${camera} is moving to position ${settings.newPosition} & alarm is off.")
			} // end of handling wrongPosition
    } // end of handling someone home
    else if (!(evt.value in stMode) || nobodyHome) {	
		wrongPosition = (state.position != origPosition)
		if (wrongPosition) {
	        log.debug "moveHandler:  Returning to position $origPosition & re-arming"
			switch (origPosition) {
			    case "1":
			        camera?.preset1()
			        break
			    case "2":
			        camera?.preset2()
			        break
			    case "3":
			        camera?.preset3()
			        break
			    case "4":
			        camera?.preset4()
			        break
			    case "5":
			        camera?.preset6()
			        break
			    case "6":
			        camera?.preset6()
			        break
			    default:
			        camera?.preset1()
			}
			state.position = settings.origPosition
			notificationHandler("${camera} is moving to position ${settings.origPosition} & alarm is on.")
	    	camera?.alarmOn()
		} // end of wrongPosition check
    }
}