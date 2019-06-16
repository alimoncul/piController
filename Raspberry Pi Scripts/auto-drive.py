import random
from gpiozero import DistanceSensor
import sys
import time
import gps
import py_qmc5883l
from socket import *
from math import *
import RPi.GPIO as GPIO

# *-------Hedef göndermek için--------*
tcpTargetSocket = socket(AF_INET, SOCK_STREAM)
addressTarget = (('', 10255))
tcpTargetSocket.bind(addressTarget)
tcpTargetSocket.listen(1)
# *-------------------------*

# *------Start komutunu almak için----------*
tcpStartSocket = socket(AF_INET, SOCK_STREAM)
addressCommand = (('', 10202))
tcpStartSocket.bind(addressCommand)
tcpStartSocket.listen(1)
# *-------------------------*

# *-------Manyetometre için----------*
sensor = py_qmc5883l.QMC5883L()
sensor.calibration = [[1.0027893959888179, -0.01014176445068371, -212.7622017592909],[-0.01014176445068371, 1.036873712655171, 355.2126029765021],[0.0, 0.0, 1.0]]
sensor.set_declination(5.11)
# *-------------------------*

# *-----------Motor için-------------*
GPIO.setmode(GPIO.BCM)  # GPIO isimleri ile pinleri seçiyoruz
GPIO.setwarnings(False)
leftForward = 17
leftBackward = 27
rightForward = 23
rightBackward = 22
GPIO.setup(leftForward, GPIO.OUT)  # sol-ileri 17
GPIO.setup(leftBackward, GPIO.OUT)  # sol-geri 27
GPIO.setup(rightForward, GPIO.OUT)  # sag-ileri 23
GPIO.setup(rightBackward, GPIO.OUT)  # sag-geri 22
# *-------------------------*

def calculateDistance(vehicleLocation, targetLocation):
    # iki nokta arası uzaklığı hesaplıyor
    lat1 = radians(vehicleLocation[0])
    lon1 = radians(vehicleLocation[1])
    lat2 = radians(targetLocation[0])
    lon2 = radians(targetLocation[1])
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return 6373.0*c*1000

def getUltrasonicSensorData(sensor=DistanceSensor(echo=20, trigger=21, max_distance=4)):
    # Ultrasonik sensörün uzaklığını alır
    time.sleep(0.2)
    return sensor.distance * 100

def getHeading():
    # Araç heading'ini alır
    heading = sensor.get_bearing()
    return heading

def getVehicleLocation():
    # Araç lokasyonunu alır
    session = gps.gps("localhost", "2947")
    session.stream(gps.WATCH_ENABLE | gps.WATCH_NEWSTYLE)
    while True:
        rep = session.next()
        try:
            if (rep["class"] == "TPV"):
                message = str(rep.lat) + "/" + str(rep.lon)
                return [float(x) for x in message.split("/")]
        except Exception as e:
            message = "noGPS"
            return message

def getTargetLocation():
    # Hedef lokasyonu alır
    tcpTargetSock, addr = tcpTargetSocket.accept()
    data = tcpTargetSock.recv(1024)
    data = data.decode("utf-8")
    tcpTargetSock.close()
    return [float(x) for x in data.split("/")]

def sendStatus(status):
    # Telefona durum bilgisini yollar
    UDP_IP = "<broadcast>"
    UDP_PORT = 24999
    sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
    sock.settimeout(5)
    sock.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)
    sock.sendto(str.encode(status), (UDP_IP, UDP_PORT))

def getCommand():
    # Start komutunu bekler
    tcpStartSock, addr = tcpStartSocket.accept()
    data = tcpStartSock.recv(1024)
    data = data.decode("utf-8")
    tcpStartSock.close()
    return data

def emergencyStop():
	# Aracı durdurur
    GPIO.output(leftForward, GPIO.LOW)
    GPIO.output(rightForward, GPIO.LOW)
    GPIO.output(leftBackward, GPIO.LOW)
    GPIO.output(rightBackward, GPIO.LOW)

def turnRight():
	# Aracı sağa döndürür
    GPIO.output(leftForward, GPIO.HIGH)
    GPIO.output(rightBackward, GPIO.HIGH)
    time.sleep(0.05)
    GPIO.output(leftForward, GPIO.LOW)
    GPIO.output(rightBackward, GPIO.LOW)

def goForward():
	# Aracı ileriye doğru hareket ettirir
    GPIO.output(leftForward, GPIO.HIGH)
    GPIO.output(rightForward, GPIO.HIGH)

def calcBearing(lat1, lon1, lat2, lon2):
    # Yatay azimut hesaplar
    dLon = lon2 - lon1
    y = sin(dLon) * cos(lat2)
    x = cos(lat1) * sin(lat2) \
        - sin(lat1) * cos(lat2) * cos(dLon)
    return atan2(y, x)

def turnToTarget(livePosition, targetPosition):
    # Aracı hedefe doğru döndürür
    sendStatus("TURNING")
    deviceLon = radians(float(livePosition[1]))
    deviceLat = radians(float(livePosition[0]))
    targetLon = radians(float(targetPosition[1]))
    targetLat = radians(float(targetPosition[0]))
    Bearing = calcBearing(deviceLat, deviceLon, targetLat, targetLon)
    Bearing = degrees(Bearing)
    # Aracın baş kısmı (header) derecesinden, yatay azimut derecesini çıkarıp cihazı hedefe doğru döndürür.
    deviceHeader = getHeading()
    differenceToAzimuth = abs(deviceHeader - Bearing)
    while differenceToAzimuth > 4:
        turnRight()
        time.sleep(0.1)
		# Aracın baş kısmını her sağ döndürmeden sonra tekrardan günceller.
        deviceHeader = getHeading()
        differenceToAzimuth = abs(deviceHeader - Bearing)
        differenceToAzimuth = differenceToAzimuth % 360
        time.sleep(0.5)

while True:
    targetLocation = getTargetLocation()
    vehicleLocation = getVehicleLocation()
    sendStatus("TARGETSEND")

    while True:
        command=getCommand()
        if "go" in command:
            turnToTarget(vehicleLocation,targetLocation)
            goForward()
            sendStatus("VEHICLESTARTED")
            while True:
                sensorDistance=getUltrasonicSensorData()
                if(sensorDistance<60):
                    emergencyStop()
                    sendStatus("OBSTACLE")
                    break
                distance=calculateDistance(getVehicleLocation(), targetLocation)
                if(distance<5):
                    emergencyStop()
		    sendStatus("ARRIVED")
                    break
            time.sleep(5)
            sendStatus("READY")
            break
