/**
 *  Lights While Present
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *
 *  This app turns a switch on when people are present, and turns it off when no-one's home.
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
 *  TODO:  include motion sensors?
 */

definition (
    name: "Lights While Present",
	parent: "burrow:Smart Burrow",
    namespace: "burrow",
    author: "Thom Rosario",
    category: "Convenience",
    description: "This app turns a switch on when people are present, and turns it off when no-one's home.",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light10-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light10-icn@2x.png"
)

preferences {
    section ("Which switch?") {
		paragraph ("This app ensures that lights will be on whenever certain people are present..")
        input ("presence", "capability.presenceSensor", title: "Who?", required: true, multiple: true)
		input ("light", "capability.switch", title: "Which light?", required: false, multiple: true)
		//input ("dimmer", "capability.switchLevel", title: "or this dimmer...", required: false, multiple: true)
		input ("lightOnModes", "mode", title:"During which modes should this light be lit?", required: true, multiple: true)
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
    subscribe (location, "mode", lightHandler)
	subscribe (presence, "presence", lightHandler)	
	lightHandler ()
	//log.debug "init:  Current mode = ${location.mode}, people = ${presence.collect{it.label + ': ' + it.currentpresence}}"
}

def lightHandler (evt) {
	log.debug "lightHandler: entered. event $evt"
	def nobodyHome = presence.find{it.currentPresence == "present"} == null
	//log.debug "lightHandler: nobodyHome = ${nobodyHome}"
	if ((location.mode in lightOnModes) && nobodyHome) {
		light?.off ()
		if (light.find {it.hasCommand ('setLevel')} != null) {
			light?.setLevel (0)
		}
		parent.notificationHandler ("Turning off ${light}.")
		//log.debug "lightHandler: ${light} off"	
	}
	else {
		light?.on ()
		if (light.find {it.hasCommand ('setLevel')} != null) {
			light?.setLevel (100)
		}
		parent.notificationHandler ("Turning on ${light}.")
		//log.debug "lightHandler: ${light} on"	
	} 
}