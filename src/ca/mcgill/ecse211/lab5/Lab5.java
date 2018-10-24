// Lab2.java
package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.GyroPoller;
import ca.mcgill.ecse211.sensors.LightPoller;
import ca.mcgill.ecse211.sensors.RGBPoller;
import ca.mcgill.ecse211.sensors.SensorData;
import ca.mcgill.ecse211.sensors.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * This class implements the main starting point for the Search and Localize lab
 * 
 * @author Caspar Cedro & Percy Chen & Patrick Erath & Anssam Ghezala & Susan Matuszewski & Kamy
 *         Moussavi Kafi
 */

// Test to make sure latest version pushed
public class Lab5 {
	// Motor Objects, and Robot related parameters
	private static final Port usPort = LocalEV3.get().getPort("S1");
	// initialize multiple light ports in main
	private static Port[] lgPorts = new Port[2];
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	private static final Port gPort = LocalEV3.get().getPort("S4");
	private static EV3GyroSensor gSensor = new EV3GyroSensor(gPort);
	/**
	 * Motor object instance that allows control of the left motor connected to port
	 * B
	 */
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	/**
	 * Motor object instance that allows control of the right motor connected to
	 * port A
	 */
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	/**
	 * length of the tile
	 */
	public static final double TILE = 30.48;

	/**
	 * This variable denotes the radius of our wheels in cm.
	 */
	public static final double WHEEL_RAD = 2.2;
	// 2.15

	/**
	 * This variable denotes the track distance between the center of the wheels in
	 * cm (measured and adjusted based on trial and error).
	 */
	public static double TRACK =  10.3;//10.2;//9.5;// for localization: 10.8;
	// last TRACK value: 10.4

	public static final double LOCALIZE_WHEEL_RAD = 2.2;
	public static final double LOCALIZE_TRACK = 10.8; // 10.8 before
	/**
	 * This variables holds the starting corner coordinates for our robot.
	 */
	public static final int[] SC = { 1, 1 };

	/**
	 * This variable holds the color of the target ring in the range [1,4]. 1
	 * indicates a ORANGE ring 2 indicates a GREEN ring 3 indicates a BLUE ring 4
	 * indicates a YELLOW ring
	 */
	public static final int TR = 4;

	/**
	 * These are the coordinates for our search area. LL = Lower Left UR = Upper
	 * Right
	 */
	public static final int LLx = 2, LLy = 2, URx = 5, URy = 5;

	/**
	 * This method is our main entry point - instantiate objects used and set up
	 * sensor.
	 * 
	 * @param args an array of arguments that can be passed in via commandline or
	 *             otherwise.
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
		// @SuppressWarnings("resource")
		lgPorts[0] = LocalEV3.get().getPort("S2");
		lgPorts[1] = LocalEV3.get().getPort("S3");
		// lgPorts[2] = LocalEV3.get().getPort("S4");
		EV3ColorSensor[] lgSensors = new EV3ColorSensor[2];
		for (int i = 0; i < lgSensors.length; i++) {
			lgSensors[i] = new EV3ColorSensor(lgPorts[i]);
		}

		SampleProvider backLight = lgSensors[0].getRedMode();
		SampleProvider frontLight1 = lgSensors[1].getRGBMode();
		
		// SampleProvider frontLight2 = lgSensors[2].getRedMode();

		SampleProvider gProvider = gSensor.getAngleMode();
		
		// STEP 1: LOCALIZE to (1,1)
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
		Thread gPoller = new GyroPoller(gProvider, new float[gProvider.sampleSize()], sensorData);
		gPoller.start();
		
		// Run color classification
		if (buttonChoice == Button.ID_DOWN) {
			while (Button.waitForAnyPress() != Button.ID_ESCAPE)
				;
			System.exit(0);
		}
		;

		// Start localizing
		final Navigation navigation = new Navigation(leftMotor, rightMotor);
		final UltrasonicLocalizer usLoc = new UltrasonicLocalizer(navigation, leftMotor, rightMotor);
		final LightLocalizer lgLoc = new LightLocalizer(navigation, leftMotor, rightMotor);
		final RingSearcher searcher = new RingSearcher(navigation, leftMotor, rightMotor);
		
		// spawn a new Thread to avoid localization from blocking
		(new Thread() {
			public void run() {
 				// target color
				ColorCalibrator.Color targetColor = ColorCalibrator.Color.values()[TR - 1];

			usLoc.localize(buttonChoice);
			lgLoc.localize(SC);
			gSensor.reset();
				// TODO: delete test code
				try {
					Odometer odometer = Odometer.getOdometer();
					// STEP 2: MOVE TO START OF SEARCH AREA
					 navigation.travelTo(LLx, LLy, false);
					 Lab5.TRACK = LOCALIZE_TRACK;
					// STEP 3: SEARCH ALL COORDINATES
					searchArea(navigation, searcher, targetColor);
					navigation.travelTo(odometer.getXYT()[0], URy+0.5, true);
					navigation.travelTo(URx, URy+0.5, true);
					navigation.travelTo(URx, URy, false);
					// STEP 4: NAVIGATE TO URx, URy
				} catch (OdometerExceptions e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}).start();
		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}

	/**
	 * This method finished the search process, it returns when it find the targeted
	 * run
	 * 
	 * @param navigation
	 * @param searcher
	 * @param targetColor: target color
	 */
	public static void searchArea(Navigation navigation, RingSearcher searcher, ColorCalibrator.Color targetColor) {
		int counter = 0;
		for (double i = LLx + 0.5; i < URx + 1; ) {
			if (counter % 2 == 0) {
				// if we are at the first, third...etc verticle zone, search up
				navigation.travelTo(i, LLy+0.3, false);
				navigation.turnTo(180, false, true);
				if (searcher.search(110, targetColor)) return;
				navigation.turnTo(0, false, true);
				for (double j = LLy + 0.7; j < URy; j++) {
					navigation.travelTo(i, j, true);
					if (searcher.search(70, targetColor)) return;
					navigation.turnTo(0, false, true);
					if (searcher.search(280, targetColor)) return;
					navigation.turnTo(0, false, true);
				}
			} else {
				navigation.travelTo(i, URy-0.3, true);
				navigation.turnTo(0, false, true);
				if (searcher.search(70, targetColor)) return;
				navigation.turnTo(0, false, true);
				if (searcher.search(310, targetColor)) return;
				navigation.turnTo(180, false, true);
				//when we are at the second, fourth... verticle zone, search down
				for (double j = URy - 1 + 0.3; j > LLy; j--) {
					navigation.travelTo(i, j, true);
					if (searcher.search(110, targetColor)) return;
					navigation.turnTo(180, false, true);
					if (searcher.search(260, targetColor)) return;
					navigation.turnTo(180, false, true);
				}
			}
			counter ++;
			i = (i+2 < URx)? i+2 : (URx-0.5);
		}
	}
}
