// IP: http://192.168.4.1/
#include <ESP8266WiFi.h>
#include <Wire.h>
#include <LSM303.h>
#include <L3G.h>
#include "QueueList.h"
//////////////////////
// WiFi Definitions //
//////////////////////
const char WiFiAPPSK[] = "ARHUDWifi";
#define MAX_READINGS 20
#define MAX_CHAR_REPORT 300
#define DEBUG false

//////////////////////
//  IMU Definitions //
//////////////////////
long t0, dT = 50;
LSM303 compass;
L3G gyro;
char report[MAX_CHAR_REPORT];
String reportS;
String value;
//String reportQueue[MAX_READINGS];
QueueList <String> reportQueue;
//int queueSize = 0;
WiFiServer server(80);

void initHardware();

void setupWiFi();

void setup(){
  initHardware();
  Wire.begin();
  if (!gyro.init()){
    if(DEBUG)
      Serial.println("{'error': 'Failed to autodetect gyro type!'}");
    while (1);
  }
  gyro.enableDefault();
  compass.init();
  compass.enableDefault();

  while(reportQueue.count() < MAX_READINGS) {
    // for(int i = 0; i < MAX_CHAR_REPORT; i++){
    //   report[i] = '\0';
    // }
    memset(report, '\0', MAX_CHAR_REPORT);
    compass.read();
    gyro.read();
    snprintf(report, sizeof(report),
    //"{ 'timestamp': %d, 'acc': {'x':%d, 'y':%d, 'z':%d}, 'gir': {'x':%d, 'y':%d, 'z':%d}, 'mag': {'x':%d, 'y':%d, 'z':%d} }",
    "{ 'timestamp': %d, 'acc': {'x':%d, 'y':%d, 'z':%d}, 'gir': {'x':%d, 'y':%d, 'z':%d} }",
    millis(),
    compass.a.x, compass.a.y, compass.a.z,
    gyro.g.x, gyro.g.y, gyro.g.z//,
    //compass.m.x, compass.m.y, compass.m.z
    );
    if(DEBUG)
      Serial.println(report);
    reportS = "";
    for(int i = 0; i < MAX_CHAR_REPORT; i++){
      if(report[i] != '\0')
        reportS += report[i];
    }
    //reportQueue[queueSize++] = reportS;
    reportQueue.push(reportS);
  }

  setupWiFi();
  server.begin();
  t0 = millis();
}

void loop(){
  if(/*reportQueue.count() == MAX_READINGS && */millis() - t0 >= dT ) {
    // for(int i = 0; i < MAX_CHAR_REPORT; i++){
    //   report[i] = '\0';
    // }
    memset(report, '\0', MAX_CHAR_REPORT);
    compass.read();
    gyro.read();
    snprintf(report, sizeof(report),
    //"{ 'timestamp': %d, 'acc': {'x':%d, 'y':%d, 'z':%d}, 'gir': {'x':%d, 'y':%d, 'z':%d}, 'mag': {'x':%d, 'y':%d, 'z':%d} }",
    "{ 'timestamp': %d, 'acc': {'x':%d, 'y':%d, 'z':%d}, 'gir': {'x':%d, 'y':%d, 'z':%d} }",
    millis(),
    compass.a.x, compass.a.y, compass.a.z,
    gyro.g.x, gyro.g.y, gyro.g.z//,
    //compass.m.x, compass.m.y, compass.m.z
    );
    if(DEBUG)
      Serial.println(report);
    reportS = "";
    for(int i = 0; i < MAX_CHAR_REPORT; i++){
      if(report[i] != '\0')
        reportS += report[i];
    }
    // for(int i = 0; i < MAX_READINGS-1; i++){
    //   // reportQueue[i] = "";
    //   reportQueue[i] = reportQueue[i+1];
    // }
    if(reportQueue.count() > MAX_READINGS)
      reportQueue.pop();
    //reportQueue[MAX_READINGS-1] = reportS;
    reportQueue.push(reportS);
    t0 = millis();
  }

  // Check if a client has connected
  WiFiClient client = server.available();
  if (!client) {
    return;
  }

  // Read the first line of the request
  String req = client.readStringUntil('\r');
  //Serial.println(req);
  client.flush();

  // Match the request
  bool validate = false; // We'll use 'val' to keep track of both the
                // request type (read/set) and value if set.
  int initialIndex = req.indexOf("/volldws");
  if (initialIndex != -1) {
    validate = true;
  }
  if(validate){
    client.flush();
    // Prepare the response. Start with the common header:
    String s = "HTTP/1.1 200 OK\r\n";
    //s += "Access-Control-Allow-Origin: *\r\n";
    s += "Content-Type: application/json;charset=utf-8\r\n\r\n";
    s += "[";
    // If we have the distance value, print out a message saying which value is it.
    int count = reportQueue.count();
    for(int i = 0; i < count; i++){
      s += (reportQueue.pop());
      if (i+1 != count)
        s += ",";
      else
        s += "";
    }
    s += "]";
  
    // Send the response to the client
    client.print(s);
    delay(1);
  }
  //Serial.println("Client disonnected");

  // The client will actually be disconnected
  // when the function returns and 'client' object is detroyed
}

void setupWiFi(){
  WiFi.mode(WIFI_AP);

  // Do a little work to get a unique-ish name. Append the
  // last two bytes of the MAC (HEX'd) to "Thing-":
  uint8_t mac[WL_MAC_ADDR_LENGTH];
  WiFi.softAPmacAddress(mac);
  String macID = String(mac[WL_MAC_ADDR_LENGTH - 2], HEX) +
                 String(mac[WL_MAC_ADDR_LENGTH - 1], HEX);
  macID.toUpperCase();
  String AP_NameString = "VolLDWS ESP8266 " + macID;

  char AP_NameChar[AP_NameString.length() + 1];
  memset(AP_NameChar, 0, AP_NameString.length() + 1);

  for (int i=0; i<AP_NameString.length(); i++)
    AP_NameChar[i] = AP_NameString.charAt(i);

  WiFi.softAP(AP_NameChar, WiFiAPPSK);
}

void initHardware(){
  Serial.begin(115200);
  // Don't need to set ANALOG_PIN as input,
  // that's all it can be.
}
