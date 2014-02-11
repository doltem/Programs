#include <XBee.h>

#define crcMSB 0x00
#define crcLSB 0x00

//////////CRC VARIABLE//////
uint16_t crcTable[256];
uint16_t poly=0x8005; //polynom based on CRC-16 Standard

////////////////////XBEE VARIABLE/////////////////////
XBee xbee = XBee();
//Response Objects
XBeeResponse response = XBeeResponse(); 
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

//Request Objects
XBeeAddress64 GatewayAddr = XBeeAddress64(0x0013A200, 0x4092D859);
//packet payload
uint8_t initPayload[] = {0x01 ,0x80,0x10,0x00,0x58,0x00,0x0a,0x00,0x00,0x18,0x00,0x00,0x01,0x18,0x01,0x00,0x02,0x21,0x1d,0xbb,0x00,0x03,0x21,0x00,0x00,0x00,0x04,0x21,0x1b,0x4e,0x00,0x05,0x18,0x01};
uint8_t StatPayload[] = {0x01 ,0x80,0x10,0x00,0x58,0x00,0x0a,0x00,0x00,0x18,0x00,0x00,0x01,0x18,0x01,0x00,0x02,0x21,0x1d,0xbb,0x00,0x03,0x21,0x00,0x00,0x00,0x04,0x21,0x1b,0x4e,0x00,0x05,0x18,0x01, crcMSB, crcLSB};
//Transmit Packets
ZBTxRequest StatPacket = ZBTxRequest(GatewayAddr, StatPayload, sizeof(StatPayload));


void setup() {
	crcTableInit();

	uint16_t crc = getCRC(initPayload,34);
	//fill to new payload
    StatPayload[34]=getMSB(crc,16);
    StatPayload[35]=getLSB(crc,16);

	Serial.begin(115200);
	xbee.setSerial(Serial);
}

// continuously reads packets, looking for ZB Receive or Modem Status
void loop() {
    xbee.readPacket();
    
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
			} else {
				// the remote XBee did not receive our packet. is it powered on?
				//flashLed(statusLed, 3,500);
			}
		}
		else {
			// not something we were expecting
			//flashLed(statusLed, 1, 25);    
		}
      } 
	  else {
        // not something we were expecting
        //flashLed(errorLed, 1, 25);    
      }
}

void crcTableInit(){
	uint16_t remain; //remainder variable
	for(int div = 0; div <256 ; ++div){ //divident index
		remain = div << 8;
		
		for(uint16_t bit = 0; bit<8; ++bit){
			uint16_t topbit = remain & (1<<15);
			remain <<= 1;
			if (topbit)
            {
                remain ^= poly;
            }
		}
		
		crcTable[div] = remain;
	}
}

uint16_t getCRC(uint8_t *payload, int length){
	uint8_t data;
	uint16_t remain = 0; //remainder initial value
	
	for(int byte=0; byte<length ;++byte){
		data = *payload++ ^ (remain >> 8);
		remain = crcTable[data] ^ (remain<<8);
		
	}
	
	return remain;
}

unsigned int getMSB(unsigned int value, int bitlength){
	bitlength=bitlength/2;
	unsigned int mask=getMask(bitlength);;
        unsigned int msb= (value>>bitlength);
        msb = msb & mask;
	return msb;
}
	
unsigned int getLSB(unsigned int value, int bitlength){
	bitlength=bitlength/2;
	unsigned int mask=getMask(bitlength);
        unsigned int lsb= value & mask;
	return lsb;
}
	
unsigned int getMask(int length){
	int num=0;
        int powval=0;
	for(int i=0;i<length;i++){
            powval=pangkat(2,i);
            num=num+powval;
	}
	return num;
}

int pangkat(int val, int expo){
  int hasil=val;
  if(expo<1){ hasil=1;}
  else if(expo<2){hasil=val;}
  else{
    for(int i=1;i<expo;i++){
      hasil=hasil*val;
    }
  }
  return hasil;
}