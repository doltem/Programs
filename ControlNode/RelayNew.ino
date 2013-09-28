
/* this is coding for testing sending with simulated light sensor data. need to be added :
	1.Pin for led status
	2.remote adress for sending stat
	3.Send LED status
	4.Parse Input from gateway

	sending frame checked : OK
*/
#include <MsTimer2.h>
#include <XBee.h>
#include <math.h>

//Endpoint Identifier
#define EndOnoffL 0x12
#define EndGateway 0x80

//Cluster Identifier (M ->MSB , L ->LSB)
#define MCustomCluster 0x10
#define LCustomCluster 0x00

//Cluster ID joined
#define CustomCluster 0x1000

//FrameControl
#define FCServer2Client 0x58
#define FCClient2Server 0x48

//Command Identifier
#define CReportAttribute 0x0a

//Attribute id for Special Custom CLuster 
#define LOnOffId 0x00
#define LOccId 0x01
#define LLightId 0x02
#define LLightSetId 0x03
#define LMode 0x04

//Data Type Identifier
#define DT8Bitmap 0x18
#define DT8Enum 0x30
#define DT16Uint 0x21

//Occupancy Value
#define OCCUPIED 0x01
#define UNOCCUPIED 0x00

//Light measurement data type
#define LUXSET 0x00
#define LUXLEVEL 0x01

//LED mapping
#define setLed 6
#define statusLed 7
#define modeLed 7
#define setButton 3
#define manSwitch 2

//Operational Mode
#define AUTO 0x01
#define MANUAL 0x00
//Lamp Status
#define ON 0x01
#define OFF 0x00

int Trans = 0;

///sensor value table
int pirval[5] = { 0, -1, -1, -1};
double lightval[5] = { 0, -1, -1, -1};

///zone mapping table , always insert -1 if element not used. in some table rightmost column or lowest row, used -1 as table limit
int zonepir [5][4] = { //pir mapping to zones
	{  pirval[0],  -1, -1, -1},
	{  -1,  -1, -1, -1},
	{ -1, -1, -1, -1},
	{ -1, -1, -1, -1},
	{ -1, -1, -1, -1} //row 5  is table limit, always set -1
};

double zonelux [5][4] = { //ligh sensor mapping to zones
	{  lightval[0],  -1, -1, -1},
	{ -1, -1, -1, -1},
	{ -1, -1, -1, -1},
	{ -1, -1, -1, -1},
	{ -1, -1, -1, -1} //row 5  is table limit, always set -1
};

int zoneocclamp [3][4] ={  //lamp and occupancy status of each zones
	{0, -1, -1, -1}, //lamp
	{0, -1, -1, -1}, //occupancy
	{AUTO, -1, -1, -1} //mode
};

double zonelight [2][4] ={ //light level and setpoint status of each zones
	{0, -1, -1, -1}, //lux
	{100, -1, -1, -1} //setpoint
};

///pin mapping, always insert -1 if element not used
int zonerelay[5]= {8,-1,-1,-1,-1}; //pin mapping for relay
int pinpir[5] = {3,-1,-1,-1,-1}; //pin mapping for pir
int pinlight[5] = {A6,-1,-1,-1,-1}; //pin mapping for light level sensor


//Sensor Value container for packet
byte OccValue =0; //PIR Sensor
double LightValue=0; //Light Sensor
unsigned int LightValueMSB = 0;
unsigned int LightValueLSB = 0;
//setpoint & error band Value container for packet
double LightSet=0;
unsigned int LightSetMSB = 0;
unsigned int LightSetLSB = 0;
double LightBand=10;
//lamp container for packet
byte Lamp;
byte mode;

//operational VARIABLE
byte aktif=0;

//endpoint variable
byte endpoint=0;

//Xbee Object
XBee xbee = XBee();
// create reusable response objects for responses we expect to handle 
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

