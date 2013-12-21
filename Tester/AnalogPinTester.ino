
in pin[4]={A0,A1,A2,A3}
void setup() {
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  for(int i=0;i<4;i++){
    Serial.print("nilai analog pin ");
    Serial.print(pin[i]);
    Serial.print(" : ");
    Serial.println(analogRead(pin[i]));
  }
  delay(2000);
}



