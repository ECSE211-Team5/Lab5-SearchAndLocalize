package ca.mcgill.ecse211.sensors;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ca.mcgill.ecse211.odometer.OdometerExceptions;

/**
 * This class implements methods to manage data from our sensors
 * 
 * @author Caspar Cedro & Patrick Erath
 */
public class SensorData {
  // Sensor data parameters
  private volatile double light; // Head angle
  private volatile double distance;

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
    this.distance = 40;
    this.light = 0.5;
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
    double[] sensordata = new double[2];
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
   * Overrides distance. Use for ultra sonic sensor.
   * 
   * @param d the value of d
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
   * Overrides light. Use for light sensor.
   * 
   * @param l the value of l
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
