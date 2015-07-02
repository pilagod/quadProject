#include<SoftwareSerial.h>
#include <Servo.h>
#include "Wire.h"

Servo yaw_servo;
Servo roll_servo;
Servo pitch_servo;

const double alpha = 0.5; // r=low pass pole, alpha=1/(1+r)
double yaw, pitch, roll;



// 定義連接藍牙模組的序列埠
//SoftwareSerial BT(11, 12); // 接收腳, 傳送腳 
char val;


char num[5];
int numFlag = 0;
int result[3];
int resultFlag = 0;


void setup() {
  
  yaw_servo.attach(2);
  roll_servo.attach(7);
  pitch_servo.attach(9);
  
  result[0] = 0;
  result[1] = 0;
  result[2] = 90;
  
  Serial.begin(115200);   // 與電腦序列埠連線
  Serial.println("Serial is ready!");
 
}
 
void loop() {
  if (Serial.available()) {
    val = Serial.read();
    if(val == ',' || val == ';'){
        num[numFlag] = '\0';
        result[resultFlag] = atoi(num);
        numFlag = 0;
        resultFlag ++;
        
        if(resultFlag == 3){
          for(int i=0; i<3; i++){
            if(i<2){
            }
          }
          updateServo(); //!!!
          resultFlag = 0;
          numFlag = 0;
        }
        return;
    }
    
    num[numFlag] = val;
    numFlag ++; 
  }
  
}

void updateServo()
{       
    yaw = result[0];
    roll = result[1] + 90;
    pitch = result[2] + 180;
   
    if(yaw>179) yaw=179;
    else if(yaw<0) yaw=0;
    if(roll>179) roll=179;
    else if(roll<0) roll=0;
    if(pitch>179) pitch=179;
    else if(pitch<0) pitch=0;
   
    //change direction
    roll = -1*roll+179;
    pitch = -1*pitch+179;
   
    fYaw = yaw * alpha + (fYaw * (1.0 - alpha));
    fPitch = pitch * alpha + (fPitch * (1.0 - alpha));
    fRoll = roll * alpha + (fRoll * (1.0 - alpha));
   
   
    Serial.print(fYaw);
    Serial.print("      ");
    Serial.print(fPitch);
    Serial.print("      ");
    Serial.println(fRoll);
   
    // Pitch Servo update
    yaw_servo.write(fYaw);//result[0]);
    roll_servo.write(fPitch);
    pitch_servo.write(fRoll);
        
}
  
