
var upmMicrophone = require("jsupm_mic");

// Attach microphone to analog port A0
var myMic = new upmMicrophone.Microphone(1);

var threshContext = new upmMicrophone.thresholdContext;
threshContext.averageReading = 0;
threshContext.runningAverage = 0;
threshContext.averagedOver = 2;

var is_running = false;
var LCD = require('jsupm_i2clcd');

// Initialize Jhd1313m1 at 0x62 (RGB_ADDRESS) and 0x3E (LCD_ADDRESS)
var myLcd = new LCD.Jhd1313m1 (0, 0x3E, 0x62);
myLcd.setColor(255, 0, 0);
var highscore = 0;

while(1)
{
    var buffer = new upmMicrophone.uint16Array(128);
    var len = myMic.getSampledWindow(2, 128, buffer);
    if (len)
    {
        var thresh = myMic.findThreshold(threshContext, 30, buffer, len);
        if (thresh){
            myLcd.setCursor(0,0);
            myLcd.write('Mic level: '+ thresh);
            if(thresh > highscore){
                myLcd.setCursor(1,0);
                myLcd.write('Highscore: '+ thresh);
                highscore = thresh;
            }
        }
    }
}

// Print message when exiting
process.on('SIGINT', function()
{
    console.log("Exiting...");
    process.exit(0);
});
