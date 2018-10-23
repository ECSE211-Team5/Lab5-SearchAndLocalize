package ca.mcgill.ecse211.sensors;

import java.util.Arrays;
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
 * @author Caspar Cedro & Percy Chen & Patrick Erath & Anssam Ghezala & Susan Matuszewski & Kamy
 *         Moussavi Kafi
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
    int sample[] = new int[5];
    int sorted_sample[] = new int[5];
    int sample_index = 0;
    boolean first_five = true;
    int first_five_counter = 0;
    while (true) {
      us.fetchSample(usData, 0); // acquire data
      distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int

      // filter value
      sample[sample_index] = distance;
      sorted_sample = sample.clone();
      Arrays.sort(sorted_sample);

      // keep track of how many samples have been taken up to 5
      if (first_five) {
        first_five_counter++;
        if (first_five_counter > 4) {
          first_five = false;
        }
      }
      // no adjustments for first 5 samples to fill array
      if (!first_five) {
        if (distance > sorted_sample[2]) {
          distance = sorted_sample[2];
        }
      }

      cont.setD(distance); // now take action depending on value
      sample_index = (sample_index + 1) % 5;

      try {
        Thread.sleep(30);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }
  }
}
