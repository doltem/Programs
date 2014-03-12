#include <MsTimer2.h>

double sensor1[30];
double sensor2[30];
double sensor3[30];
int counter = 0;
int aktif=0;
int n=0;

void setup() {
  Serial.begin(9600);
  //interrupts
  MsTimer2::set(1000, active); 
  MsTimer2::start();
  delay(10000);
  sensor1[n]=0;
  sensor2[n]=0;
  sensor3[n]=0;
}

void loop() {
 if(n<30){
  if(aktif==1){
    sensor1[n]=((sensor1[n]*1000/1024)*1.3242-4.9094)/counter;
    sensor2[n]=((sensor2[n]*1000/1024)*1.3552-9.5246)/counter;
    sensor3[n]=((sensor3[n]*1000/1024)*1.3894+3.8677)/counter;
    Serial.print("sensor 1 : "); Serial.print(sensor1[n]);
    Serial.print(" ; sensor 2 : "); Serial.println(sensor2[n]);
    Serial.print(" ; sensor 3 : "); Serial.println(sensor3[n]);
    counter=0;
    aktif=0;
    ++n;
    sensor1[n]=0;
  sensor2[n]=0;
  sensor3[n]=0;
  }
  else{
    tesLight();
  }
}
else{
  Serial.println("DATA COMPLETED");
  Serial.println("Sensor 1");
  for(int i=0;i<n;i++){
    Serial.println(sensor1[i]);
  }
  Serial.println("");
   Serial.println("Sensor 2");
  for(int i=0;i<n;i++){
    Serial.println(sensor2[i]);
  }
  Serial.println("");
   Serial.println("Sensor 3");
  for(int i=0;i<n;i++){
    Serial.println(sensor3[i]);
  }
  delay(120000);
}

}



void tesLight(){
  counter++;
  sensor1[n]+=(double)analogRead(A0);
  sensor2[n]+=(double)analogRead(A1);
  sensor3[n]+=(double)analogRead(A2);
  //Serial.println(analogRead(A0));
}
  

  void active(){
    aktif=1;
  }
