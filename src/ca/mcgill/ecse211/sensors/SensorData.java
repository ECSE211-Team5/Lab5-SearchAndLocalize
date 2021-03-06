package ca.mcgill.ecse211.sensors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ca.mcgill.ecse211.odometer.OdometerExceptions;

/**
 * This class implements methods to manage data from our sensors
 * 
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
public class SensorData {
  // Sensor data parameters
  private volatile double light; // Head angle
  private volatile double distance;
  private volatile double angle;
  private volatile int rgb[][];

  // Class control variables
  private volatile static int numberOfIntances = 0; // Number of OdometerData
                                                    // objects instantiated
                                                    // so far
  private static final int MAX_INSTANCES = 1; // Maximum number of
                                              // OdometerData instances

  // Thread control tools
  private static Lock lock = new ReentrantLock(true); // Fair lock for
                                                      // concurrent writing
  private volatile boolean isReseting = false; // Indicates if a thread is
                                               // trying to reset any
                                               // position parameters
  private Condition doneReseting = lock.newCondition(); // Let other threads
                                                        // know that a reset
                                                        // operation is
                                                        // over.

  private static SensorData sensorData = null;

  /**
   * Default constructor. The constructor is private. A factory is used instead such that only one
   * instance of this class is ever created.
   */
  protected SensorData() {
    // Default distance value is 40 cm from any walls.
    this.distance = 40;
    // Default light value is 0.5
    this.light = 0.5;
    rgb = new int[1][3];
    for (int i = 0; i < rgb.length; i++) {
      for (int j = 0; j < rgb[i].length; j++) {
        rgb[i][j] = 0;
      }
    }
  }

  /**
   * OdometerData factory. Returns an OdometerData instance and makes sure that only one instance is
   * ever created. If the user tries to instantiate multiple objects, the method throws a
   * MultipleOdometerDataException.
   * 
   * @return An OdometerData object
   * @throws OdometerExceptions
   */
  public synchronized static SensorData getSensorData() throws OdometerExceptions {
    if (sensorData != null) { // Return existing object
      return sensorData;
    } else if (numberOfIntances < MAX_INSTANCES) { // create object and
                                                   // return it
      sensorData = new SensorData();
      numberOfIntances += 1;
      return sensorData;
    } else {
      throw new OdometerExceptions("Only one intance of the SensorData can be created.");
    }

  }

  /**
   * Return the Sensor data.
   * 
   * @param position the array to store the sensor data
   * @return the sensor data.
   */
  public double[] getDL() {
    double[] sensordata = new double[4];
    lock.lock();
    try {
      while (isReseting) { // If a reset operation is being executed, wait
        // until it is over.
        doneReseting.await(); // Using await() is lighter on the CPU
        // than simple busy wait.
      }

      sensordata[0] = distance;
      sensordata[1] = light;
    } catch (InterruptedException e) {
      // Print exception to screen
      e.printStackTrace();
    } finally {
      lock.unlock();
    }

    return sensordata;
  }

  /**
   * get the rgb data for color sensor
   * 
   * @return: rgb data
   */
  public int[][] getRGB() {
    lock.lock();
    try {
      while (isReseting) {
        doneReseting.await();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
    return rgb.clone();
  }

  /**
   * This method returns the currently stored angle value
   * 
   * @return The current angle value
   */
  public double getA() {
    lock.lock();
    try {
      while (isReseting) {
        doneReseting.await();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
    return angle;
  }

  /**
   * This method overwrites the distance value. Use for ultrasonic sensor.
   * 
   * @param d The value to overwrite distance with
   */
  public void setD(double d) {
    lock.lock();
    isReseting = true;
    try {
      this.distance = d;
      isReseting = false; // Done reseting
      doneReseting.signalAll(); // Let the other threads know that you are
                                // done reseting
    } finally {
      lock.unlock();
    }
  }

  /**
   * This method overwrites the angle value.
   * 
   * @param a The value to overwrite angle with
   */
  public void setA(double a) {
    lock.lock();
    isReseting = true;
    try {
      this.angle = a;
      isReseting = false; // Done reseting
      doneReseting.signalAll(); // Let the other threads know that you are
                                // done reseting
    } finally {
      lock.unlock();
    }
  }

  /**
   * set rgb data for color sensor with specific id
   * 
   * @param i: id for the color sensor
   * @param r: red value
   * @param g: green value
   * @param b: blue value
   */
  public void setRGB(int i, int r, int g, int b) {
    lock.lock();
    isReseting = true;
    try {
      rgb[i][0] = r;
      rgb[i][1] = g;
      rgb[i][2] = b;
      isReseting = false; // Done reseting
      doneReseting.signalAll(); // Let the other threads know that you are
                                // done reseting
    } finally {
      lock.unlock();
    }
  }

  /**
   * This method overwrites the light value.
   * 
   * @param l The value to overwrite the current light value with
   */
  public void setL(double l) {
    lock.lock();
    isReseting = true;
    try {
      this.light = l;
      isReseting = false; // Done reseting
      doneReseting.signalAll(); // Let the other threads know that you are
                                // done reseting
    } finally {
      lock.unlock();
    }
  }
}
