/**
 *   This file is part of CSPk4J the CSP concurrency library for Java.
 *
 *   CSPk4J is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CSPk4J is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CSPk4J.  If not, see <http://www.gnu.org/licenses/>.
 *
**/
package org.cspk4j.simulator;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEvent;
import org.cspk4j.DefaultFilter;
import org.cspk4j.Filter;
import org.cspk4j.CspEnvironment.Listener;


public abstract class CspSimulator implements Runnable, Listener {
	
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	protected final CspEnvironment environment;
	protected final Filter filter;
	
	public CspSimulator(CspEnvironment simulator){
		this(simulator, new DefaultFilter());
	}
	
	public CspSimulator(CspEnvironment simulator, Filter filter) {
		this.environment = simulator;
		this.environment.registerListener(this);
		this.filter = filter;
	}

	public final void eventsChanged(Collection<CspEvent> events) {
		if(events == null)
			throw new NullPointerException("events");
	
		internalEventChanged(filter.filter(events));
	}
	
	abstract void internalEventChanged(Collection<CspEvent> events);

	public final void run() {
		run(null);
	}

	public void run(String name) {
		environment.reset();
		environment.start(name);
		
		// terminate if environment terminates
		boolean running = true;
		while(running){
			
			synchronized(environment){
				internalRun();
				running = environment.running();
			}
			
		}
		logger.log(Level.INFO,"\n ####################### terminating simulation ####################### \n");
	}
	
	protected abstract void internalRun();

}
