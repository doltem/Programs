
int pin[4]={4,7,8,9};
int button[4]={1,2,3,4};

void setup() {
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  // read the input pin:
  for(int i=0;i<4;i++){
    if(digitalRead(pin[i])==HIGH){
      Serial.print("Pin ");
      Serial.print(button[i]);
      Serial.println(" Mendeteksi input");
      delay(1000);
    }
  }
 
}



