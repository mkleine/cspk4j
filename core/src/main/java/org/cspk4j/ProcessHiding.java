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

public class ProcessHiding extends CspProcess {

	final private String toHide;
	final private Collection<String> alphabet;

	CspProcess delegate;
	
	ProcessHiding(String name, String toHide, Collection<String> alphabet, CspEnvironment simulator) {
		super(name, simulator);
		this.toHide = toHide;
		this.alphabet = alphabet;
	}

	@Override
	public void doStart(CspEvent cspEvent) {
		if(cspEvent.isHidden() && cspEvent.first() == this)
			delegate = cspEvent.hidden().last();
		else
			delegate = cspEvent.offeredBy(this, -1);
		computeEvents(delegate);
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
		computeEvents(delegate);
	}


	@Override
	void doTick(TickEvent tickEvent) {
		if(!tickEvent.isHidden()){
			events.clear();
			logger.log(Level.INFO,this+ " terminated");
		} else {
			doStart(tickEvent);
		}
		//events.addAll(tickEvent.first().events());
	}
	
	@Override
	void computeInitials() {
		computeEvents(simulator.getProcess(toHide));
	}

	void computeEvents(CspProcess source) {
		events.clear();
		Collection<CspEvent> delegateEvents = source.events();
		events.addAll(delegateEvents);
		
		// hide events
		for(CspEvent e : delegateEvents){
			if(alphabet.contains(e.name)){
				events.remove(e);
				if(e instanceof UserCspEvent){
					events.add(new UserCspEvent(this, (UserCspEvent) e));
				} else {
					throw new IllegalStateException();
				}
				// TODO hide tick events?
			} else {
				e.offeredBy(this);
			}
		}
	}

	@Override
	public String toString() {
		return "HIDE("+getName()+")";
	}

}