///create payload for status delivery
//Gateway Adress
XBeeAddress64 GatewayAddr = XBeeAddress64(0x0013A200, 0x408B5E9F);
//packet payload
uint8_t StatPayload[] = {endpoint, EndGateway, MCustomCluster, LCustomCluster, FCServer2Client, Trans, CReportAttribute, 0x00,LOnOffId, DT8Bitmap, Lamp, 0x00, LOccId, DT8Bitmap, OccValue, 0x00, LLightId, DT16Uint, LightValueMSB, LightValueLSB, 0x00, LLightSetId, DT16Uint, LightSetMSB, LightSetLSB, 0x00,LMode, DT8Bitmap, mode};
//Transmit Packets
ZBTxRequest StatPacket = ZBTxRequest(GatewayAddr, StatPayload, sizeof(StatPayload));

void setup() {
  //digital pin Mode Setting
  pinMode(setLed, OUTPUT);
  pinMode(statusLed, OUTPUT);
  pinMode(setButton, INPUT);
  pinMode(manSwitch, INPUT);
  	
	byte c=0; //mode setting for relay pin
	while(zonerelay[c]!=-1){
                Serial.print("nilai c : ");
                Serial.println(c);
		pinMode(zonerelay[c],OUTPUT);
		c++;
	}
   
	c=0; //mode setting for pir pin
	while(pinpir[c]!=-1){
		pinMode(pinpir[c],INPUT);
		c++;
	}
  
  //interrupt
  //attachInterrupt(0, manualSwitch, CHANGE); //change mode to manual or turn on auto
  //attachInterrupt(1, setPoint, RISING); 	//set light level treshold
  
  // start serial for print data
  Serial.begin(9600);
  xbee.setSerial(Serial);
  
  //Reserved for Startup Action
  flashLed(8, 3, 500);
  
//Interrupt for Sending Device Stat every 1s
  MsTimer2::set(1000, activate); 
  MsTimer2::start();
  
  digitalWrite(5,HIGH);
}

void loop() {
	checkPacket(); 		//check incoming packet
	readSensor(); 		//reading sensor value and assigning to zones
	updateStatus(); 	//update zone status
	autoSwitch();	//actuating lamp based on status and mode
	if (aktif==1) sendStatus(); 	//Sending data to Gateway
}

