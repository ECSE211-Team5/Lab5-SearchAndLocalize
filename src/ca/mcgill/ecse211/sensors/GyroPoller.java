package ca.mcgill.ecse211.sensors;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.robotics.SampleProvider;

/**
 * This class helps our robot to navigate using a gyroscope
 * 
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
public class GyroPoller extends Thread {
  protected SampleProvider us;
  protected SensorData cont;
  protected float[] lgData;
  public GyroPoller Instance;

  /**
   * This constructor creates an instance of the GyroPoller class to aid navigation
   * 
   * @param us A SampleProvider class instance that helps us to store an array of ultrasonic sensor
   *        data.
   * @param lgData An array used to store data.
   * @param cont A SensorData object instance used to manage sensor data.
   * @throws OdometerExceptions
   */
  public GyroPoller(SampleProvider us, float[] lgData, SensorData cont) throws OdometerExceptions {
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
    while (true) {
      processData();
    }
  }

  protected void processData() {
    us.fetchSample(lgData, 0); // acquire data
    int distance = (int) (lgData[0]); // extract from buffer, cast to int
    // Ensure the distance is between 0 and 360
    while (distance < 0) {
      distance += 360;
    }

    while (distance > 360) {
      distance -= 360;
    }
    cont.setA(distance); // now take action depending on value
  }
}
