#include <XBee.h>

XBee xbee = XBee();
//payload
uint8_t payload[] = { 0, 0 }
//Response Objects
XBeeResponse response = XBeeResponse(); 
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();
//Request Objects
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x403e0f30);
ZBTxRequest StatPacket = ZBTxRequest(addr64, payload, sizeof(payload));


void setup() {

  Serial.begin(9600);
  xbee.setSerial(Serial);

}

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {
    
    xbee.readPacket(500);
    
    if (xbee.getResponse().isAvailable()) {
      if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        xbee.getResponse().getZBRxResponse(rx);
        xbee.send(StatPacket);  
      } 
	  else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
        xbee.getResponse().getZBTxStatusResponse(txStatus);

			// get the delivery status, the fifth byte
			if (txStatus.getDeliveryStatus() == SUCCESS) {
				// success.  time to celebrate
				flashLed(statusLed, 2, 50);
			} else {
				// the remote XBee did not receive our packet. is it powered on?
				//flashLed(statusLed, 3,500);
			}
		}
		else {
			// not something we were expecting
			flashLed(statusLed, 1, 25);    
		}
      } 
	  else {
        // not something we were expecting
        flashLed(errorLed, 1, 25);    
      }
    } 
	else if (xbee.getResponse().isError()) {
      
    }
}