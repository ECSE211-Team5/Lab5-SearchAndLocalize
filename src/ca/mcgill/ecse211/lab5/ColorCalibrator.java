package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.sensors.SensorData;

public class ColorCalibrator {
	
	public static enum Color{
		Orange, Green, Blue, Yellow, Other
	}
	
	//TODO: Implement color Calibration
	public static Color getColor(int r, int g, int b) {
		if ((r >= 8 && r <= 10) && (g >= 5 && g <= 7) && (b == 1)) {
			return Color.Yellow;
		} 
		else if ((r >= 1 && r <= 2) && (g >= 8 && g <= 10) && (b >=6 && b <= 8)) {
			return Color.Blue;
		}
		else if ((r >= 2 && r <= 3) && (g >= 6 && g <= 8) && (b >= 0 && b <= 1)) {
			return Color.Green;
		}
		else if ((r >= 6 && r <= 7) && (g >= 1 && g <= 2) && (b == 0)) {
			return Color.Orange;
		}
		else {
		return Color.Other;
		}
		
	}
}
