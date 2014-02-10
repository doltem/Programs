/*
  AnalogReadSerial
  Reads an analog input on pin 0, prints the result to the serial monitor.
  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.
 
 This example code is in the public domain.
 */

// the setup routine runs once when you press reset:
void setup() {
  pinMode(2,INPUT);
  pinMode(3,INPUT);
  pinMode(5,INPUT);
  pinMode(6,INPUT);
  
  pinMode(4,INPUT);
  pinMode(7,INPUT);
  pinMode(8,INPUT);
  pinMode(9,INPUT);
  // initialize serial communication at 9600 bits per second:
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  //analog
  /*Serial.print("nilai A0 : ");
  Serial.println(analogRead(A0));
  Serial.print("nilai A1 : ");
  Serial.println(analogRead(A1));
  Serial.print("nilai A2 : ");
  Serial.println(analogRead(A2));
  Serial.print("nilai A3 : ");
  Serial.println(analogRead(A3));
  //DIGITAL
  Serial.print("nilai D1 : ");
  Serial.println(digitalRead(2));
  Serial.print("nilai D2 : ");
  Serial.println(digitalRead(3));
  Serial.print("nilai D3 : ");
  Serial.println(digitalRead(5));
  Serial.print("nilai D4 : ");
  Serial.println(digitalRead(6));
  *///bUTTON
  Serial.print("Button 1 : ");
  Serial.println(digitalRead(4));
  Serial.print("Button 2 : ");
  Serial.println(digitalRead(7));
  Serial.print("Button 3 : ");
  Serial.println(digitalRead(8));
  Serial.print("Button 4 : ");
  Serial.println(digitalRead(9));
  
  
  delay(2000);
}