# file: rfcomm-server.py
#
#
#
#

from bluetooth import *
import subprocess
import time
import re
import csv
import sys

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "d758c62c-83c4-11e8-adc0-fa7ae01bbebc"

advertise_service( server_sock, "SampleServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
                   
print "Waiting for connection on RFCOMM channel %d" % port

client_sock, client_info = server_sock.accept()
print "Accepted connection from ", client_info
start = time.time()
#for unioque file names
moment=time.strftime("%Y-%b-%d__%H_%M_%S",time.localtime())

#    writer.writerow({'first_name': 'Baked', 'last_name': 'Beans'})
#    writer.writerow({'first_name': 'Lovely', 'last_name': 'Spam'})
#    writer.writerow({'first_name': 'Wonderful', 'last_name': 'Spam'})

yesOrNo = sys.argv[1];
roomNum = sys.argv[2];
with open('/home/pi/Documents/project/hwb/rooms/training'+yesOrNo+roomNum+'.arff', 'w') as file:
	baseArff = "@relation testingDataUnlabeled\n\n@attribute collar_wifi real\n@attribute collar_bluetooth real\n@attribute hub_wifi real\n@attribute hub_bluetooth real\n@attribute restricted {YES,NO}\n\n@data\n";
	if (yesOrNo == 'NO'):
		file.write(baseArff)
	try:
		while True:
			collarData = client_sock.recv(1024)
			collarData += ","
			timestamp = str(time.time()-start)

			hcidata = subprocess.check_output(['hcitool','rssi','B8:27:EB:45:A2:58'])
			numFound = re.search('((-)?\d+)',hcidata)
			hcidata = numFound.group(1)


			wifile = open("/proc/net/wireless","r")
			wifidata = wifile.read();

			levelMatch = re.search('wlan0:\s+\d+\s+\d+\.\s+(\s|\S+)\.',wifidata)
			level = levelMatch.group(1)
			level.strip()
			level += ","
			RSSIString = collarData+level+hcidata
			match = re.search('\A(?:-\d+|\d+),(?:-\d+|\d+),(?:-\d+|\d+),(?:-\d+|\d+)\Z',RSSIString)
			if(match):
				file.write(RSSIString+','+yesOrNo+'\n')
			else:
				x=x-1;
				print "failed on string:"+testString
            #write to csv file
	except IOError:
		pass

print "disconnected"

client_sock.close()
server_sock.close()
print "all done"
