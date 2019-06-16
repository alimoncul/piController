#!/usr/bin/python
#-*- coding: utf-8 -*-
import time
from gpiozero import DistanceSensor
from socket import *
import RPi.GPIO as GPIO
import time
import threading

# Motorlar Başlatılıyor...
GPIO.setmode(GPIO.BCM) #GPIO isimleri ile pinleri seçiyoruz
leftForward = 17
leftBackward = 27
rightForward = 23
rightBackward = 22
GPIO.setup(leftForward, GPIO.OUT) #sol-ileri 17
GPIO.setup(leftBackward, GPIO.OUT) #sol-geri 27
GPIO.setup(rightForward, GPIO.OUT) #sag-ileri 23
GPIO.setup(rightBackward, GPIO.OUT) #sag-geri 22

def emergencyStop():
    GPIO.output(leftForward, GPIO.LOW)
    GPIO.output(rightForward, GPIO.LOW)
    GPIO.output(leftBackward, GPIO.LOW)
    GPIO.output(rightBackward, GPIO.LOW)
    exit()

#---- Gelen verileri dinlemek için TCP soketi oluşturuyoruz ----
HOST = ''
PORT = 2522
BUFSIZE = 1024
ADDR = (HOST,PORT)

tcpSerSock = socket(AF_INET, SOCK_STREAM)
tcpSerSock.bind(ADDR)
tcpSerSock.listen(1)


while True:
        tcpCliSock,addr = tcpSerSock.accept()
        data = 0
        data = tcpCliSock.recv(BUFSIZE)[0]
        #data: ileri-56 / geri-50 / sağ-54 / sol-52 / dur-48
        if(data==56):
            GPIO.output(leftForward, GPIO.HIGH)
            GPIO.output(rightForward, GPIO.HIGH)
        elif(data==50):
            GPIO.output(leftBackward, GPIO.HIGH)
            GPIO.output(rightBackward, GPIO.HIGH)
        elif(data==54):
            GPIO.output(leftForward, GPIO.HIGH)
            GPIO.output(rightBackward, GPIO.HIGH)
        elif(data==52):
            GPIO.output(leftBackward, GPIO.HIGH)
            GPIO.output(rightForward, GPIO.HIGH)
        elif(data==48):
            GPIO.output(leftBackward, GPIO.LOW)
            GPIO.output(rightBackward, GPIO.LOW)
            GPIO.output(leftForward, GPIO.LOW)
            GPIO.output(rightForward, GPIO.LOW)
        else:
            GPIO.output(leftBackward, GPIO.LOW)
            GPIO.output(rightBackward, GPIO.LOW)
            GPIO.output(leftForward, GPIO.LOW)
            GPIO.output(rightForward, GPIO.LOW)
    
#---------------------------------
