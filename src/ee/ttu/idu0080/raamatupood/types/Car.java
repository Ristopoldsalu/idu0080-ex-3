package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;

/**
 * implements Serializable, et saaks saata ühest JVM-st teise.
 */
public class Car implements Serializable {
	private static final long serialVersionUID = 1L;
	public int doors;

	public Car(int doors) {
		this.doors = doors;
	}

	public String toString() {
		return "car has " + doors + " doors";
	}
	
	public int getDoors()
	{
		return doors;
	}
}
