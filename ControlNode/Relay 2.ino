
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
#define LLightBandId 0x04
#define LMode 0x05

//Data Type Identifier
#define DT8Bitmap 0x18
#define DT8Enum 0x30
#define DT16Uint 0x21

//initial value for packet
#define OccValue 0 //PIR Sensor
#define LightValueMSB 0 //MSB of light level value
#define LightValueLSB 0 //LSB of light level value
#define LightSetMSB 0 //MSB of light level set point value
#define LightSetLSB 0 //LSB of light level set point value
#define LightBandMSB 0 //MSB of light level error band value
#define LightBandLSB 0 //LSB of light level error band value
#define Lamp 0 //operatioan mode status -> AUTO
#define mode 0x01 //operatioan mode status -> AUTO
#define endpoint 1 //endpoint = zone

//Occupancy Value
#define OCCUPIED 0x01
#define UNOCCUPIED 0x00

//Light measurement data type
#define LUXSET 0x00
#define LUXLEVEL 0x01
#define LUXBAND 0x02

//LED mapping
#define setLed 7
#define statusLed 4
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

//----------------------------------------------------------------------------//

///sensor value table
int pirval[5] = { 0, 0, 0, -1,-1};
double lightval[5] = { 0, 0, -1, -1,-1}; //final value of lighting levele
double lightcal[5] = {1.3552, 1.3242,-1,-1};
double lightoffset[5] = {-9.5246, -4.9094, -1 , -1};
double lightves[5]= { 0, 0, 0, 0, 0}; //vessel for determine lighting level value mean after a cycle
int lightcount=0; //counter for each lighting level sensor reading


int zoneocclamp [4][4] ={  //lamp and occupancy status of each zones
	{0, 0, -1, -1}, //lamp
	{0, -1, -1, -1}, //occupancy
	{AUTO, 0, -1, -1}, //mode
	{0, 0, 0, 0}//button state
};

double zonelight [3][4] ={ //light level and setpoint status of each zones
	{0, -1, -1, -1}, //lux
	{0, -1, -1, -1}, //setpoint
	{5, -1, -1, -1}, //error band
};

//dump for sensor value array
int dump=-1;
double ddump=-1;

//int mock=1; //data for testing zone occupancy
//double dmock=100; //data for testing zone lighting level

//----------------------------PIR DELAY TIMER---------------------------------//

unsigned int pirdelay = 600; //in secs
unsigned long pirtime[4];

//----------------------------INSERT -1 or &dump FOR UNUSED VARIABLES BELOW----------------//

//pin mapping, ALWAYS INSERT -1 if element not used
byte modeButton=4;
int button[5]={4,-1,-1,-1,-1};
int zonerelay[5]= {10,11,-1,-1,-1}; //pin mapping for relay
int pinpir[5] = {2,3,5,-1,-1}; //pin mapping for pir
int pinlight[5] = {A0,A1,-1,-1,-1}; //pin mapping for light level sensor


//pir mapping to zones , ALWAYS INSERT &dump if element not used. LOWEST ROW CAN'T BE USED
int *zonepir [5][4] = { 
	{  &pirval[0], &pirval[2] , &dump, &dump},
	{  &pirval[1], &dump, &dump, &dump},
	{ &dump, &dump, &dump, &dump},
	{ &dump, &dump, &dump, &dump},
	{ &dump, &dump, &dump, &dump} //row 5  is table limit, always set -1
};

//ligh sensor mapping to zones , ALWAYS INSERT &ddump if element not used. LOWEST ROW CAN'T BE USED
double *zonelux [5][4] = { 
	{  &lightval[0],  &lightval[1], &ddump, &ddump},
	{ &ddump, &ddump, &ddump, &ddump},
	{ &ddump, &ddump, &ddump, &ddump},
	{ &ddump, &ddump, &ddump, &ddump},
	{ &ddump, &ddump, &ddump, &ddump} //row 5  is table limit, always set to -1(dump variable)
};



//----------------------------------------------------------------------------//
//operational VARIABLE
byte aktif=0;

