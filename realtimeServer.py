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
writeStr =''
while True:
	with open('/home/pi/Documents/project/hwb/realtime.arff', 'w', 0) as arff:
		arff.write(writeStr)
		try:
			RSSIString=''
			baseArff = "@relation testingDataUnlabeled\n\n@attribute collar_wifi real\n@attribute collar_bluetooth real\n@attribute hub_wifi real\n@attribute hub_bluetooth real\n@attribute restricted {YES,NO}\n\n@data\n";
			for x in range(5):
				collarData = client_sock.recv(4096)
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
				testString = collarData+level+hcidata+',?'
				match = re.search('\A(?:-\d+|\d+),(?:-\d+|\d+),(?:-\d+|\d+),(?:-\d+|\d+),\?\Z',testString)
				testString+='\n'
				if(match):
					RSSIString+=testString
				else:
					x=x-1;
					print "failed on string:"+testString
				#write to csv file
			#print baseArff+RSSIString
			writeStr=baseArff+RSSIString
			#time.sleep(1)
			
		except IOError:
			pass

print "disconnected"

client_sock.close()
server_sock.close()
print "all done"
