#include <XBee.h>
#include <MsTimer2.h>


int treshold=50;
int light=0;
int counter=0;
int lampstat=0;
int active=0;

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
  Serial.begin(9600);
  xbee.setSerial(Serial);
  MsTimer2::set(10000, activate); //Interrupt for Sending Device Stat every 1s
  MsTimer2::start();
}

// the loop routine runs over and over again forever:
void loop() {
  if(active==1){
    xbee.send(StatPacket);
    active=0;
    counter=0;
  }
}

void activate(){
  if(lampstat==0){
    digitalWrite(10,HIGH);
    lampstat=1;
  }
  else if(lampstat==1){
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

    digitalWrite(10,LOW);
    active=1;
    lampstat=0;
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




