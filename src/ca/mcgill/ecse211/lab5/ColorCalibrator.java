package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.sensors.SensorData;

public class ColorCalibrator {
	private static Color currentColor;
	
	public static enum Color{
		Orange, Green, Blue, Yellow, Other
	}
	
	//TODO: Implement color Calibration
	public static Color getColor(int r, int g, int b) {
		if ((r >= 7 && r <= 8) && (g >= 3 && g <= 6) && (b >=0 && b <= 1)) {
			currentColor = Color.Yellow;
		} 
		else if ((r >= 0 && r <= 1) && (g >= 5 && g <= 8) && (b >=3 && b <= 5)) {
			currentColor = Color.Blue;
		}
		else if ((r >= 2 && r <= 3) && (g >= 6 && g <= 8) && (b >= 0 && b <= 1)) {
			currentColor = Color.Green;
		}
		else if ((r >= 4 && r <= 5) && (g >= 0 && g <= 1) && (b == 0)) {
			currentColor = Color.Orange;
		}
		else {
			currentColor = Color.Other;
		}
	
		return currentColor;
	}
	
	public static Color getColor() {
		if (currentColor != null) return currentColor;
		else return Color.Other;
	}
}
