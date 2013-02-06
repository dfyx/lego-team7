package strategies.util;

import strategies.Strategy;

/**
 * This strategy implements a state machine
 *
 * @param <State> The enum of all states
 */
public abstract class StateMachineStrategy<State> extends Strategy {
	
	private State startState;
	private State currentState;
	
	/**
	 * 
	 * @param startState The initial state
	 */
	public StateMachineStrategy(State startState) {
		this.startState = startState;
	}
	
	/**
	 * This method is called every cycle.
	 * 
	 * @param currentState The current state in the cycle
	 * @return The new state for the next cycle
	 */
	abstract protected State run(State currentState);
	
	@Override
	protected final void doInit() {
		currentState = startState;
	}

	@Override
	protected final void doRun() {
		currentState = run(currentState);
	}
}
