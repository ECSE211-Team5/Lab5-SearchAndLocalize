package ca.mcgill.ecse211.sensors;

import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.robotics.SampleProvider;

public class RGBPoller extends LightPoller {
	private static int numPoller = 0;
	private int index;

	public RGBPoller(SampleProvider us, float[] usData, SensorData cont) throws OdometerExceptions {
		super(us, usData, cont);
		index = numPoller++;
	}

	@Override
	protected void processData() {
		us.fetchSample(lgData, 0); // acquire data
		int r = (int) (lgData[0] * 100); // extract from buffer, cast to int
		int g = (int) (lgData[1] * 100); // extract from buffer, cast to int
		int b = (int) (lgData[2] * 100); // extract from buffer, cast to int
		cont.setRGB(index, r, b, b); // now take action depending on value
	}
}
