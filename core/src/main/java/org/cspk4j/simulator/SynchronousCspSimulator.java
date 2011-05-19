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
import java.util.HashSet;
import java.util.logging.Level;

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEvent;

public abstract class SynchronousCspSimulator extends CspSimulator {
	
	protected final Collection<CspEvent> offeredEvents = new HashSet<CspEvent>();

	public SynchronousCspSimulator(CspEnvironment environment) {
		super(environment);
	}

	@Override
	void internalEventChanged(Collection<CspEvent> events) {
		offeredEvents.clear();
		offeredEvents.addAll(events);
	}

	@Override
	protected
	void internalRun() {
		if(environment.running()) {
			CspEvent cspEvent = null; 
			// wait for events
			while(offeredEvents.isEmpty()) {
				try {
					logger.log(Level.INFO,"\n ####################### waiting for new events ####################### \n");
					environment.wait();
					if(!environment.running())
						return;
					logger.log(Level.INFO,"\n ####################### notified ####################### \n");
				} catch (InterruptedException e) {
					logger.log(Level.WARNING,"interrupted",e);
				}
			}

			cspEvent = chooseEvent();

			if(cspEvent != null)
				cspEvent.perform();
		}		
	}

	protected abstract CspEvent chooseEvent();

}
