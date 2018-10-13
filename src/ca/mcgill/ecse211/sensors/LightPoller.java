package ca.mcgill.ecse211.sensors;

import lejos.robotics.SampleProvider;

/**
 * This class implements the Light Sensor Poller for our robot
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class LightPoller extends Thread {
  private SampleProvider us;
  private SensorData cont;
  private float[] lgData;

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
   */
  public LightPoller(SampleProvider us, float[] lgData, SensorData cont) {
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
    int distance;
    while (true) {
      us.fetchSample(lgData, 0); // acquire data
      distance = (int) (lgData[0] * 100); // extract from buffer, cast to int
      cont.setL(distance); // now take action depending on value
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }
}
