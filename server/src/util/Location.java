package util;

/**
 * 
 * This object is used to simulate server/client locations and calculate ping for testing.
 * 
 * This could just be a awt.Point object, but we are using floats instead of ints.
 * 
 * @author Christian
 *
 */
public class Location {
	
	public final float x, y;
	
	public Location(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
