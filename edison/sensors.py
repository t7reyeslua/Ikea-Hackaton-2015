#!/usr/bin/env python

#need to clean these...
import time
import pyupm_grove as grove
import pyupm_mic as upmMicrophone 
import httplib, urllib, urllib2

#Create the temp sensor object using AIO pin 2
temp = grove.GroveTemp(2)
print temp.name()

# Attach microphone to analog port A1
myMic = upmMicrophone.Microphone(1)
threshContext = upmMicrophone.thresholdContext()
threshContext.averageReading = 0
threshContext.runningAverage = 0
threshContext.averagedOver = 2

#Stuff for HTTP posts
ip = "http://94.143.213.153/Areas"

def get_temp():
    celsius = temp.value()
    fahrenheit = celsius * 9.0/5.0 + 32.0;
    print "%d degree cel or %d degrees fahrenheit" \
        % (celsius, fahrenheit)
    return celsius

def get_sound():
    buffer = upmMicrophone.uint16Array(128)
    len = myMic.getSampledWindow(2, 128, buffer);
    if len:
        thresh = myMic.findThreshold(threshContext, 30, buffer, len)
        if(thresh):
            print "Threshold is ", thresh
    return thresh

def send_data(temperature, sound):
    values = {'temperature':temperature,
            'sound':sound,
            'name' : 'Office'}
    data = urllib.urlencode(values)
    req = urllib2.Request(ip, data)
    try:
        response = urllib2.urlopen(req)
    except urllib2.HTTPError:
        pass

def main():
    print "hi!\n"
    while(1):
        temp = get_temp()
        sound = get_sound()
        send_data(temp, sound);
        time.sleep(5);

if __name__ == "__main__":
    main()
