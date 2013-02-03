package robot;

import utils.Utils;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;

/**
 * This class encapsulates the whole underbody of the robot which consists of
 * two regulated motors that move the robot in a tank-like fashion. This way you
 * can for example turn without moving forward.
 * <p>
 * To facilitate working with this rather non-intuitive way of transportation
 * this class provides some functions that do the necessary calculations.
 * <p>
 * Calling methods such as {@link #rotate(int) rotate}, {@link #move(int) move}
 * or {@link #stop() stop} do <b>not</b> change the motors' speed directly. This
 * was introduced to make sure that no conflicting commands will be executed
 * within short periods of time. Instead, call {@link #commit() commit} at the
 * end of your main loop to execute the last given command.
 * <p>
 * To make sure there is no interference, the motors used by this class should
 * <b>never</b> be manipulated by anyone else.
 */
public class Engine {
    private static final double DISTANCE_SCALE_FACTOR = 40.5 * Math.PI / 360;
    
	private static final NXTRegulatedMotor LEFT_MOTOR = Motor.A;
	private static final NXTRegulatedMotor RIGHT_MOTOR = Motor.B;

	protected int newLeftSpeed = 0;
	protected int newRightSpeed = 0;
	
	private Engine() {
	}
	
	private static final Engine INSTANCE = new Engine();
	
	/**
	 * Get an instance of Engine
	 * 
	 * @return an instance
	 */
	public static Engine getInstance() {
		return INSTANCE;
	}
	
	int lastTachoLeft = LEFT_MOTOR.getTachoCount();
	int lastTachoRight = RIGHT_MOTOR.getTachoCount();
	
	public void commit() {
		if (newLeftSpeed == 0) {
			LEFT_MOTOR.stop(true);
		} else if (newLeftSpeed > 0) {
			LEFT_MOTOR.setSpeed(newLeftSpeed);
			LEFT_MOTOR.forward();
		} else {
			LEFT_MOTOR.setSpeed(-newLeftSpeed);
			LEFT_MOTOR.backward();
		}
		if (newRightSpeed == 0) {
			RIGHT_MOTOR.stop(true);
		} else if (newRightSpeed > 0) {
			RIGHT_MOTOR.setSpeed(newRightSpeed);
			RIGHT_MOTOR.forward();
		} else {
			RIGHT_MOTOR.setSpeed(-newRightSpeed);
			RIGHT_MOTOR.backward();
		}
		
		lastTachoLeft = LEFT_MOTOR.getTachoCount();
		lastTachoRight = RIGHT_MOTOR.getTachoCount();
	}

	/**
	 * Stop the motor
	 */
	public void stop() {
		newLeftSpeed = 0;
		newRightSpeed = 0;
	}

	/**
	 * Check whether the robot is moving (or rotating).
	 * 
	 * This represents the current state of the motors, regardless of any
	 * commands that have been given but not committed.
	 * 
	 * @return <tt>true</tt>, iff the robot is either moving or rotating.
	 */
	public boolean isMoving() {
		return LEFT_MOTOR.isMoving() || RIGHT_MOTOR.isMoving();
	}

	/**
	 * Check whether the robot will be moving (or rotating) in the next cycle.
	 * 
	 * Note that this only represents current knowledge. Someone might for
	 * example call {@link #stop() stop} before the next call of {@link
	 * #commit() commit}.
	 * 
	 * @return <tt>true</tt>, iff the robot will either either be moving or
	 * rotating.
	 */
	public boolean willBeMoving() {
		return newLeftSpeed * newRightSpeed != 0;
	}

	/**
	 * Rotate the robot on the spot.
	 * <p>
	 * This is just a convenience function for {@link #move(int, int)}.
	 * 
	 * @param speed
	 *            The speed used for rotation. If between -1000 and 0, the
	 *            robot rotates left. If between 0 and 1000, it rotates right.
	 */
	public void rotate(int speed) {
		if (speed < 0)
			move(-speed, -1000);
		else
			move(speed, 1000);
	}

	/**
	 * Move straight forward with a given speed.
	 * <p>
	 * This is just a convenience function for {@link #move(int, int)}.
	 * 
	 * @param speed
	 *            The speed to move with. If between -1000 and 0, the robot
	 *            moves backward. If between 0 it 1000, it moves forward.
	 */
	public void move(int speed) {
		move(speed, 0);
	}

