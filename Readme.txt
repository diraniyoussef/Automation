ControlLinker_v0.8

--This version stems from ControlLinker_v0.7
--This version is intended to run on the XEN VPS 

***Features
--If same full message (and not just same signature) is received on the other socket, say within 15 seconds, then kill the old socket right away (you may as well wait 4 seconds if you want) and don't wait 2 minutes for automatic kill. This problem was raised when the mobile app can switch orientation, thus resending the same message on another socket and by consequence makes threads leak ! (I haven't noticed any leak caused by the module - NodeMCU).
Implementing it is difficult since I would have to remember a number of messages (say 15) per sender (mob or mod) in memory, and each message has its own timer of 15 seconds. Then the sender's messages will be analyzed and compared to all the remembered recorded messages. -CANCELLED
Since the implementation is difficult, I will try work on the mobile app. 
--I don't want to record in the NodeMCU, a large array of the users, like 1000, 2000 users, who can send request to it. 
Especially that the NodeMCU is mainly replying with one message, which is the report. So no need to record many users. Just one user (any user) is enough. It will be necessary to name our NodeMCU a special name like "broad1" or "broad2". When a message is sent from a "broad" sender, the receiver would be any user, so we can't rely on the receiver to forward the message because it's fake! Instead, we will search all the printers (who are previous senders) for their receivers, if the receiver matches "broad" then we forward the message to that printer. I.e. we forward the message to who had already mentioned our broad.
A lot of cases fall in question. Like injection? Multiple surjections? etc. We won't care for now for these special embarrassing cases.
Why this note in the first place?
Say our panel will be located somewhere far from my reach. And I have allocated like 200 users (assume the memory isn't complaining). What to do to add some more users? OTA?
This looks like a decent solution. But since allocating 200 users in Arduino code was barely perceptible (not problematic at all) so TO BE IMPLEMENTED LATER
--I don't find it meaningful to communicate on 2 ports for mob and 2 other ports for mod. I will use just 3552 and 3553.
--


***TO BE MADE LATER
--For authentication purpose, add, in the correct place in BufferListeners.java, a check of the incoming message that the sender is not "dummy", and if it were then it will discard it (and probably consider a security threat).
Also, make sure the sender is not the same as the receiver in the incoming message (I don't know if this is already taken of). BTW, this is taken care of in the database itself! So it's wrong to add such a constraint. This must be left to Youssef to add into the database.




