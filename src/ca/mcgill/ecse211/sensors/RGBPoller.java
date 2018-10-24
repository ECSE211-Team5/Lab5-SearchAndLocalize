package ca.mcgill.ecse211.sensors;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.robotics.SampleProvider;

/**
 * This class polls a light sensor that is used to detect colored rings.
 * 
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
public class RGBPoller extends LightPoller {
  private static int numPoller = 0;
  private int index;

  /**
   * This constructor creates an instance of the RGBPoller class to provide color data from an light
   * sensor to our robot.
   * 
   * @param us A SampleProvider class instance that helps us to store an array of ultrasonic sensor
   *        data.
   * @param usData An array to store light data.
   * @param cont A SensorData object that is used to process color data.
   * @throws OdometerExceptions
   */
  public RGBPoller(SampleProvider us, float[] usData, SensorData cont) throws OdometerExceptions {
    super(us, usData, cont);
    index = numPoller++;
  }

  @Override
  protected void processData() {
    us.fetchSample(lgData, 0); // acquire data at offset 0
    // get RGB values from buffer, multiply by 100 for convenience and allow it to be cast to int
    int r = (int) (lgData[0] * 100); // extract from buffer, cast to int
    int g = (int) (lgData[1] * 100); // extract from buffer, cast to int
    int b = (int) (lgData[2] * 100); // extract from buffer, cast to int
    cont.setRGB(index, r, g, b); // now take action depending on value
  }
}
