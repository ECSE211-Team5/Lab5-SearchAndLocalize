package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class helps our robot to localize itself using the light sensor
 * 
 * @author Caspar Cedro & Percy Chen & Patrick Erath & Anssam Ghezala & Susan
 *         Matuszewski & Kamy Moussavi Kafi
 */
public class LightLocalizer {
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	private Odometer odometer;
	private SensorData data;
	private Navigation navigation;
	private static final int FORWARD_SPEED = 200;
	private static final double SENSOR_DIS = 16.3;
	private static final int blackLineColor = 25;

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
		navigation.turnTo(90, false);
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.forward();
		rightMotor.forward();
		while (data.getDL()[1] > blackLineColor)
			;
		Sound.beep();
		odometer.setX(0);
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		// 3. Go backwards by sensor-wheel center distance in x-direction
		leftMotor.rotate(Navigation.convertDistance(Lab5.LOCALIZE_WHEEL_RAD, -SENSOR_DIS), true);
		rightMotor.rotate(Navigation.convertDistance(Lab5.LOCALIZE_WHEEL_RAD, -SENSOR_DIS), false);
		// 4. Go backwards by sensor-wheel center distance in y-direction
		navigation.turnTo(0, false);
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(Navigation.convertDistance(Lab5.LOCALIZE_WHEEL_RAD, -SENSOR_DIS - 2.5), true);
		rightMotor.rotate(Navigation.convertDistance(Lab5.LOCALIZE_WHEEL_RAD, -SENSOR_DIS - 2.5), false);
		odometer.setTheta(0);
		odometer.setX(sC[0]);
		odometer.setY(sC[1]);

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
