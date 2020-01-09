#include <SoftwareSerial.h>

SoftwareSerial BT(9, 8); 
// creates a "virtual" serial port/UART
// connect BT module TX to D9
// connect BT module RX to D8
// connect BT Vcc to 5V, GND to GND

#define DK 2 // define pin 2 as control pin
void setup()  
{
  Serial.begin(9600);
  // set digital pin to control as an output
  pinMode(DK, OUTPUT);

  // set the data rate for the SoftwareSerial port
  BT.begin(9600);

  // Send test message to other device
  BT.println("Hello from Arduino");
}

char a; // stores incoming character from other device

void loop() 
{
  if (BT.available())
  // if text arrived in from BT serial...
  {
    a=(BT.read());
    Serial.print(a);
    if (a=='1')
    {
      digitalWrite(DK, HIGH);// Turn LIGH ON
      BT.println("Light on");
    }
    if (a=='2')
    {
      digitalWrite(DK, LOW);// Turn LIGHT OFF
      BT.println("Light off");
    }
    if (a=='?')
    {
      BT.println("Send '1' to turn LIGHT ON");
      BT.println("Send '2' to turn LIGHT OFF");
    }   
    
  }
}
