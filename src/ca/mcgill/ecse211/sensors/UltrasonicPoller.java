package ca.mcgill.ecse211.sensors;

import lejos.robotics.SampleProvider;

/**
 * This class implements the Ultrasonic Sensor Poller for our Wall Follower.
 * 
 * Control of the wall follower is applied periodically by the UltrasonicPoller thread. The while
 * loop at the bottom executes in a loop. Assuming that the us.fetchSample, and cont.processUSData
 * methods operate in about 20mS, and that the thread sleeps for 50 mS at the end of each loop, then
 * one cycle through the loop is approximately 70 mS. This corresponds to a sampling rate of 1/70mS
 * or about 14 Hz.
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class UltrasonicPoller extends Thread {
  private SampleProvider us;
  private SensorData cont;
  private float[] usData;

  /**
   * This constructor creates an instance of the UltrasonicPoller class to provide distance data
   * from an ultrasonic sensor to our Wall Follower.
   * 
   * @param us a SampleProvider class instance that helps us to store an array of ultrasonic sensor
   *        data.
   * @param usData an array of distance data to be used by our Wall Follower's
   *        UltrasonicControllers.
   * @param cont a BangBangController or PController instance that has accumulated distance data
   *        stored in usData passed to it.
   */
  public UltrasonicPoller(SampleProvider us, float[] usData, SensorData cont) {
    this.us = us;
    this.cont = cont;
    this.usData = usData;
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
      us.fetchSample(usData, 0); // acquire data
      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
      cont.setD(distance); // now take action depending on value
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }
}