//Xbee Object
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
uint8_t StatPayload[] = {endpoint, EndGateway, MCustomCluster, LCustomCluster, FCServer2Client, Trans, CReportAttribute, 0x00,LOnOffId, DT8Bitmap, Lamp, 0x00, LOccId, DT8Bitmap, OccValue, 0x00, LLightId, DT16Uint, LightValueMSB, LightValueLSB, 0x00, LLightSetId, DT16Uint, LightSetMSB, LightSetLSB, 0x00, LLightBandId, DT16Uint, LightBandMSB, LightBandLSB,0x00,LMode, DT8Bitmap, mode};
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
                //Serial.print("nilai c : ");
                //Serial.println(c);
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
  Serial.begin(115200);
  xbee.setSerial(Serial);
  
  //Reserved for Startup Action
  flashLed(8, 3, 500);
  
  MsTimer2::set(10000, activate); //Interrupt for Sending Device Stat every 1s
  MsTimer2::start();
  
  digitalWrite(5,HIGH);
}

void loop() {
	checkPacket(); 		//check incoming packet
	readSensor(); 		//reading sensor value and assigning to zones
	updateStatus(); 	//update zone status
	autoSwitch();	//relay activation decision based on status and mode
	manualSwitch(); //manual switching relay based on button
	relaySwitch(); //activating relay
	if (aktif==1) {
		int i=0;
		while(pinlight[i]!=-1){
			lightval[i]=(lightves[i]/lightcount*1000/1024*lightcal[i])+lightoffset[i]; //get mean value of lighting level reading
			
			lightves[i]=0;
			i++;
		}
		lightcount = 0;
		sendStatus(); 	//Sending data to Gateway
	}
	//delay(500);Serial.println();
}

