/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

/**
 * This class implements correction for the odometry on our robot using a light sensor.
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private static final SensorModes myColor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
  private static SampleProvider myColorSample = myColor.getMode("Red");
  private static float[] sampleColor = new float[myColor.sampleSize()];

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions {

    this.odometer = Odometer.getOdometer();

  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  public void run() {
    long correctionStart, correctionEnd;
    boolean onTopOfLine = false;

    while (true) {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      myColorSample.fetchSample(sampleColor, 0);

      // Check if sensor read black line and didn't already read the same one
      if (sampleColor[0] < .35 && !onTopOfLine) {

        // Sensed new line
        Sound.beep();
        onTopOfLine = true;

        double x = odometer.getXYT()[0];
        double y = odometer.getXYT()[1];

        if (Math.abs(x % 30.48) < Math.abs(y % 30.48)) {
          ;
          odometer.setX(Math.round(x / 30.48) * 30.48);
        } else {
          odometer.setY(Math.round(y / 30.48) * 30.48);
        }

      } else if (sampleColor[0] > .35) {
        // No longer on top of line, reset to false
        onTopOfLine = false;
      }

      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here
        }
      }
    }
  }
}
