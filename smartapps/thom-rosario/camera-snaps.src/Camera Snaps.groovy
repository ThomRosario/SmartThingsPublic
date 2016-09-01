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
		input ("returnPosition", "number", title: "... and return the camera to this preset position.", required: true, defaultValue: "1")
	}
	section("Snap mode specifics") {
		input ("camMoveDelay", "number", title: "Wait how many seconds between taking photos (no less than 5)?", required: false, defaultValue: "10")
		input ("numPresets", "number", title: "How many preset positions should we snap (typically 3)?", required: true, defaultValue: "3")
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
	state.i = 1
}

def scheduleHandler (evt) {
	log.debug "scheduleHandler called: state.i = ${state.i} ${evt}"
	if (state.i <= numPresets) {
		camera?.alarmOn ()
		camera?.ledAuto ()
		moveHandler(state.i)
	}
	else {
		moveHandler(returnPosition)
	}
}

def moveHandler(preset) {
	log.debug "moveHandler:  moving the camera to $preset.  state.i = ${state.i}"
	switch (preset) {
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
	if (state.i <= numPresets) {
		runIn (camMoveDelay, snapHandler)
	}
	else {
		state.i = 1
		camera?.alarmOff ()
	}
}

def snapHandler() {
	camera?.take()
	state.i = state.i + 1
	runIn (state.shutterDelay, scheduleHandler)
	log.debug "snapHandler:  done.  state.i = ${state.i}"
}
