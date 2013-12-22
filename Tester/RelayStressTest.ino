
int treshold=0;
int timer=20000;
int light=0;
int counter=0;

XBee xbee = XBee();
// create reusable response objects for responses we expect to handle 
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

///create payload for status delivery
//Gateway Adress
XBeeAddress64 GatewayAddr = XBeeAddress64(0x0013A200, 0x4092D859);
//packet payload
uint8_t payload[] = {0,1,2,3}; //{counter,analogread,fail indicator}
//Transmit Packets
ZBTxRequest StatPacket = ZBTxRequest(GatewayAddr, payload, sizeof(payload));

void setup() {
  pinMode(10,OUTPUT);
  pinMode(11,OUTPUT);
  pinMode(12,OUTPUT);
  pinMode(13,OUTPUT);
  Serial.begin(9600);
  xbee.setSerial(Serial);
}

// the loop routine runs over and over again forever:
void loop() {
  //activate relay
  digitalWrite(10,HIGH);
  delay(timer);
  //evaluating relay
  if(analogRead(A0)>=treshold){
    counter++;
    payload[0]=counter;
    payload[1]=getMSB(analogRead(A0),16);
    payload[2]=getLSB(analogRead(A0),16);
    payload[3]=0;

  }
  else{
    payload[0]=counter;
    payload[1]=getMSB(analogRead(A0),16);
    payload[2]=getLSB(analogRead(A0),16);
    payload[3]=1;
  }
  //Sending packet & checkstatus
  xbee.send(StatPacket);
  delay(500);
  checkPacket();
  //deactivate relay
  digitalWrite(10,LOW);
  delay(timer);
}

void checkPacket(){
  if (xbee.readPacket(500)) {
    if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {//get RX response

    } 
    else if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);

      // get the delivery status, the fifth byte
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        //reset counter
        counter=0;
      } else {
      }
    }
    else {
    }
  }
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




