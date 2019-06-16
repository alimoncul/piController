#!/usr/bin/python
#-*- coding: utf-8 -*-
import time
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
# StepMotor Başlatılıyor...
control_pins = [0, 5, 6, 13]  # Step-motor pinleri
for pin in control_pins:  # for dongusu ile pinleri setliyoruz
    GPIO.setup(pin, GPIO.OUT)
    GPIO.output(pin, 0)

def step_motor_saat_yonunde():
    step=[[1, 1, 0, 0], [0, 1, 1, 0], [0, 0, 1, 1], [1, 0, 0, 1]]
    global control_pins
    for i in range(2):
        for halfstep in range(4):
            for pin in range(4):
                GPIO.output(control_pins[pin], step[halfstep][pin])
                time.sleep(0.0048)
    time.sleep(0.25)

def step_motor_saat_yonuters():
    step=[[1, 1, 0, 0], [0, 1, 1, 0], [0, 0, 1, 1], [1, 0, 0, 1]]
    global control_pins
    for i in range(4):
        for halfstep in range(4):
            for pin in range(3,-1,-1):
                GPIO.output(control_pins[pin], step[3-halfstep][pin])
                time.sleep(0.0048)
    time.sleep(0.25)

while True:
    step_motor_saat_yonunde()
    step_motor_saat_yonuters()
    step_motor_saat_yonunde()

