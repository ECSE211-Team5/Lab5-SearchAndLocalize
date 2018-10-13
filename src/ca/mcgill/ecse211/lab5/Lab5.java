// Lab2.java
package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.sensors.LightPoller;
import ca.mcgill.ecse211.sensors.UltrasonicPoller;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * This class implements the main starting point for the Localization lab
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class Lab5 {
  // Motor Objects, and Robot related parameters
  private static final Port usPort = LocalEV3.get().getPort("S1");
  //initialize multiple light ports in main
  private static Port[] lgPorts;
  private static final TextLCD lcd = LocalEV3.get().getTextLCD();

  /**
   * Motor object instance that allows control of the left motor connected to port B
   */
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

  /**
   * Motor object instance that allows control of the right motor connected to port A
   */
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

  /**
   * This variable denotes the radius of our wheels in cm.
   */
  public static final double WHEEL_RAD = 2.2;

  /**
   * This variable denotes the track distance between the center of the wheels in cm (measured and
   * adjusted based on trial and error).
   */
  public static final double TRACK = 10.7;
  //last TRACK value: 10.7
  
  /**
   * This variables holds the starting corner coordinates for our robot.
   */
  public static final int[] SC = {1,1};
  
  /**
   * This variable holds the color of the target ring in the range [1,4].
   * 1 indicates a ORANGE ring
   * 2 indicates a GREEN ring
   * 3 indicates a BLUE ring
   * 4 indicates a YELLOW ring
   */
  public static final int TR = 1;
  
  /**
   * These are the coordinates for our search area.
   * LL = Lower Left
   * UR = Upper Right
   */
  public static final int LLx = 3, LLy = 3, URx = 7, URy = 7;
    
  /**
   * This array contains the set of all coordinates that our robot has visited.
   * By default all values are set to false.
   */
  public static boolean[][] visitedSearchAreaCoordinates = new boolean[URx-LLx+1][URy-LLy+1]; 

  /**
   * This method is our main entry point - instantiate objects used and set up sensor.
   * 
   * @param args an array of arguments that can be passed in via commandline or otherwise.
   */
  public static void main(String[] args) throws OdometerExceptions {
    // Odometer related objects
    Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
    Display odometryDisplay = new Display(lcd);

    // Sensor Related Stuff
    SensorData sensorData = SensorData.getSensorData();

    // Ultrasonic sensor stuff
    @SuppressWarnings("resource")
    SensorModes usSensor = new EV3UltrasonicSensor(usPort);
    SampleProvider usDistance = usSensor.getMode("Distance");
    float[] usData = new float[usDistance.sampleSize()];

    // Light sesnor sensor stuff
    //@SuppressWarnings("resource")
    lgPorts[0] = LocalEV3.get().getPort("S2");
    lgPorts[1] = LocalEV3.get().getPort("S3");
    lgPorts[2] = LocalEV3.get().getPort("S4");
    //SensorModes lgSensor = new EV3ColorSensor(lgPort);
    //SampleProvider lgLight = lgSensor.getMode("Red");
    //float[] lgData = new float[lgLight.sampleSize()];

    
    //STEP 1: LOCALIZE to (1,1)
    // ButtonChoice left or right
    lcd.clear();
    lcd.drawString("<  Left  |  Right >", 0, 0);
    lcd.drawString(" falling | rising  ", 0, 1);
    lcd.drawString("  edge   |  edge   ", 0, 2);
    final int buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)

    // Start odometer and odometer display
    Thread odoThread = new Thread(odometer);
    odoThread.start();
    Thread odoDisplayThread = new Thread(odometryDisplay);
    odoDisplayThread.start();

    // Start ultrasonic and light sensors
    Thread usPoller = new UltrasonicPoller(usDistance, usData, sensorData);
    usPoller.start();
    //Thread lgPoller = new LightPoller(lgLight, lgData, sensorData);
    //lgPoller.start();

    // Start localizing
    final Navigation navigation = new Navigation(leftMotor, rightMotor);
    final UltrasonicLocalizer usLoc = new UltrasonicLocalizer(navigation, leftMotor, rightMotor);
    final LightLocalizer lgLoc = new LightLocalizer(navigation, leftMotor, rightMotor);

    // spawn a new Thread to avoid localization from blocking
    (new Thread() {
      public void run() {
        usLoc.localize(buttonChoice);
        lgLoc.localize();
        // nav.travelToCoordinate(0, 0); nav.turnTo(0);
      }
    }).start();
    
    //STEP 2: MOVE TO START OF SEARCH AREA
    
    //STEP 3: SEARCH ALL COORDINATES

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
