# file: rfcomm-client.py
#
#
#
#
#

from bluetooth import *
import sys
import subprocess
import time
import re

addr = "B8:27:EB:EB:CA:4D"

if len(sys.argv) < 2:
    print "no device specified.  Searching all nearby bluetooth devices for"
    print "the SampleServer service"
else:
    addr = sys.argv[1]
    print "Searching for SampleServer on %s" % addr

# search for the SampleServer service
uuid = "d758c62c-83c4-11e8-adc0-fa7ae01bbebc"
service_matches = find_service( uuid = uuid, address = addr )

if len(service_matches) == 0:
    print "couldn't find the SampleServer service =("
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print "connecting to \"%s\" on %s" % (name, host)

# Create the client socket
sock = BluetoothSocket( RFCOMM )
sock.connect((host, port))

print "connected.  type stuff"
while True:
    #grab bt
    hcidata = subprocess.check_output(['hcitool','rssi','B8:27:EB:EB:CA:4D'])
    numFound = re.search('((-)?\d+)',hcidata)
    hcidata = numFound.group(1)

    #grab wifi
    wifile = open("/proc/net/wireless","r")
    wifidata = wifile.read();
    levelMatch = re.search('wlan0:\s+\d+\s+\d+\.\s+(\s|\S+)\.',wifidata)
    level = levelMatch.group(1)+","
    level.strip()

    level += hcidata
    #if len(data) == 0: break
    sock.send(level)
    time.sleep(.2)

sock.close()