	/**
	 * Move in a circle See also #move
	 * 
	 * @param speed
	 *            Should not be above 500
	 * @param innerRadius
	 *            Should not be below ~100 mm (in mm)
	 */
	public void moveCircle(int speed, int innerRadius) {
		int oldInner = innerRadius;
		if (innerRadius < 50)
			throw new IllegalArgumentException(
					"inner radius of circle to small: " + innerRadius + " < "
							+ 50);
		
		// TODO SB this factor varies with distance.
		// diameter not radius!
		// desired   3   4   6  <- real with radius/x
		//      10  15  12  10
		//      20  22  18  13
		//      30  30  25  18
		//      60  49  40  30
		innerRadius /= 3;

		int wheelWidth = 92; // in mm

		int outerRadius = innerRadius + wheelWidth;

//		System.out.println("Driving (inner/outer): " + innerRadius + "mm / "
//				+ outerRadius + "mm with real inner: " + oldInner + "mm");

		int outerSpeed = speed;

		// innerSpeed = speedCoefficient*outerSpeed
		//
		int speedCoefficientUp = innerRadius;
		int speedCoefficientDown = outerRadius;

		int innerSpeed = (speedCoefficientUp * outerSpeed)
				/ speedCoefficientDown;

		newRightSpeed = innerSpeed;
		newLeftSpeed = outerSpeed;

		commit();

		Delay.msDelay(100);

//		System.out.println("desL / realL  ||  desR / realR: " + newLeftSpeed
//				+ " / " + LEFT_MOTOR.getRotationSpeed() + "  ||  "
//				+ newRightSpeed + " / " + RIGHT_MOTOR.getRotationSpeed());
	}

	/**
	 * Move forward or backward in a curved path. See also #moveCircle
	 * 
	 * This can be used to move in any way the engine setup allows ranging from
	 * a straight line to a turn on the spot.
	 * <p>
	 * Note that <tt>speed</tt> doesn't refer exclusively to forward/backward
	 * movement but also to rotation speed while <tt>direction</tt> determines
	 * whether the robot will move in a straight line, in a curve or on the
	 * spot. Therefore a <tt>speed</tt> of 0 always means that the robot will
	 * not move at all.
	 * 
	 * @param speed
	 *            The speed. If between -1000 and 0, it moves backward. If
	 *            between 0 and 1000, it moves forward.
	 * @param direction
	 *            The direction to move (speed difference between left and right
	 *            chain). Between -1000 and 0 for moving left and between 0 and
	 *            1000 for moving right.
	 */
	public void move(int speed, int direction) {
		if (1000 < speed || -1000 > speed || 1000 < direction
				|| -1000 > direction)
			throw new IllegalStateException("Incorrect parameters speed:"
					+ speed + ", direction:" + direction);

		final int MAX = 1000;

		if (speed < -MAX || speed > MAX) {
			throw new IllegalArgumentException("Speed must be between "
					+ -MAX	+ " and " + MAX);
		}

		if (direction < -MAX || direction > MAX) {
			throw new IllegalArgumentException("Direction must be between "
					+ -MAX + " and " + MAX);
		}

		// Calculate linear function
		int left = MAX + 2 * direction;
		int right = MAX - 2 * direction;

		// Clamp to valid region
		left = Utils.clamp(left, -MAX, MAX);
		right = Utils.clamp(right, -MAX, MAX);

		/* This results in following function for the right motor
		 *
		 *  1000 :---------:          :
		 *       :         :\         :
		 *       :         : \        :
		 *       :         :  \       :
		 *       :         :   \      :
		 *     0.:.........:....\.....:.
		 *       :         :     \    :
		 *       :         :      \   :
		 *       :         :       \  :
		 *       :         :        \ :
		 * -1000 :         :         \:
		 *
		 *    -1000       0        1000
		 *
		 * For the left motor this is mirrored on the x-axis.
		 */

		// Fix point multiplication with speed
		left = left * speed / MAX;
		right = right * speed / MAX;

		newLeftSpeed = left;
		newRightSpeed = right;
	}
	
	/**
	 * Returns an estimate of the distance which has been covered since the last
	 * call to {@link #commit()}. The estimate will be more accurate for high
	 * driving speeds.
	 * 
	 * @return the estimated distance in mm
	 */
	public int estimateDistance() {
        return (int) ((LEFT_MOTOR.getTachoCount() - lastTachoLeft
                + RIGHT_MOTOR.getTachoCount() - lastTachoRight) 
                / 2 * DISTANCE_SCALE_FACTOR);
	}
}
