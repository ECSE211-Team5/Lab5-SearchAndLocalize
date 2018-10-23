package ca.mcgill.ecse211.lab5;

/**
 * 
 * @author Caspar Cedro & Percy Chen & Patrick Erath & Anssam Ghezala & Susan Matuszewski & Kamy
 *         Moussavi Kafi
 *
 */
public class ColorCalibrator {
  private static Color currentColor;

  public static enum Color {
    Orange, Green, Blue, Yellow, Other
  }

  // TODO: Implement color Calibration
  public static Color getColor(int r, int g, int b) {
    if ((r >= 8 && r <= 12) && (g >= 6 && g <= 8) && (b >= 0 && b <= 1)) {
      currentColor = Color.Yellow;
    } else if ((r >= 1 && r <= 2) && (g >= 4 && g <= 12) && (b >= 5 && b <= 10)) {
      currentColor = Color.Blue;
    } else if ((r >= 2 && r <= 4) && (g >= 6 && g <= 9) && (b >= 0 && b <= 1)) {
      currentColor = Color.Green;
    } else if ((r >= 6 && r <= 10) && (g >= 1 && g <= 3) && (b == 0)) {
      currentColor = Color.Orange;
    } else {
      currentColor = Color.Other;
    }

    return currentColor;
  }

  public static Color getColor() {
    if (currentColor != null)
      return currentColor;
    else
      return Color.Other;
  }
}
