package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class helps our robot to localize itself using the light sensor
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class LightLocalizer {
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	private Odometer odometer;
	private SensorData data;
	private Navigation navigation;
	private static final double GO_BACK_DISTANCE = 0.6;
	private static final int CORRECT_ANGLE_FACTOR = 265; 

	private static final int FORWARD_SPEED = 150;
	private static final double SENSOR_DIS = 16.3;
	private static final int blackLineColor = 25;
	private double TRACK = 10.7;
	private double WHEEL_RAD = 2.2;

	/**
	 * This is the class constructor
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @throws OdometerExceptions
	 */
	public LightLocalizer(Navigation nav, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor)
			throws OdometerExceptions {
		this.odometer = Odometer.getOdometer();
		this.data = SensorData.getSensorData();
		this.navigation = nav;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}

	/**
	 * Once the robot know what angle it is facing, this method looks for the x,y
	 * axis origins knowing it is in the first tile facing north.
	 */
	public void localize(int[] sC) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		// 1. GO forward find the y=0 line
		leftMotor.forward();
		rightMotor.forward();
		while (data.getDL()[1] > blackLineColor)
			;
		Sound.beep();
		odometer.setY(0);

		// 2. Turn and go forward find the x=0 line
		navigation.turnTo(90);
		leftMotor.forward();
		rightMotor.forward();
		while (data.getDL()[1] > blackLineColor)
			;
		Sound.beep();
		odometer.setX(0);

		// 3. Go backwards by sensor-wheel center distance in x-direction
		leftMotor.rotate(Navigation.convertDistance(Lab5.WHEEL_RAD, -SENSOR_DIS), true);
		rightMotor.rotate(Navigation.convertDistance(Lab5.WHEEL_RAD, -SENSOR_DIS), false);
		// 4. Go backwards by sensor-wheel center distance in y-direction
		navigation.turnTo(0);
	    leftMotor.rotate(Navigation.convertDistance(Lab5.WHEEL_RAD, -SENSOR_DIS), true);
	    rightMotor.rotate(Navigation.convertDistance(Lab5.WHEEL_RAD, -SENSOR_DIS), false);
		odometer.setTheta(0);
		odometer.setX(sC[0]);
		odometer.setY(sC[1]);
		// turn 40 degree relative to current rotation (likely to be 0)
//		navigation.turnTo(45);
//		leftMotor.setSpeed(300);
//		rightMotor.setSpeed(300);
//		// first go forward until reaches a black line
//		while (!isBlackLineTriggered()) {
//			leftMotor.forward();
//			rightMotor.forward();
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		navigation.stop();
//
//		// Then go back until the center of the robot rotation is in the position to
//		// perform color
//		// sensor localization
//		double[] position = odometer.getXYT();
//		navigation.travelTo(position[0] - GO_BACK_DISTANCE, position[1] - GO_BACK_DISTANCE, false);
//
//		// go back to 0 rotation to prepare for the localization
//		navigation.turnTo(0);
//
//		// prepare input for the localization, rotate to detect 4 black lines and record
//		// the angle
//		double thetaX1, thetaX2, thetaY1, thetaY2;
//		thetaX1 = getRorationOnBlackLine();
//		thetaY1 = getRorationOnBlackLine();
//		thetaX2 = getRorationOnBlackLine();
//		thetaY2 = getRorationOnBlackLine();
//		navigation.turnTo(thetaY2);
//
//		// start using formula to calculate deltaX and deltaY to the origin, as well as
//		// a more
//		// precise angle
//		double thetaY = (thetaY2 - thetaY1) / 2;
//		double thetaX = (thetaX2 - thetaX1) / 2;
//		double deltaX = -SENSOR_DIS * Math.cos(Math.toRadians((thetaY)));
//		double deltaY = -SENSOR_DIS * Math.cos(Math.toRadians((thetaX)));
//		double deltaTheta = 90 + deltaY - (thetaY2 - 180);
//
//		// correct to 0 rotation
//		navigation.rotate(deltaTheta - CORRECT_ANGLE_FACTOR);
//		odometer.setTheta(0);
//
//		// go to origin and turn to 0 rotation
//		odometer.setXYT(deltaX / Lab5.TILE, deltaY / Lab5.TILE, 0);
//		navigation.travelTo(0, 0, false);
//		navigation.turnTo(0);
//
//		odometer.setX(1);
//		odometer.setY(1);
		// Now at SC ! :)
	}
	
	public double getRorationOnBlackLine() {
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		while (!isBlackLineTriggered()) {
			leftMotor.forward();
			rightMotor.backward();
		}
		Sound.beep();

		return odometer.getXYT()[2];
	}
	
	private boolean isBlackLineTriggered() {
		return data.getDL()[1] < blackLineColor;
	}

	/**
	 * This method allows the conversion of a distance to the total rotation of each
	 * wheel need to cover that distance.
	 * 
	 * @param radius   The radius of our wheels
	 * @param distance The distance travelled
	 * @return A converted distance
	 */
	public static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * This method allows the conversion of an angle value
	 * 
	 * @param radius   The radius of our wheels
	 * @param distance The distance travelled
	 * @param angle    The angle to convert
	 * @return A converted angle
	 */
	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
