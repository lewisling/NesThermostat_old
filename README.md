# NesThermostat [![Build Status](https://travis-ci.org/vchatela/NesThermostat.svg?branch=master)](https://travis-ci.org/vchatela/NesThermostat)
The android application to manage the Heater on the local server

![Screenshot](https://cdn.rawgit.com/vchatela/NesThermostat/master/raw/Screenshot_2015-12-03-15-09-08.png)

## Features
- A first Page which contain all the rooms
- An activty by room, which allow to turn on/off the heater in the room and to fix the requiered temp. 

### Update
Use SSH instead of php request to secure the connection and avoid externals attacks.

Note : The server will manage the heater to work arround this temperature.

##Tests
###Unit Tests

###Functionnal Tests
Verify the connection by SSH throw the JSch library. 
- Connect to the remote server, launch command and compare the result with the known right value.


#Licence

Copyright 2015 ValentinC

[Creative Common BY-NC-SA](http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode)
