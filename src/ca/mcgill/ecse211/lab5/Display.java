package ca.mcgill.ecse211.lab5;

import java.text.DecimalFormat;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.SensorData;
import lejos.hardware.lcd.TextLCD;

/**
 * This class is used to display the content of the odometer variables (x, y, Theta)
 */
public class Display implements Runnable {
  private SensorData sensdata;
  private Odometer odo;
  private TextLCD lcd;
  private double[] position;
  private double[] data;
  private double[] rgb;
  private final long DISPLAY_PERIOD = 25;
  private long timeout = Long.MAX_VALUE;

  /**
   * This is the class constructor
   * 
   * @param odoData
   * @throws OdometerExceptions
   */
  public Display(TextLCD lcd) throws OdometerExceptions {
    this.odo = Odometer.getOdometer();
    this.sensdata = SensorData.getSensorData();
    this.lcd = lcd;
  }

  /**
   * This is the overloaded class constructor
   * 
   * @param odoData
   * @throws OdometerExceptions
   */
  public Display(TextLCD lcd, long timeout) throws OdometerExceptions {
    odo = Odometer.getOdometer();
    this.timeout = timeout;
    this.lcd = lcd;
  }

  /**
   * This method is called when the Display thread is started.
   */
  public void run() {

    lcd.clear();

    long updateStart, updateEnd;

    long tStart = System.currentTimeMillis();
    do {
      updateStart = System.currentTimeMillis();

      // Retrieve x, y and Theta information
      position = odo.getXYT();
      data = sensdata.getDL();
      rgb = sensdata.getRGB()[0];
      
      // Print x,y, and theta information
      DecimalFormat numberFormat = new DecimalFormat("######0.00");
      lcd.drawString("X: " + numberFormat.format(position[0]), 0, 0);
      lcd.drawString("Y: " + numberFormat.format(position[1]), 0, 1);
      lcd.drawString("T: " + numberFormat.format(position[2]), 0, 2);
      lcd.drawString("D: " + numberFormat.format(data[0]), 0, 3);
      lcd.drawString("L: " + numberFormat.format(data[1]), 0, 4);
      lcd.drawString(String.format("(r: %d, g: %d, b: %d)", (int)rgb[0],(int)rgb[1],(int)rgb[2]), 0, 5);
      
      // this ensures that the data is updated only once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < DISPLAY_PERIOD) {
        try {
          Thread.sleep(DISPLAY_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } while ((updateEnd - tStart) <= timeout);
  }
}
