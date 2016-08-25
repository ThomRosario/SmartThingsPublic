/**
 *  Camera IR Modes
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *  Based on 
 *  "Foscam Mode Alarm" and "Foscam Presence Alarm"     Copyright 2014 skp19 and
 *
 *  This app enables or disables the IR sensors on Foscam cameras based on the SmartThings mode your house is in.  
 *  When the camera's light conditions hover around the auto-IR-toggle value, and the motion sensor is armed, the 
 *  camera senses this as motion and triggers the alarm.  This app lets you set the mode based on what mode your 
 *  SmartThings hub is in.  I'll use it in conjunction with a parent app that controls the IR mode based on other 
 *  SmartThings inputs (like doors or motion sensors), which are more reliable than the camera's light sensors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  TODO:  include light readings from sensors?
 */

definition (
    name: "Camera IR Modes",
    namespace: "burrow",
    author: "Thom Rosario",
    category: "Safety & Security",
	parent: "burrow:Smart Burrow",
    description: "Using the Foscam Universal Device Handler created by skp19, this smart app gives you mode-based control over your camera's IR sensor modes.",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/camera.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/camera@2x.png"
)

preferences {
    section ("Which camera?") {
		paragraph ("Pick the camera and modes where you'd like the IR mode to be disabled.  IR mode will change to automatic during other modes.")
		input ("camera", "capability.imageCapture", multiple: false, title:"Choose a camera.")
		input ("irOffModes", "mode", multiple: true, title:"When this mode activates...")
	}
}

def installed () {
	log.debug "Installed with settings: ${settings}"
	init ()
}

def updated () {
	log.debug "Updated with settings: ${settings}"
	unsubscribe ()
	init ()
}

def init () {
    subscribe (location, "mode", irModeHandler)
	irModeHandler ()
}

def irModeHandler () {
	if (location.mode in irOffModes) {
		camera?.ledOff ()
	}
	else {
		camera?.ledAuto ()
	} 
}