package ca.mcgill.ecse211.sensors;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.robotics.SampleProvider;

public class GyroPoller extends Thread{
	protected SampleProvider us;
	  protected SensorData cont;
	  protected float[] lgData;
	  public GyroPoller Instance;
	  
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
	      while(distance < 0) {
	    	  distance += 360;
	      }
	      
	      while(distance > 360) {
	    	  distance -= 360;
	      }
	      cont.setA(distance); // now take action depending on value
	  }
}
