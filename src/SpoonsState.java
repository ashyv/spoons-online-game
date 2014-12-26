import java.io.Serializable;
import java.util.ArrayList;


public class SpoonsState implements Serializable{
	private double timeLeft;
	ArrayList<SpoonsCard> hand;
	boolean canGrabSpoon;
	int numSpoonsLeft;
	
	//private final static int GRAB_SPOON  = 0;
	
	public SpoonsState(ArrayList<SpoonsCard> hand, boolean canGrabSpoon, int numSpoonsLeft){
		this.hand = hand;
		this.canGrabSpoon = canGrabSpoon;
		this.numSpoonsLeft = numSpoonsLeft;
	}
}