//functions for sending and receiving packet
void checkPacket(){
	if (xbee.readPacket(500)) {
		//led action
		flashLed(statusLed, 2, 300);

		if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {//get RX response
			xbee.getResponse().getZBRxResponse(rx); //fill response to RX
			int clusterID=joinBit(rx.getData(2),rx.getData(3),16);
			
			if(clusterID==CustomCluster){//act based on determined Cluster ID
				byte zone=rx.getData(1)-1;
				zoneocclamp[2][zone] = rx.getData(10);
				zoneocclamp[1][zone]= rx.getData(14); //lamp value
				zonelight[1][zone]=getLight(rx.getData(18),rx.getData(19));
			}
		} 
		else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) { // get modem status response
			xbee.getResponse().getModemStatusResponse(msr);      
			if (msr.getStatus() == ASSOCIATED) { //Associated in Network
			  flashLed(statusLed, 3, 500);
			} else if (msr.getStatus() == DISASSOCIATED) { //Dissociated in Network
			  flashLed(statusLed, 7, 100);
			} else {
			  // another status
			  flashLed(statusLed, 10, 10);
			}
		} 
		else if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
			xbee.getResponse().getZBTxStatusResponse(txStatus);

			// get the delivery status, the fifth byte
			if (txStatus.getDeliveryStatus() == SUCCESS) {
				// success.  time to celebrate
				flashLed(statusLed, 2, 100);
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
}

void sendStatus(){
        //Serial.println("ind SendStatus loop ");
	byte j=0;
	while(zonerelay[j]!=(-1)){
		endpoint=j+1; //assign endpoint value to packet
                mode=zoneocclamp[2][j]; //assign occupancy value to packet
		OccValue=zoneocclamp[1][j]; //assign occupancy value to packet
		Lamp=zoneocclamp[0][j]; //assign occupancy value to packet
		convertLight(zonelight[1][j], LUXSET); //assign light level set point value to packet
		convertLight(zonelight[0][j], LUXLEVEL); //assign light level value to packet
		xbee.send(StatPacket);
		aktif=0;
                //Serial.print("nilai j : ");
                //Serial.println(j);
		j++;
	}
}

//functions for read sensor and update status
void readSensor(){
	byte i=0; //counter
	while(pinpir[i]!=-1){
		pirval[i]=digitalRead(pinpir[i]);
                //Serial.print("Nilai sensor Pir : ");
                //Serial.println(pirval[i]);
		i++;
	}
	i=0;
	while(pinlight[i]!=-1){
		lightval[i]=analogRead(pinlight[i]);
		lightval[i]=lightval[i]/1024*5; //voltage value
		lightval[i]=4*lightval[i]*100-9.77-0.97; //illuminance value
                //Serial.print("Nilai sensor cahaya : ");
                //Serial.println(lightval[i]);
		i++;
	}
}

void updateStatus(){
	byte i=0; //zone counter

	while(zonerelay[i]!=-1){
		int j=0; //occupancy status row counter
		int k=0; //light level row counter
		byte occ=0;
		double lux=0;
		while(zonepir[j][i]!=-1){
                        Serial.print("Nilai sensor Pir : ");
                        Serial.println(zonepir[j][i]);
			occ=occ || zonepir[j][i];
			j++;
		}
		while(zonelux[k][i]!=-1){
                        //Serial.print("Nilai sensor cahaya : ");
                        //Serial.println(zonelux[k][i]);
			lux=lux+zonelux[k][i]/2;
			k++;
		}
		
		zoneocclamp[1][i]=occ;
		zonelight[0][i]=lux;
		i++;
	}
}

//functions for lamp switching
void autoSwitch(){
	byte j=0;
	while(zonerelay[j]!=-1){	
		if(zoneocclamp[2][j]==AUTO){ //if in auto mode,checked occupancy and lighting level first
			if(zoneocclamp[1][j]==OCCUPIED){
				if(zonelight[0][j]<(zonelight[1][j]-LightBand)){
					zoneocclamp[0][j]==ON;
				}
				else if(zonelight[0][j]>(zonelight[1][j]+LightBand)){
					zoneocclamp[0][j]==OFF;
				}
			}
			else if(zoneocclamp[1][j]==UNOCCUPIED){
				zoneocclamp[0][j]==OFF;
			}
		}
		//if device in manual , lamp activating based on assigned lamp value from HMI
		digitalWrite(zonerelay[j], zoneocclamp[0][j]);
		j++;
	}
}

void manualSwitch(){
	byte i=0;
	if(mode==AUTO){ 
		mode=MANUAL;
		while(zonerelay[i]!=-1){
			digitalWrite(zonerelay[i],LOW);
			i++;
		}
	}
	else {
		mode=AUTO;
	}
}

//function for set light level treshold
void setPoint(){
	byte j=0;
	while(zonerelay[j]!=-1){
		zonelight[1][j]=zonelight[0][j];
		j++;
	}
	flashLed(modeLed,3, 500);
}

double getLight(int mlux, int llux){
	double light;
	mlux = (double) mlux;
	llux = (double) llux;
	light=joinBit(mlux, llux,16);
	light=pow(10,((light-1)/10000));
	return light;
}

//function for converting lux set point or lux value
void convertLight(double lux, byte type){
	lux=10000*log10(lux)+1; //mapped to zigbee standard
	lux=(int) lux;
	switch(type){
		case LUXSET:
			LightSetMSB=getMSB(lux,16);
			LightSetLSB=getLSB(lux,16);
		break;
		
		case LUXLEVEL:
			LightValueMSB=getMSB(lux,16);
			LightValueLSB=getLSB(lux,16);			
		break;
	}
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
	aktif=1;
}

//flashing led
void flashLed(int pin, int times, int wait) {
    
    for (int i = 0; i < times; i++) {
      digitalWrite(pin, HIGH);
      delay(wait);
      digitalWrite(pin, LOW);
      
      if (i + 1 < times) {
        delay(wait);
      }
    }
}