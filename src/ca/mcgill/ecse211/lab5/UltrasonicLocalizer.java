package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class helps our robot to localize itself using the ultrasonic sensor
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class UltrasonicLocalizer {
  private static final int ROTATE_SPEED = 80;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;

  private Odometer odometer;
  private SensorData data;
  private Navigation navigation;

  private static final int READINGS_THRESHOLD = 10;
  private static final double wallDistance = 40.0;
  private static final double wallDistanceError = 5;

  /**
   * This is the class constructor
   * 
   * @param leftMotor
   * @param rightMotor
   * @throws OdometerExceptions
   */
  public UltrasonicLocalizer(Navigation nav, EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor) throws OdometerExceptions {
    this.odometer = Odometer.getOdometer();
    this.data = SensorData.getSensorData();
    this.navigation = nav;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
  }

  /**
   * Wrapper for risingEdge or falling edge methods. Left or Right button option.
   * 
   * @param buttonChoice The left or right button on the EV3 brick
   */
  public void localize(int buttonChoice) {
    if (buttonChoice == Button.ID_RIGHT) {
      risingEdge();
    } else {
      fallingEdge();
    }
  }

  /**
   * Make the robot find North by using the rising edge, start facing left of x-axis wall
   */
  private void risingEdge() {
    double angle1, angle2, theta = 0;
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);

    // 1. turn until no wall, then see wall and check angle1
    leftMotor.forward();
    rightMotor.backward();

    turnToWall();
    turnAwayFromWall();
    Sound.beep();
    angle1 = odometer.getXYT()[2];

    // 2. turn opposite way, until no wall, then see wall and check angle2
    rightMotor.forward();
    leftMotor.backward();

    turnToWall();
    turnAwayFromWall();
    Sound.beep();
    angle2 = odometer.getXYT()[2];

    // 3. Turn to calculate angle, then turn to face y-axis, wait for button press
    if (angle1 < angle2)
      theta = -222 + (angle1 + angle2) / 2;
    else
      theta = -46 + (angle1 + angle2) / 2;
    navigation.turnTo(theta + 180);
    odometer.setTheta(0);
    Button.waitForAnyPress();
  }

  /**
   * Make the robot find North by using the falling edge, start facing top of y-axis wall
   */
  private void fallingEdge() {
    double angle1, angle2, theta = 0;
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);

    // 1. Turn right until we see the first wall, then turn left
    leftMotor.forward();
    rightMotor.backward();

    turnAwayFromWall();
    turnToWall();
    Sound.beep();
    angle1 = odometer.getXYT()[2];

    // 2. Turn left until we see the second wall, then turn right
    rightMotor.forward();
    leftMotor.backward();

    turnAwayFromWall();
    turnToWall();
    Sound.beep();
    angle2 = odometer.getXYT()[2];

    // 3. Calculate the angle and face y-axis 0
    if (angle1 < angle2)
      theta = -222 + (angle1 + angle2) / 2;
    else
      theta = -46 + (angle1 + angle2) / 2;
    navigation.turnTo(theta);
    odometer.setTheta(0);
    // wait for any button pressure
    Button.waitForAnyPress();
  }


  /**
   * Keep turning until robot is sure to have seen the wall. Takes 10 readings to be sure.
   */
  private void turnToWall() {
    int numberOfReadings = 0;

    while (numberOfReadings < READINGS_THRESHOLD) {
      if (data.getDL()[0] < wallDistance - wallDistanceError)
        numberOfReadings++;
    }
  }

  /**
   * Keep turning until robot is sure to have not seen the wall.
   */
  private void turnAwayFromWall() {
    int numberOfReadings = 0;

    while (numberOfReadings < READINGS_THRESHOLD) {
      if (data.getDL()[0] > wallDistance + wallDistanceError)
        numberOfReadings++;
    }
  }
}
