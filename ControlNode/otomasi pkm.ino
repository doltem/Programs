#include <MsTimer2.h>
#include <XBee.h>
#include <math.h>
#include "DHT.h"

//-----------Xbee Var-----------------//
XBee xbee = XBee();
// create reusable response objects for responses we expect to handle 
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

///create payload for status delivery
//Gateway Adress
XBeeAddress64 GatewayAddr = XBeeAddress64(0x0013A200, 0x4092D859);
//packet payload [mode ac,mode lampu,relay ac,relay lampu, pir, temperature,humidity,cahaya msb,cahaya lsb,setpoint ac,erroband ac, setpoint lampu msb,setpoint lampu lsb,errorband lampu]
uint8_t StatPayload[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
//Transmit Packets
ZBTxRequest StatPacket = ZBTxRequest(GatewayAddr, StatPayload, sizeof(StatPayload));

//------------Pin Variable---------//
short pin[5] = {10,11,2,3,A0}; //{relay ac, relay lampu, pir, temperature, light,mode}

//-----------Nilai Variable--------//
short digi[3] = {1,1,0}; //{relay ac,relay lamp, pir}
float analog[3] = {0,0,0}; //temperature, humidity, light
float setpoint[4] = {0,0,0,0}; //{temp,tempdeadband,light,lightdeadband}

short mode[2]={1,1};//mode auto atau manual {ac,lampu}
//---------dht variable------------//
DHT dht(pin[3],DHT22);

//-----------Other Variable--------//
byte aktif=0;

void setup(){
	for(short i=0;i<3;i++){
		if(i<2){
			pinMode(pin[i], OUTPUT);
		}
		else{
			pinMode(pin[i], INPUT);
		}
	}
	dht.begin();
	// start serial for print data
	Serial.begin(115200);
	xbee.setSerial(Serial);
	  
	//Timer Begin
	MsTimer2::set(5000, activate); //Interrupt for Sending Device Stat every 1s
	MsTimer2::start();
}

void loop(){
	checkPacket(); 		//check incoming packet
	readSensor(); 		//reading sensor value and assigning to zones
	autoSwitch();	//relay activation decision based on status and mode
	relaySwitch(); //activating relay
	if (aktif==1) {
		sendStatus(); 	//Sending data to Gateway
	}
}

//function for read sensor value
void readSensor(){
	short i=2;
	while(i<5){
		switch(i){
			case 2:
				digi[2]=digitalRead(pin[i]);
				break;

			case 3:
				analog[0]=dht.readTemperature();
				analog[1]=dht.readHumidity();
				break;

			case 4:
				analog[2]=3.8677*analogRead(pin[i]);
				break;
		}
		i++;
	}
}

//function for automation
void autoSwitch(){
	if(mode[0]==1){
		if(digi[2]==1){
			//ac
			if(analog[0]<(setpoint[0]-setpoint[1])){
				digi[0]=1;
			}
			else if(analog[0]>(setpoint[0]+setpoint[1])){
				digi[0]=0;
			}
		}
		else{
			digi[0]=digi[1]=0;
		}
	}

	if(mode[1]==1){
		if(digi[2]==1){
			//lampu
			if(analog[2]<(setpoint[2]-setpoint[3])){
				digi[1]=1;
			}
			else if(analog[2]>(setpoint[2]+setpoint[3])){
				digi[1]=0;
			}
		}
		else{
			digi[0]=digi[1]=0;
		}
	}
}

//function for activating relay
void relaySwitch(){
	digitalWrite(pin[0], digi[0]);
	digitalWrite(pin[1], digi[1]);
}

//function for checking packet
void checkPacket(){
	if (xbee.readPacket(500)) {
		if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {//get RX response
			xbee.getResponse().getZBRxResponse(rx); //fill response to RX
			//parsing packet
			mode[0]=rx.getData(1); //mode ac
			mode[1]=rx.getData(2); //mode lampu
			digi[0]=rx.getData(3); //relay ac
			digi[1]=rx.getData(4); //relay lampu
			setpoint[0]=joinBit(rx.getData(5),rx.getData(6),16)/10; //setpoint ac msb & lsb
			setpoint[1]=joinBit(rx.getData(7),rx.getData(8),16)/10; //erorband ac msb & lsb
			setpoint[2]=joinBit(rx.getData(9),rx.getData(10),16)/10; //setpoint lampu msb & lsb
			setpoint[3]=joinBit(rx.getData(11),rx.getData(12),16)/10; //erroband lampu msb & lsb
			//send new status
			sendStatus();
		} 
		else if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
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
		}
	}
}

//function for sending status packet
void sendStatus(){
	StatPayload[0]=mode[0];//mode ac
	StatPayload[1]=mode[1];//mode lampu;
	StatPayload[2]=digi[0];//relay ac;
	StatPayload[3]=digi[1];//relay lampu;
	StatPayload[4]=digi[2];//sensor pir;
	StatPayload[5]=getMSB(10*analog[0],16);//temp msb;
	StatPayload[6]=getLSB(10*analog[0],16);//temp lsb;
	StatPayload[7]=getMSB(10*analog[1],16);//hum msb;
	StatPayload[8]=getLSB(10*analog[1],16);//hum lsb;
	StatPayload[9]=getMSB(10*analog[2],16);//cahaya msb;
	StatPayload[10]=getLSB(10*analog[2],16);//cahaya lsb;
	StatPayload[11]=getMSB(10*setpoint[0],16);//sp ac msb;
	StatPayload[12]=getLSB(10*setpoint[0],16);//sp ac lsb;
	StatPayload[13]=getMSB(10*setpoint[1],16);//errorband ac msb;
	StatPayload[14]=getLSB(10*setpoint[1],16);//errorband ac lsb;
	StatPayload[15]=getMSB(10*setpoint[2],16);//sp lampu msb;
	StatPayload[16]=getMSB(10*setpoint[2],16);//sp lampu lsb;
	StatPayload[17]=getMSB(10*setpoint[3],16);//errorband lampu msb;
	StatPayload[18]=getLSB(10*setpoint[3],16);//errorband lampu lsb;

	xbee.send(StatPacket);
	aktif=0;
}


//Bit manipulation Functions
unsigned int joinBit(unsigned int valMSB,unsigned int valLSB, int bitlength){
        bitlength=bitlength/2;
	unsigned int val=(valMSB<<bitlength) | valLSB;	
	return val;
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

//Timer ISR for activating xbee
void activate(){
	//Serial.println("Timer Activated");
	aktif=1;
}