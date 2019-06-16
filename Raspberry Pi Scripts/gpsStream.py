#!/usr/bin/python
import socket
import gps
import time

session = gps.gps("localhost","2947")
session.stream(gps.WATCH_ENABLE | gps.WATCH_NEWSTYLE)

server_address = ('<broadcast>', 11445)

# Bir UDP Soket oluşturur
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.settimeout(2)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

while True:
    rep = session.next()
    time.sleep(1)
    try:
        if(rep["class"] == "TPV"):
            message=str(rep.lat) + "/" + str(rep.lon) + "/" + str(rep.track)
			# Veriyi gönderir.
            sock.sendto(message.encode('ascii'), server_address)
            
    except Exception as e:
        message="noGPS"
        sock.sendto(message.encode('ascii'), server_address)
