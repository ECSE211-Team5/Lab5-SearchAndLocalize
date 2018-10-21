package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class RingSearcher {
	private static final int FORWARD_SPEED = 150;
	private static final int ROTATE_SPEED = 50;
	private static final int RING_DISTANCE = 50;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Navigation navigation;
	private Odometer odometer;
	private SensorData data;

	/**
	 * 
	 * @param nav
	 * @param leftMotor
	 * @param rightMotor
	 * @throws OdometerExceptions
	 */
	public RingSearcher(Navigation nav, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor)
			throws OdometerExceptions {
		this.odometer = Odometer.getOdometer();
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		navigation = nav;
		data = SensorData.getSensorData();
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { this.leftMotor, this.rightMotor }) {
			motor.stop();
			motor.setAcceleration(300);
		}
	}

	public boolean search(int angle, ColorCalibrator.Color target) {
		navigation.turnTo(angle, true);
		double[] position = odometer.getXYT();
		boolean foundRing = false;
		boolean ColorMatched = false;
		while(leftMotor.isMoving() || rightMotor.isMoving()) {
			if(data.getDL()[0] < RING_DISTANCE) {
				foundRing = true;
				break;
			}
		}
		
		if(foundRing) {
			ColorCalibrator.Color color = goForRingColor();
			Sound.beep();
			if(color == target) ColorMatched = true;
		}
		
		Sound.beep();
		navigation.travelBackTo(position[0], position[1]);
		navigation.turnTo(0, false);
		return ColorMatched;
	}

	
	private ColorCalibrator.Color goForRingColor() {
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		while(ColorCalibrator.getColor() == ColorCalibrator.Color.Other) {
			leftMotor.forward();
			rightMotor.forward();
		}
		leftMotor.stop(true);
		leftMotor.stop(false);
		ColorCalibrator.Color color = ColorCalibrator.getColor();
		return color;
	}
}
