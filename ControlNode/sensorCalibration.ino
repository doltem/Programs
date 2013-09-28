#include <MsTimer2.h>

#include <XBee.h>

int del=1000;
int buttonPin = 10;
int intnolPin = 2, sensorPin =2;
int intsatuPin=3;// 
int ledPin = 6, powerPir = 8;      // select the pin for the LED
int sensorValue = 0;  // variable to store the value coming from the sensor

XBee xbee = XBee();
//packet payload
uint8_t TesPayload[] = { 0xAA, 0xBB, 0xCC, 0xDD};
// SH + SL Address of remote OnOff Device
XBeeAddress64 TesAddr = XBeeAddress64(0x0013A200, 0x4092D859);
//Transmit Packets
ZBTxRequest TesPacket = ZBTxRequest(TesAddr, TesPayload, sizeof(TesPayload));

void setup() {
  // declare the ledPin as an OUTPUT:
  pinMode(ledPin, OUTPUT); 
  pinMode(intnolPin, INPUT);
  pinMode(intnolPin, INPUT);  
  Serial.begin(9600);
  xbee.setSerial(Serial);
  //digitalWrite(powerPir,HIGH);
  //digitalWrite(powerButton,HIGH);
  
  //interrupts
  MsTimer2::set(1000, timerdua); 
  MsTimer2::start();
  attachInterrupt(0, intnol, RISING);
  attachInterrupt(1, intsatu, RISING);
}

void loop() {
  //delay(del);
  //digitalWrite(ledPin,LOW);
 //tesXbee();
 tesLight();
 delay(2000);
}

void tesWarm(){
    int time=100*100;
  // read the value from the sensor:
  digitalWrite(powerPir,HIGH);
  delay(time);
  sensorValue = analogRead(sensorPin);
  digitalWrite(powerPir,LOW);
  Serial.print("nilai sensor :");
  Serial.println(sensorValue);  
  // turn the ledPin on
  delay(2000);
}

void tesLight(){
	double sensorB=analogRead(A0);
	double sensorL=analogRead(A1);
	sensorB=sensorB/1024*3.33; //voltage value
	sensorB=4*sensorB*100-9.77; //illuminance value
	Serial.print("Nilai Sensor Baru : ");
	Serial.println(sensorB);
	sensorL=sensorL/1024*3.33; //voltage value
	sensorL=4*sensorL*100-9.77-0.97; //illuminance value
	Serial.print("Nilai Sensor Lama : ");
	Serial.println(sensorL);
}
	
void tesPir(){
  sensorValue = digitalRead(sensorPin);
  Serial.print("nilai sensor :");
  Serial.println(sensorValue);
	if (digitalRead(buttonPin)==HIGH) Serial.println("Button Pressed");
  delay(1000);
}

void tesXbee(){
	xbee.send(TesPacket);
}

void intnol(){
	del=1000;
}

void intsatu(){
	del=3000;
}

void timerdua(){
	digitalWrite(ledPin,HIGH);
}
	