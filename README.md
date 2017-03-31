# LaunchpadAndroidApp
This app was created for our CPEN 291 Computer Engineering Design Studio I project course. This last project was quite open-ended with a key requirement: integration of an Arduino, Raspberry Pi, web server, and a website or app. Within a group of 6, my partner, Nishat, and I took the role of implementing the app.

The idea for this project was based around the concept of integrating music, computer engineering, and "Internet-of-things." We decided to create a physical launchpad that would use the Arduino and Raspberry Pi to play uploaded sounds. The sounds would be uploaded to the hardware using an Android app.

The role of the app was to essentially send requests to a web server with updated button sounds. The app mimics a physical launchpad where a user can play sounds on the app and "upload" a preferred sound layout to the physical launchpad. The app has a drawer of sounds, and two modes: "sound play" and "sound assignment." When a sound in the drawer is clicked in "sound play" mode, a sound file is played from the web server. In "sound assignment" mode, a preferred sound can be assigned to a button, which can then be clicked to play sounds when toggled back to "sound play" mode. The app allows the user to play multiple sounds simultaneously in response to multiple button presses by using background threads. 

The original app with progressive commits is in a private repository from our class CPEN 291. We transferred the work from the private repository to this public repository so the source code can be visible. The app development process spanned 2 weeks. This included brainstorming the layout and tools required for the app, learning web server communication, and integrating our knowledge and ideas. 

Partner for app development: Nishat Gupta (https://github.com/nishat1)
