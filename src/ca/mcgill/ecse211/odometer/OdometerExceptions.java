package ca.mcgill.ecse211.odometer;

/**
 * This class is used to handle errors regarding the singleton pattern used for the odometer and
 * odometerData
 *
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
@SuppressWarnings("serial")
public class OdometerExceptions extends Exception {
  /**
   * This is an OdometerExceptions class constructor that accepts a descriptive error message.
   * 
   * @param Error a String that contains an error message
   */
  public OdometerExceptions(String Error) {
    super(Error);
  }
}
