// Lab2.java
package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.sensors.*;
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
  private static Port[] lgPorts = new Port[2];
  private static final TextLCD lcd = LocalEV3.get().getTextLCD();

  /**
   * Motor object instance that allows control of the left motor connected to port B
   */
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

  /**
   * Motor object instance that allows control of the right motor connected to port A
   */
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

  /**
   * length of the tile 
   */
  public static final double TILE = 30.48;
  
  /**
   * This variable denotes the radius of our wheels in cm.
   */
  public static final double WHEEL_RAD = 2.2;

  /**
   * This variable denotes the track distance between the center of the wheels in cm (measured and
   * adjusted based on trial and error).
   */
  public static final double TRACK = 10.7;
  //last TRACK value: 10.4
  
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
    //lgPorts[2] = LocalEV3.get().getPort("S4");
    EV3ColorSensor[] lgSensors = new EV3ColorSensor[2];
    for(int i = 0; i < lgSensors.length; i++) {
    	lgSensors[i] = new EV3ColorSensor(lgPorts[i]);
    }
    
    SampleProvider backLight = lgSensors[0].getRedMode();
    SampleProvider frontLight1 = lgSensors[1].getRGBMode();
    //SampleProvider frontLight2 = lgSensors[2].getRGBMode();
    
    //target color
    ColorCalibrator.Color targetColor = ColorCalibrator.Color.values()[TR-1];

    
    //STEP 1: LOCALIZE to (1,1)
    // ButtonChoice left or right
    lcd.clear();
    lcd.drawString("<  Left  |  Right >", 0, 0);
    lcd.drawString(" falling | rising  ", 0, 1);
    lcd.drawString("  edge   |  edge   ", 0, 2);
    lcd.drawString("        \\/        ", 0, 3);
    lcd.drawString("  Color Detection  ", 0, 4);
    
    
    final int buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
    
    
    // Start odometer and odometer display
    Thread odoThread = new Thread(odometer);
    odoThread.start();
    Thread odoDisplayThread = new Thread(odometryDisplay);
    odoDisplayThread.start();

    // Start ultrasonic and light sensors
    Thread usPoller = new UltrasonicPoller(usDistance, usData, sensorData);
    usPoller.start();
    Thread bLgPoller = new LightPoller(backLight, new float[backLight.sampleSize()], sensorData);
    bLgPoller.start();
    Thread fLgPoller1 = new RGBPoller(frontLight1, new float[frontLight1.sampleSize()], sensorData);
    fLgPoller1.start();
    //Thread fLgPoller2 = new RGBPoller(frontLight2, new float[frontLight2.sampleSize()], sensorData);
    //fLgPoller2.start();
    
    //Run color classification
    if(buttonChoice == Button.ID_DOWN) {
    	while (Button.waitForAnyPress() != Button.ID_ESCAPE);
        System.exit(0);
    };

    // Start localizing
    final Navigation navigation = new Navigation(leftMotor, rightMotor);
    final UltrasonicLocalizer usLoc = new UltrasonicLocalizer(navigation, leftMotor, rightMotor);
    final LightLocalizer lgLoc = new LightLocalizer(navigation, leftMotor, rightMotor);

    // spawn a new Thread to avoid localization from blocking
    (new Thread() {
      public void run() {
        usLoc.localize(buttonChoice);
        lgLoc.localize(SC);
        //nav.travelToCoordinate(0, 0); nav.turnTo(0);
        
        //STEP 2: MOVE TO START OF SEARCH AREA
        navigation.travelTo(LLx, LLy, false);
        //STEP 3: SEARCH ALL COORDINATES
        for (int i = LLx; i < URx+1; i++) {
        	if((i - LLx)%2 == 0) {
        		for (int j = LLy; j < URy+1; j++) {
            		//LIGHT SENSOR RING DETECTION CODE NEEDED IN NAVIGATION TO SLOW DOWN.
            		navigation.travelTo(i, j, true);
            		visitedSearchAreaCoordinates[URx-i][URy-j] = true;
            	}
        	}else {
        		for (int j = URy; j >= LLy; j--) {
            		//LIGHT SENSOR RING DETECTION CODE NEEDED IN NAVIGATION TO SLOW DOWN.
            		navigation.travelTo(i, j, true);
            		visitedSearchAreaCoordinates[URx-i][URy-j] = true;
            	}
        	}
        	
        }
        
        //STEP 4: NAVIGATE TO URx, URy
      }
    }).start();

    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
}
