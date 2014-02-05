#include <MsTimer2.h>

double sensor = 0;
int counter = 0;
int aktif=0;
void setup() {
  Serial.begin(9600);
  //interrupts
  MsTimer2::set(3000, active); 
  MsTimer2::start();
}

void loop() {
 if(aktif==1){
    sensor=1.96908*(sensor*1000/1024)/counter;
    Serial.println(sensor);
    counter=0;
    sensor=0;
    aktif=0;
 }
 tesLight();
}


void tesLight(){
  counter++;
  sensor+=(double)analogRead(A0);
  //Serial.println(analogRead(A0));
}
  

  void active(){
    aktif=1;
  }
