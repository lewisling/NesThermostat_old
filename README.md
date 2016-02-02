# NesThermostat [![Build Status](https://travis-ci.org/vchatela/NesThermostat.svg?branch=master)](https://travis-ci.org/vchatela/NesThermostat)
The android application to manage the Heater on the local server (at home)

![Screenshot](http://s21.postimg.org/jkomfcfvr/Screenshot_2015_12_03_15_09_08.png)

## Features
- A first Page which contain all the rooms
- An activty by room, which allow to turn on/off the heater in the room and fix the requiered temp. 

### Update
- Use SSH instead of php request to secure the connection and avoid externals attacks.
- Add POJO management to parse Json from openweatherapp API to get weather for the first page
- Use Timer to update all the IHM every 5 seconds


Note : The server will manage the heater to work arround this temperature.

##Tests
###Unit Tests

###Functionnal Tests
Verify the connection by SSH throw the JSch library. 
- Connect to the remote server, launch command and compare the result with the known right value.


#Licence

Copyright 2015 ValentinC

[Creative Common BY-NC-SA](http://creativecommons.org/licenses/by-nc-sa/4.0/legalcode)
