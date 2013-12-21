
int treshold=0;
int light=0;
int fail=0;
int counter=0;

void setup() {
  pinMode(10,OUTPUT);
pinMode(11,OUTPUT);
pinMode(12,OUTPUT);
pinMode(13,OUTPUT);
  Serial.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  digitalWrite(10,HIGH);
  delay(15000);
  if(analogRead(A0)>=treshold){
    counter++;
  }
  else{
    fail++;
  }
  digitalWrite(10,LOW);
  delay(15000);
}



