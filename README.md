# piController[WIP]
### A graduation project developed at Çanakkale 18 March University in 2019.

## Getting Started

&nbsp;&nbsp;&nbsp;&nbsp;Our main goal is to create a vehicle which is semi-autonomous and controlled with android device. We can manually control it via using TCP. [Manual Control Script](https://github.com/alimoncul/piController/blob/master/Raspberry%20Pi%20Scripts/wirelessAndroid.py) is always working and listening incoming TCP packets from phone. We are sending 4 different digits 2,4,6,8 as backward, left, right, forward from Android to Pi. When we let go any direction button, it sends digit 0 as to stop the vehicle. 

![preview](/demo/Manual.gif)

<p align="center">
  Demonstration of Manual Controlling
</p>

## Semi-Autonomous Drive
&nbsp;&nbsp;&nbsp;&nbsp;When vehicle is booted [GPS Stream](https://github.com/alimoncul/piController/blob/master/Raspberry%20Pi%20Scripts/gpsStream.py), [Camera Stream](https://github.com/alimoncul/piController/blob/master/Raspberry%20Pi%20Scripts/liveCamera.py), [Stepper Motor Loop](https://github.com/alimoncul/piController/blob/master/Raspberry%20Pi%20Scripts/lcal-sensor.py), [Semi-autonomous Script](https://github.com/alimoncul/piController/blob/master/Raspberry%20Pi%20Scripts/auto-drive.py) Python scripts starts automatically. We can do this by editing /etc/rc.local file on device. On Android application first we hit GPS button and vehicle location shows on map.
Then we pick a target location by either touching on the map or typing on Latitude, Longitude Textboxes. After picking a target location we hit SHOW button. SHOW button sends the selected target location to vehicle and when vehicle receives the target location it notifies the Android application. Then we can hit START button to simply start the process. 

&nbsp;&nbsp;&nbsp;&nbsp;The vehicle computes azimuth to target location and turns its heading to target. When vehicle faces the right way, it starts to move towards the target. As vehicle move towards the target it simultaneously checks for 2 things: Obstacles in front of the vehicle and if it's arrived to target location. 

&nbsp;&nbsp;&nbsp;&nbsp;Obstacle detection is achieved by ultrasonic sensor (HC-SR04). The sensor continuously checks the front, right and left of the vehicle as it is rotated by the stepper mottor attached to it. If it detects obstacle near 60 cm. of the sensor, it counts as the vehicle encountered an obstacle, and stops the autonomus driving. It sends a warning to Android application as to switch to manual driving and the vehicle detected an obstacle. 

&nbsp;&nbsp;&nbsp;&nbsp;If the vehicle doesn't encounter any obstacles, it goes forward to target and continously checking the current GPS location of vehicle and how close it is to target location. When it's 5 meters or closer to target location, it stops and notifies the Android application that the vehicle has arrived to target.

![preview](/demo/autodrive.gif)

<p align="center">
  Demonstration of Semi-Autonomous Drive
</p>


## Authors

* **Alim Öncül** - [alimoncul](https://github.com/alimoncul)
* **Barış Boz** - [bboz](https://github.com/bboz)
* **Eren Kundakçı** - [erenkundakci](https://github.com/erenkundakci)

See also the list of [contributors](https://github.com/alimoncul/piController/contributors) who participated in this project.



