AutoReflected_2In -> B1_v2.4		Design Spark

--This stems from B1_v2.3
--This interworks with 
--This interworks with Arduino "2I_InfoRem_v1.32" or anything in accordance with the new functionality of this PCB
--

***Feature
--Fixing the RST pin issue happening since B1_v2.2. The design wasn't working well because when the NodeMCU was just ON, D7 plays the "subtle" role of a 3.3V whenever there is a load, which there are in my case, and that actually gives a HIGH state to the RST button. All that happens before the sketch executes and have the chance to set D7 to LOW.
FIXING WAYS ?
>> One was is to connect the RST to D9 or D10 pins (which are set to ON when the NodeMCU starts) through 2 cascaded NOT gates. 
potential problem : Any silly things might happen due to response delay in the BJTs. In fact, when the NodeMCU just starts, for any critical race reason, RST might consider for a time lapse that it is ON. This problem stalls the NodeMCU completely.
At all cases, simplifying the handling of RST is of huge interest. And any solution must be well monitored, not to cause any troubles.
>> A better way is to connect RST pin to D9 (or D10, but D9 is better - check note in .sch file) pin through a 1K resistor. When the sketch program executes we explicitly set D9 to HIGH. And when we want to make a reset, we set D9 to LOW for say 100 ms then to HIGH.
This notes to be working for 1K but not for 10K. With 10K, when D9 is LOW, RST doesn't sense LOW.
^^ I havent' seen a problem when uploading a sketch whilst the NodeMCU is on the PCB.


***Is this a major-amendment version?
--NO.

***Main Goal for the electronic design of the PCB
--

***Details
--

