
int pin[4]={2,3,5,6};

void setup() {
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  for(int i=0;i<4;i++){
    digitalWrite(pin[i],HIGH);
  }
 
}



