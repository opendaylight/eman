#!/usr/bin/python
# Title:         EOserver.py
# Description:   Simple httpServer to run on mininet host and return sample ENERGY_OBJECT JSON 
#                default prints a response as string, paths access sample files in mininet custom folder
# Auther:        Frank Sanodval
# Created:       12/9/2016
# version:       0.1
# usage:         from mininet prompt: h1 python custom/EOserver.py &
#                from mininet prompt: h2 curl 10.0.0.1
#                from mininet prompt: h1 kill %python
# license        none
# python version        2.7.6   
#
import time
import BaseHTTPServer
import json
import random
import time


HOST_NAME = '127.0.0.1'
PORT_NUMBER = 9000 


class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(s):
        """Respond to a GET request."""
        s.send_response(200)
        s.send_header("Content-type", "application/json")
        s.end_headers()

        timeStamp = 36598000
        eoPower = str(20 + random.randint(0, 10))

        EoPwrObj = {
            'eoObject' : {
                'timeStamp': "timeStamp",
                'eoPower': eoPower,
                'eoPowerNameplate': "22",
                'eoPowerUnitMultiplier': "22",
                'eoPowerAccuracy': "22",
                'eoPowerMeasurementCaliber': "22",
                'eoPowerCurrentType': "22",
                'eoPowerMeasurementLocal': "22",
                'eoPowerAdminState': "22",
                'eoPowerOperState': "22",
                'eoPowerStateEnterReason': "22"
            }
        }


        if s.path=="/EO":
            f = open(INFILE)
            s.wfile.write(f.read())
        else:
#            print json.dumps(EoPwrObj)
            s.wfile.write(json.dumps(EoPwrObj))
            d = EoPwrObj['eoObject']
            print d['eoPower']

if __name__ == '__main__':
    server_class = BaseHTTPServer.HTTPServer
    httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
    print "EOserver on"
    print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER)
    