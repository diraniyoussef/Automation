# Automation
automation based on WiFi (and/or Internet) - communication between 2 panels (extendable) and an android mobile app can monitor the system.

So impressive project. I'm so proud to share it with you ! It may outcome some commercial products...

Based on esp8266 (NodeMCU actually).

Two general purpose pins on a panel when the state of either of them is changed (from on to off or vice versa), then communicate with another panel this new state. The mobile communicates with the panel that had received the info.

If you understood the code you may customize it as you like.

The code I will be putting is for 2 pins communicated thru their panel to another 2 pins on another panel.

Any questions are welcome.

One day I may make a video about it. In fact one video is already made. check my profile for a vimeo link.

In general, it's about Arduino IDE for esp8266 code for the 2 panels, some DesignSpark electronics files, android studio code for the mobile app, and a java standalone app for the intermediate (relay) server in the middle that will assure communication of the 2 panels and the mobile app.
The Intermediate server has an SQLite database for authentication... System is robust.

BTW, the system supports local communication, thus without the intermediate server.