//functions for sending and receiving packet
void checkPacket(){
	if (xbee.readPacket(500)) {
		if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {//get RX response
			flashLed(statusLed, 3, 100);
			xbee.getResponse().getZBRxResponse(rx); //fill response to RX
			int clusterID=joinBit(rx.getData(2),rx.getData(3),16);
			
			if(clusterID==CustomCluster){//act based on determined Cluster ID
				byte zone=rx.getData(1)-1;
				zoneocclamp[2][zone] = rx.getData(10); //mode
				zoneocclamp[0][zone]= rx.getData(14); //lamp value
				zonelight[1][zone]=getLight(rx.getData(18),rx.getData(19)); //setpoint
				zonelight[2][zone]=getLight(rx.getData(23),rx.getData(24)); //error band
				sendStatus();
			}
		} 
		else if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
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
}

void sendStatus(){
        //Serial.println("ind SendStatus loop ");
	byte j=0;
	while(zonerelay[j]!=(-1)){
		StatPayload[0]=j+1; //assign endpoint value to packet
        StatPayload[33]=zoneocclamp[2][j]; //assign current mode status to packet
		StatPayload[14]=zoneocclamp[1][j]; //assign occupancy value to packet
		StatPayload[10]=zoneocclamp[0][j]; //assign lamp status to packet
		convertLight(zonelight[1][j], LUXSET); //assign light level set point value to packet
		convertLight(zonelight[2][j], LUXBAND); //assign light level error band to packet
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
	 //temporary value for storing lighting level value
	while(pinpir[i]!=-1){
		if(digitalRead(pinpir[i])==HIGH){
			pirval[i]=1;
			pirtime[i]=millis();
			//Serial.println("aktif");
		}
		else{
			if(pirtime[i]>=millis()){
				pirtime[i]=millis();
				//Serial.println("reset");
			}
			if( (millis()-pirtime[i]) <= (pirdelay*1000) ){
				pirval[i]=1;
				//Serial.println("jadi satu");
			}
			else{
				pirval[i]=0; 
				//Serial.println("tetap nol");
			}
		}
		i++;
	}
	/*Serial.print("nilai PIR asli : ");
	Serial.println(digitalRead(pinpir[0]));
	Serial.print("nilai PIR olahan : ");
	Serial.println(pirval[0]);*/
	i=0;
	while(pinlight[i]!=-1){
		lightves[i]=lightves[i]+(double) analogRead(pinlight[i]); //1.96908 is calibration constant
                //Serial.print("Nilai sensor cahaya : ");
                //Serial.println(lighttemp);
		i++;
	}
	lightcount++;
}

void updateStatus(){
	byte i=0; //zone counter

	while(zonerelay[i]!=-1){
		int j=0; //occupancy status row counter
		int k=0; //light level row counter
		byte occ=0;
		double lux=0;
		while(*zonepir[j][i]!=-1){
                        //Serial.print("Nilai sensor Pir : ");
                        //Serial.println(*zonepir[j][i]);
			occ=occ || *zonepir[j][i];
			j++;
		}
		                //Serial.print("Nilai sensor Pir : ");
                        //Serial.println(occ);

		while(*zonelux[k][i]!=-1){
                        //Serial.print("Nilai sensor cahaya : ");
                        //Serial.println(*zonelux[k][i]);
			lux=lux+*zonelux[k][i];
			k++;
		}
		                //Serial.print("Nilai sensor cahaya : ");
                        //Serial.println(lux);

		zoneocclamp[1][i]=occ;
		zonelight[0][i]=lux/k;
		i++;
	}
}

//functions for auto swtiching
void autoSwitch(){
	byte j=0;
	while(zonerelay[j]!=-1){
		//Serial.println("barrier AUTO 1");	
		if(zoneocclamp[2][j]==AUTO){ //if in auto mode,checked occupancy and lighting level first
			//Serial.println("barrier AUTO 2");	
			if(zoneocclamp[1][j]==OCCUPIED){
				//Serial.println("barrier ON 1");	
				if(zonelight[0][j]<(zonelight[1][j]-zonelight[2][j])){
					//Serial.println("barrier ON 2");
					zoneocclamp[0][j]=ON;
				}
				else if(zonelight[0][j]>(zonelight[1][j]+zonelight[2][j])){
					//Serial.println("barrier OFF 1");
					zoneocclamp[0][j]=OFF;
				}
			}
			else if(zoneocclamp[1][j]==UNOCCUPIED){
				//Serial.println("barrier OFF 2");
				zoneocclamp[0][j]=OFF;
			}
		}
		j++;
	}
}

//function for manual button
void manualSwitch(){
	byte j=0;
	if(digitalRead(modeButton)==LOW){
        while(zonerelay[j]!=-1){
            if(digitalRead(button[j])==HIGH && zoneocclamp[3][j]==0){
                    zoneocclamp[3][j]=1;
            }
            else if(digitalRead(button[j])==LOW && zoneocclamp[3][j]==1){
                if(zoneocclamp[0][j]==1){
                    zoneocclamp[0][j]=0;
                    zoneocclamp[2][j]=MANUAL;
                }
                else{
                    zoneocclamp[0][j]=1;
	                zoneocclamp[2][j]=MANUAL;
                }
                zoneocclamp[3][j]=0;
            }
        j++;
        }
    }
    else{
        while(zonerelay[j]!=-1){
            if(digitalRead(button[j])==HIGH && zoneocclamp[3][j]==0){
                zoneocclamp[3][j]=1;
            }
            else if(digitalRead(button[j])==LOW && zoneocclamp[3][j]==1){
            	zoneocclamp[2][j]=AUTO;
            	zoneocclamp[3][j]=0;
            }
        j++;
    	}
	}
}

//function for activating relay
void relaySwitch(){
	byte j=0;
	while(zonerelay[j]!=-1){
		digitalWrite(zonerelay[j], zoneocclamp[0][j]);
		j++;
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
	light=joinBit(mlux, llux,16);
	light=pow(10,((light-1)/10000));
	return light;
}

//function for converting lux set point or lux value
void convertLight(double lux, byte type){
	lux=10000*log10(lux)+1; //mapped to zigbee standard
	if(lux<1){ lux=0;}
	lux=(int) lux;
	switch(type){
		case LUXBAND:
			StatPayload[28]=getMSB(lux,16); //fill in lighterrobandMSB value to packet
			StatPayload[29]=getLSB(lux,16); //fill in lighterrobandLSB value to packet
		break;

		case LUXSET:
			StatPayload[23]=getMSB(lux,16); //fill in lightsetMSB value to packet
			StatPayload[24]=getLSB(lux,16); //fill in lightsetLSB value to packet
		break;
		
		case LUXLEVEL:
			StatPayload[18]=getMSB(lux,16); //fill in lightvalueMSB value to packet
			StatPayload[19]=getLSB(lux,16); //fill in lightvalueMSB value to packet			
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
	//Serial.println("Timer Activated");
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