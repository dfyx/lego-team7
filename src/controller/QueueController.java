package controller;

import java.util.List;

import strategies.Strategy;

public abstract class QueueController extends Strategy {
	private List<Strategy> strategies;

	public QueueController(List<Strategy> strategies) {
		this.strategies = strategies;
	}

	protected void doInit() {
		if (strategies.size() == 0)
			throw new IllegalStateException("No strategies available");
		strategies.get(0).init();
	}

	protected void doRun() {
		//Remove all finished strategies from the beginning
		while (strategies.size()>0 && !strategies.get(0).isRunning()) {
			// Switch to next strategy
			strategies.remove(0);
			if (strategies.size() != 0) {
				strategies.get(0).init();
			}
		}
		
		if(strategies.size()>0)
			strategies.get(0).run();
		else
			setFinished();
	}
}
