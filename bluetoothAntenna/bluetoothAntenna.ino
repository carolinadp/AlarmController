const int ledPin = 2;
const int otherLedPin = 4;
const char START_ALARM = '2';
const char IM_HERE = '1';

char data = 0; //Variable for storing received data

int ledState = HIGH;

boolean ledOn = false;

unsigned long onDelay = 500;
unsigned long lastOnTime = 0;

void checkBluetoothInput() {
  if(Serial.available() > 0)  // Send data only when you receive data:
  {
    data = Serial.read();      //Read the incoming data and store it into variable data
    Serial.print(IM_HERE);        //Print Value inside data in Serial monitor
    Serial.print("\n");        //New line 
    digitalWrite(ledPin, HIGH);
    ledOn = true;
    lastOnTime = millis();
  } 
}

void startAlarm() {
  Serial.print(START_ALARM);
  Serial.print("\n");
}

void setup() 
{
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
  pinMode(ledPin, OUTPUT);
  digitalWrite(otherLedPin, HIGH);
}

void loop()
{

  if (ledOn && millis() - lastOnTime > onDelay) {
    digitalWrite(ledPin, LOW);
    ledOn = false;
  }

  checkBluetoothInput();
  
}


