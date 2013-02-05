package bluetooth;

import lejos.nxt.Button;
import lejos.nxt.Sound;

public class TurntableTest {
		
	public static void main (String[] args) {
			
		TurnControl turntableControl = new TurnControl();
		
		
		System.out.println("Press button to open");
		Button.waitForAnyPress();
		Sound.beepSequenceUp();
		
		while (!turntableControl.connectionToTurntableSuccessful());
		turntableControl.turnClockwise(90);
		turntableControl.disconnectFromTurntable();
		
		Sound.beepSequenceUp();
		
		System.out.println("Turntable was turned.");
		Button.waitForAnyPress();
	}
}
