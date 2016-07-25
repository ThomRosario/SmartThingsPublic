/**
 *  Laundry Reminder
 *  Version 1.0
 *  Copyright 2016 Thom Rosario
 *
 *  This app will watch the vibration sensor for activity, and send a reminder at a chosen time of 
 *  the day to remind us to check the laundry so we don't leave anything in it overnight.  I've set my 
 *  Monoprice Shock Sensor (http://www.monoprice.com/product?p_id=11988) up as a "Z-Wave Door/Window Sensor", 
 *  as that's the only consistent way I could get SmartThings to acknowledge the vibrations.  "Open" equates to 
 *  vibration, and "Closed" is interpreted as no vibration.
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
    name: "Laundry Reminder",
    namespace: "Thom Rosario",
    author: "Thom Rosario",
    category: "Safety & Security",
    description: "Give yourself a reminder to check the washer before bed.",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/camera.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/camera@2x.png"
)

preferences {
    section("Which vibration sensor?") {
      input("washerContact", "capability.contactSensor", title: "Pick the sensor", required: true, multiple: false)
    }
	section("How long is your typical wash cycle?") {
		input ("normalWashLength", "text", title:"How many minutes does your laundry take?", required: true, defaultValue: "15")
	}
	section("What time do you want to be notified?") {
		input("notifyTime", "time", title: "Enter a time to be notified.", required: true)
	}
	section("Who do you want to notify?") {
		input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Send with text message (optional)",
                description: "Phone Number", required: false
			}
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
	subscribe(washerContact, "contact", washerHandler)
	schedule(notifyTime, notificationHandler)
	//def cycleMinutes = normalWashLength * minutes
	log.debug "Set washer cycle time to $normalWashLength minutes."
}

def washerHandler(evt) {
    state.washDay = true // debugging 
    if (evt.value == "open") {
      // contact was opened, must be doing laundry.  Need to time it.
      log.debug "Contact is ${evt.value} -- going to check motion in $normalWashLength minutes."
	  //state.startDate = evt.date.time
	  //log.debug "state.startDate = ${state.startDate}"
	  runIn(normalWashLength * minutes, checkMotion)
  }
  else {
      // contact was closed, make note of the time and tell the app to run later and check how long we ran
      log.debug "Contact is ${evt.value}"
	  //state.stopDate = evt.date.time
	  //log.debug "state.stopDate = ${state.stopDate}"
	  /*
	  def washLength = state.stopDate - state.startDate
	  if (washLength ≥ normalWashLength) {
		  state.washDay = true
	  } 
	  else {		  
		  // the movement wasn't long enough to be considered a wash day
		  log.debug "Movement stopped, but was shorter than a wash cycle.  washLength = $washLength and normalWashLength = $normalWashLength"
	  }	*/
   }
}

def checkMotion() {
	// running via schedule to see if the washer's still running.
    log.debug "In checkMotion scheduled method"
	def laundryState = washerContact.currentState("contact")
	if (laundryState == "open") {
		/* 
			This handler's called after the user-defined amount of time has passed.  The assumption is that
			if we're in this handler and the contact's open again, it's most likely an active wash cycle.  It's
			possible that the movement could have been two distinct temporary motions, but that's an edge case
			that I'm willing to discount for our purposes.
		*/
		log.debug "Movement's still going after $normalWashLength minutes."
		state.washDay = true
	}
	else {
		// The contact's closed; must have been a temporary movement; doesn't count as a cycle.
		log.debug "Movement ended without reaching a full cycle; doesn't count."
	}
}

def notificationHandler(evt) {
	// this is where I'll decide how to handle what happens at the user's specified time
	log.debug "We're inside the notification handler."
	if (state.washDay) then {
		if (location.contactBookEnabled && recipients) {
			log.debug "Contact Book enabled!"
		    sendNotificationToContacts("Don't forget to check the washer.", recipients)
		} 
	    else if (phone) { 
	    	// check that the user did select a phone number
			log.debug "Contact Book not enabled."
		    sendSms(phone, "Don't forget to check the washer.")
		}
		state.washDay = false // resetting the flag for tomorrow
	} 
	else {
		// didn't do any wash today; nothing todo today.
		log.debug "Didn't do any wash today.  state.washDay = $state.WashDay"
	}
}