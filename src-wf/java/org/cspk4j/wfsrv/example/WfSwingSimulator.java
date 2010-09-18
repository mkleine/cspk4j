package org.cspk4j.wfsrv.example;

import org.cspk4j.CspEnvironment;
import org.cspk4j.simulator.SwingCspSimulator;


public class WfSwingSimulator extends SwingCspSimulator {

	static int counter;
	static final Object lock = new Object();
	
	static String getName() {
		synchronized(lock) {
			return ("example chooser "+(counter++));
		}
	}
	
	public WfSwingSimulator(CspEnvironment e) {
		super(getName(),e);
	}

	@Override
	public void run(String name) {
		super.run(name);
		dispose();
	}
	
}
