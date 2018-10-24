package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * The Navigator class extends the functionality of the Navigation class. It offers an alternative
 * travelTo() method which uses a state machine to implement obstacle avoidance.
 * 
 * The Navigator class does not override any of the methods in Navigation. All methods with the same
 * name are overloaded i.e. the Navigator version takes different parameters than the Navigation
 * version.
 * 
 * This is useful if, for instance, you want to force travel without obstacle detection over small
 * distances. One place where you might want to do this is in the ObstacleAvoidance class. Another
 * place is methods that implement specific features for future milestones such as retrieving an
 * object.
 * 
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
public class Navigation {
  private static final int FORWARD_SPEED = 250;
  private static final int ROTATE_SPEED = 80;
  private static final double SENSOR_DIS = 15.5;
  private static final int ACCELERATION = 300;
  private static final int DISTANCE_THRESHOLD = 25;

  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  private Odometer odometer;
  private SensorData data;

  /**
   * This navigation class constructor sets up our robot to begin navigating a particular map
   * 
   * @param leftMotor The EV3LargeRegulatedMotor instance for our left motor
   * @param rightMotor The EV3LargeRegulatedMotor instance for our right motor
   */
  public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor)
      throws OdometerExceptions {
    this.odometer = Odometer.getOdometer();
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.data = SensorData.getSensorData();
    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {this.leftMotor,
        this.rightMotor}) {
      motor.stop();
      motor.setAcceleration(ACCELERATION);
    }
  }

  /**
   * TravelTo function which takes as arguments the x and y position in cm Will travel to designated
   * position, while constantly updating it's heading
   * 
   * When avoid=true, the nav thread will handle traveling. If you want to travel without avoidance,
   * this is also possible. In this case, the method in the Navigation class is used.
   * 
   * @param x The x coordinate to travel to (in cm)
   * @param y The y coordinate to travel to (in cm)
   */
  public void travelTo(double x, double y, boolean doCorrection) {
    double dX = x - odometer.getXYT()[0];
    double dY = y - odometer.getXYT()[1];
    double theta = Math.atan(dX / dY);
    if (dY < 0 && theta < Math.PI)
      theta += Math.PI;

    // Euclidean distance calculation.
    double distance = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

    turnTo(Math.toDegrees(theta), false, true);

    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);

    leftMotor.rotate(convertDistance(Lab5.WHEEL_RAD, distance * Lab5.TILE), true);
    rightMotor.rotate(convertDistance(Lab5.WHEEL_RAD, distance * Lab5.TILE), doCorrection);
    boolean corrected = false;
    if (!doCorrection)
      return;
    while (leftMotor.isMoving() && rightMotor.isMoving()) {
      if (data.getDL()[1] < DISTANCE_THRESHOLD) {
        Sound.beep();
        doCorrection(data.getA());
        corrected = true;
      }
    }
  }

  /**
   * This method is where the logic for the odometer will run. Use the methods provided from the
   * OdometerData class to implement the odometer.
   * 
   * @param angle The angle we want our robot to turn to (in degrees)
   */
  public void turnTo(double angle, boolean async, boolean useGyro) {
    double dTheta;

    if (useGyro) {
      dTheta = angle - data.getA();
    } else {
      dTheta = angle - odometer.getXYT()[2];
    }
    if (dTheta < 0)
      dTheta += 360;

    // TURN RIGHT
    if (dTheta > 180) {
      leftMotor.setSpeed(ROTATE_SPEED);
      rightMotor.setSpeed(ROTATE_SPEED);
      leftMotor.rotate(-convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, 360 - dTheta), true);
      rightMotor.rotate(convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, 360 - dTheta), async);
    }
    // TURN LEFT
    else {
      leftMotor.setSpeed(ROTATE_SPEED);
      rightMotor.setSpeed(ROTATE_SPEED);
      leftMotor.rotate(convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, dTheta), true);
      rightMotor.rotate(-convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, dTheta), async);
    }
  }

  /**
   * This method corrects our robot's odometer readings
   * 
   * @param angle The current angle that the robot is facing
   */
  public void doCorrection(double angle) {
    double[] position = odometer.getXYT();
    // Check that our robot's angle is within certain bounds and correct odometer if required.
    if (angle < 5 || angle > 355) {
      int sensorCoor = (int) Math.round(position[1] - SENSOR_DIS / Lab5.TILE);
      odometer.setY(sensorCoor + SENSOR_DIS / Lab5.TILE);
    } else if (angle < 185 && angle > 175) {
      int sensorCoor = (int) Math.round(position[1] + SENSOR_DIS / Lab5.TILE);
      odometer.setY(sensorCoor - SENSOR_DIS / Lab5.TILE);
    } else if (angle < 95 && angle > 85) {
      int sensorCoor = (int) Math.round(position[0] - SENSOR_DIS / Lab5.TILE);
      odometer.setX(sensorCoor + SENSOR_DIS / Lab5.TILE);
    }
  }

  /**
   * Rotate the robot by certain angle
   * 
   * @param angle The angle to rotate our robot to
   */
  public void rotate(int angle) {
    leftMotor.rotate(convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, angle), true);
    rightMotor.rotate(-convertAngle(Lab5.WHEEL_RAD, Lab5.TRACK, angle), false);
  }

  /**
   * Stop the motor
   */
  public void stop() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }

  /**
   * This method allows the conversion of a distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius The radius of our wheels
   * @param distance The distance traveled
   * @return A converted distance
   */
  public static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  /**
   * This method allows the conversion of an angle value
   * 
   * @param radius The radius of our wheels
   * @param distance The distance traveled
   * @param angle The angle to convert
   * @return A converted angle
   */
  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }
}
