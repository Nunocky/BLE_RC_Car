#include <RBL_nRF8001.h>
#include <RBL_services.h>

#define PIN_MOTOR1_CTRL1 4
#define PIN_MOTOR1_CTRL2 3

#define PIN_MOTOR2_CTRL1 7
#define PIN_MOTOR2_CTRL2 6
#define PIN_MOTOR2_PWM   5

void motor1_init() {
  pinMode(PIN_MOTOR1_CTRL1, OUTPUT);
  pinMode(PIN_MOTOR1_CTRL2, OUTPUT);
}

void motor1_off() {
  digitalWrite(PIN_MOTOR1_CTRL1, LOW);
  digitalWrite(PIN_MOTOR1_CTRL2, LOW);
}

void motor1_set(bool direction) {
  if (direction) {
    digitalWrite(PIN_MOTOR1_CTRL1, HIGH);
    digitalWrite(PIN_MOTOR1_CTRL2, LOW);
  }
  else {
    digitalWrite(PIN_MOTOR1_CTRL1, LOW);
    digitalWrite(PIN_MOTOR1_CTRL2, HIGH);
  }
}

void motor2_init() {
  pinMode(PIN_MOTOR2_CTRL1, OUTPUT);
  pinMode(PIN_MOTOR2_CTRL2, OUTPUT);
  pinMode(PIN_MOTOR2_PWM,   OUTPUT);
}

void motor2_off() {
  digitalWrite(PIN_MOTOR2_CTRL1, HIGH);
  digitalWrite(PIN_MOTOR2_CTRL2, HIGH);
  digitalWrite(PIN_MOTOR2_PWM,   HIGH);
}

void motor2_set(bool direction, int speed) {
  if (direction) {
    digitalWrite(PIN_MOTOR2_CTRL1, HIGH);
    digitalWrite(PIN_MOTOR2_CTRL2, LOW);
  }
  else {
    digitalWrite(PIN_MOTOR2_CTRL1, LOW);
    digitalWrite(PIN_MOTOR2_CTRL2, HIGH);
  }

  analogWrite(PIN_MOTOR2_PWM, speed);
}


char inputc[2];

void setup() {
  ble_set_name("BLE Car");
  ble_begin();

  // Enable serial debug
  Serial.begin(115200);

  motor1_init();
  motor2_init();

  inputc[0] = 0;
  inputc[1] = 0;
}

void loop() {

  ble_do_events();

  if (!ble_connected() ) {
    motor1_off();
    motor2_off();
    return;
  }

  if ( ble_available() ) {
    int n = 0;
    while (ble_available()) {
      int v = ble_read();
      if (v == -1)
        continue;
      if (n == 0) {
        inputc[0] = v;
        n++;
      }
      else {
        inputc[1] = v; // - 128;
        n = 0;
        break;
      }
    }

    Serial.print(inputc[1], DEC);
    Serial.print(" ");
    Serial.print(inputc[1], HEX);
    Serial.println();
  }


  // 1st byte ... motor1
  if (inputc[0] == 0) {
    motor1_off();
  }
  else {
    motor1_set(inputc[0] > 0);
  }

  // 2nd byte ... motor2
  if (inputc[1] == 0) {
    // stop
    motor2_off();
  }
  else {
    bool direction = inputc[1] < 0;
    int      speed = (inputc[1] > 0) ? inputc[1] : -inputc[1];
    speed *= 2; // 0~255
    motor2_set(direction, speed);
  }

  delay(30);
}
