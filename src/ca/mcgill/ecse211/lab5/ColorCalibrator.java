package ca.mcgill.ecse211.lab5;

/**
 * This class is used to check the color of a ring under a light sensor
 * 
 * @author Caspar Cedro
 * @author Percy Chen
 * @author Patrick Erath
 * @author Anssam Ghezala
 * @author Susan Matuszewski
 * @author Kamy Moussavi Kafi
 */
public class ColorCalibrator {
  private static Color currentColor;

  /**
   * This enumeration contains the possible colors of the ring under a light sensor
   */
  public static enum Color {
    Orange, Green, Blue, Yellow, Other
  }

  private final static int lowerYellowRBound = 8, upperYellowRBound = 12, lowerYellowGBound = 6,
      upperYellowGBound = 8, lowerYellowBBound = 0, upperYellowBBound = 1, lowerBlueRBound = 1,
      upperBlueRBound = 2, lowerBlueGBound = 4, upperBlueGBound = 12, lowerBlueBBound = 5,
      upperBlueBBound = 10, lowerGreenRBound = 2, upperGreenRBound = 4, lowerGreenGBound = 6,
      upperGreenGBound = 9, lowerGreenBBound = 0, upperGreenBBound = 1, lowerOrangeRBound = 6,
      upperOrangeRBound = 10, lowerOrangeGBound = 1, upperOrangeGBound = 3, OrangeBBound = 0;

  /**
   * This method returns the color of the ring currently under the light sensor
   * 
   * @param r The red value to check for a ring
   * @param g The green value to check for a ring
   * @param b The blue value to check for a ring
   * @return A Color enumeration value
   */
  public static Color getColor(int r, int g, int b) {
    if ((r >= lowerYellowRBound && r <= upperYellowRBound)
        && (g >= lowerYellowGBound && g <= upperYellowGBound)
        && (b >= lowerYellowBBound && b <= upperYellowBBound)) {
      currentColor = Color.Yellow;
    } else if ((r >= lowerBlueRBound && r <= upperBlueRBound)
        && (g >= lowerBlueGBound && g <= upperBlueGBound)
        && (b >= lowerBlueBBound && b <= upperBlueBBound)) {
      currentColor = Color.Blue;
    } else if ((r >= lowerGreenRBound && r <= upperGreenRBound)
        && (g >= lowerGreenGBound && g <= upperGreenGBound)
        && (b >= lowerGreenBBound && b <= upperGreenBBound)) {
      currentColor = Color.Green;
    } else if ((r >= lowerOrangeRBound && r <= upperOrangeRBound)
        && (g >= lowerOrangeGBound && g <= upperOrangeGBound) && (b == OrangeBBound)) {
      currentColor = Color.Orange;
    } else {
      currentColor = Color.Other;
    }

    return currentColor;
  }

  /**
   * This method gets the last color of the ring under the light sensor
   * 
   * @return A Color enumeration value
   */
  public static Color getColor() {
    if (currentColor != null)
      return currentColor;
    else
      return Color.Other;
  }
}
