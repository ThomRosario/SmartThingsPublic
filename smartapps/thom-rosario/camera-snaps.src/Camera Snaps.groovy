/**
 *  Camera Snaps
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *  Based on 
 *  "Foscam Mode Alarm"     Copyright 2014 skp19 and
 *  "Photo Burst When..."   Copyright 2013 SmartThings
 *
 *  Take snapshots each camera preset position whenever there's something going on.
 *
 *  TODO:  have scheduleHander write a list that tells what position and snap and move to and then 
 *         have snapHandler read the list, and increment state variable showing which picture we took
 *         write a state variable of the starting position and move back there when done
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
    name: "Camera Snaps All Presets",
	parent: "burrow:Smart Burrow",
    namespace: "burrow",
	parent: "burrow:Smart Burrow",
    author: "Thom Rosario",
    category: "Safety & Security",
    description: "Snap photos of Foscam preset positions whenever there's activity.",
    iconUrl: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment9-icn@2x.png"
)

preferences {
	section("When this happens...") {
		input("motionSensors", "capability.motionSensor", required: true, title: "... motion here", multiple: true)
        input("contactSensors", "capability.contactSensor", title: "... or this sensor opens", required: false, multiple: true)
	}
    section("Do this") {
		input("camera", "capability.imageCapture", required: true, title: "Snap a photo on this camera...", multiple: true)
		input ("returnPosition", "number", title: "... and return the camera to this preset position.", required: false, defaultValue: "1")
	}
	section("Snap mode specifics") {
		input ("camMoveDelay", "number", title: "Wait how many seconds between taking photos (no less than 5)?", required: false, defaultValue: "10")
		input ("numPresets", "number", title: "How many preset positions should we snap (typically 3)?", required: false, defaultValue: "3")
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motionSensors, "motion.active", scheduleHandler)
	subscribe(contactSensors, "contact.open", scheduleHandler)
	state.shutterDelay = 2 // give the camera time to snap the photo before moving it again
	state.presetList = [settings.returnPosition]
	log.debug "initialize:  presetList = ${state.presetList}"
}

def scheduleHandler (evt) {
	log.debug "scheduleHandler called: ${evt.value}"
	camera?.ledAuto()
	state.presetIndex = 0
	// camera?.alarmOff()
	// init these variables every time we see motion
	def schedTime = 0 // number of seconds in the future to snap & move
	state.presetNum = 0 // the preset number to move to; 0 means don't move
	log.debug "scheduleHandler: setting presetList[0] to returnPosition -- ${state.presetList[0]}"

	// check the camMoveDelay user chose before using it; this is amount of time we give the camera to move positions
	if (settings.camMoveDelay < 5) {
		settings.camMoveDelay = 5
		log.debug "scheduleHandler:  camMoveDelay was < ${camMoveDelay}; setting to ${camMoveDelay}"
	}
	log.debug "scheduleHandler:  state.presetNum = ${state.presetNum}; camMoveDelay = ${camMoveDelay}"	
	
	// set the snap and move routine
	for (int i = 1; i < numPresets + 2; i++) {
		schedTime = i * camMoveDelay // schedule moves to occur increasingly further future times
		state.presetList << i
		log.debug "scheduleHandler:  schedTime = ${schedTime} & increment = ${i}"
		runIn(schedTime, snapHandler, [overwrite: false])
	}
}

def snapHandler() {
	camera?.take()
	switch (state.presetNum) {
	    case "0":
			log.debug "snapHandler:  snapping original position. state.presetNum = ${state.presetNum}"
			state.presetNum = state.presetNum + 1
	        break
		case "${numPresets + 1}":
			log.debug "snapHandler:  on last loop; restoring position; state.presetNum = ${state.presetNum}"
			state.presetNum = returnPosition
			break
	    default:
			log.debug "snapHandler:  snapping a photo. state.presetNum = ${state.presetNum}"
			state.presetNum = state.presetNum + 1
	}		
	runIn(state.shutterDelay, moveHandler, [overwrite: false])
}

def moveHandler() {
	// moves the camera
	log.debug "moveHandler:  moving the camera.  state.presetNum = ${state.presetNum}"
	switch (state.presetNum) {
	    case "0":
	        // don't move; stay here.
	        break
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
	}
	state.presetIndex = state.presetIndex + 1
	log.debug "moveHandler:  state.presetIndex = ${state.presetIndex}"
}
