package ca.mcgill.ecse211.sensors;

import ca.mcgill.ecse211.lab5.ColorCalibrator;
import ca.mcgill.ecse211.lab5.Lab5;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * This class implements the Light Sensor Poller for our robot
 * 
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
public class LightPoller extends Thread {
  protected SampleProvider us;
  protected SensorData cont;
  protected float[] lgData;

  /**
   * This constructor creates an instance of the LightPoller class to provide distance data from an
   * light sensor to our robot.
   * 
   * @param us a SampleProvider class instance that helps us to store an array of ultrasonic sensor
   *        data.
   * @param lgData an array of distance data to be used by our Wall Follower's
   *        UltrasonicControllers.
   * @param cont a BangBangController or PController instance that has accumulated distance data
   *        stored in usData passed to it.
   * @throws OdometerExceptions
   */
  public LightPoller(SampleProvider us, float[] lgData, SensorData cont) throws OdometerExceptions {
    this.us = us;
    this.cont = cont;
    this.lgData = lgData;
  }

  /**
   * This method is called by a UltrasonicPoller (Thread) instance when it is asked to start
   * executing
   * 
   * Sensors now return floats using a uniform protocol. Need to convert US result to an integer
   * [0,255] (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  public void run() {
    while (!Lab5.foundRing) {
      processData();
      if (ColorCalibrator.getColor() == Lab5.targetColor) {
    //    Sound.twoBeeps();
        Sound.beep();
        Lab5.foundRing = true;
      } else if (ColorCalibrator.getColor() != ColorCalibrator.Color.Other) {
        Sound.twoBeeps();
        
        try {
          Thread.sleep(30);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }

  protected void processData() {
    us.fetchSample(lgData, 0); // acquire data
    int distance = (int) (lgData[0] * 100); // extract from buffer, multiply by 100 for convenience
                                            // and allow it to be cast to int
    cont.setL(distance); // now take action depending on value
  }
}
