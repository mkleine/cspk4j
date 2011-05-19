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
package org.cspk4j;

import java.util.Collection;
import java.util.logging.Level;

public class ProcessSequential extends CspProcess {

	final String first;
	final String second;
	CspProcess delegate;
	
	ProcessSequential(String name, String first, String second, CspEnvironment simulator) {
		super(name, simulator);
		this.first = first;
		this.second = second;
	}
	
	CspProcess delegate(){
		if(delegate == null)
			delegate = simulator.getProcess(first);
		return delegate;
	}
	
	@Override
	void computeInitials() {
		computeEvents();
	}

	@Override
	public void doStart(CspEvent cspEvent) {
		delegate = cspEvent.offeredBy(this, -1);
		computeEvents();
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
		computeEvents();
		consumeSingleTick();
	}

	@Override
	void doTick(TickEvent tickEvent) {
		if(tickEvent.first() == this) {
			if(!tickEvent.delegates.iterator().next().isHidden()) {
				events.clear();
				events.addAll(simulator.getProcess(second).events());
				logger.log(Level.INFO, "sequential composition "+getName()+" resolved to "+second);
			}
		} else {
			doStart(tickEvent);
		}
	}

	void computeEvents() {
		Collection<CspEvent> delegateEvents = delegate().events();

		events.clear();
		events.addAll(delegateEvents);
		
		// find and hide tick events
		for(CspEvent e : delegateEvents){
			if(e instanceof TickEvent){
				events.remove(e);
				events.add(new TickEvent(this,(TickEvent) e));
			} 
			e.offeredBy(this);
		}
	}

	private void consumeSingleTick() {
		// consume (skip) single tick
		if(events.size() == 1){
			CspEvent e = events.iterator().next();
			if(e instanceof TickEvent){
				doTick((TickEvent) e);
			}
		}
	}

	@Override
	public String toString() {
		return "SEQ("+getName()+")";
	}

}