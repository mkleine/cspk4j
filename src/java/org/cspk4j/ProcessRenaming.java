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
import java.util.Map;
import java.util.logging.Level;

public class ProcessRenaming extends CspProcess {

	final private String toRename;
	final private Map<String, String> alphabet;

	CspProcess delegate;
	
	ProcessRenaming(String name, String string, Map<String, String> map, CspEnvironment simulator) {
		super(name, simulator);
		this.toRename = string;
		this.alphabet = map;
	}

	@Override
	public void doStart(CspEvent cspEvent) {
		if(cspEvent.first() == this)
			delegate = ((RenamingEvent)cspEvent).renamed().last();
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
	}
	
	@Override
	void computeInitials() {
		computeEvents(simulator.getProcess(toRename));
	}

	void computeEvents(CspProcess source) {
		events.clear();
		Collection<CspEvent> delegateEvents = source.events();
		events.addAll(delegateEvents);
		
		// hide events
		for(CspEvent e : delegateEvents){
			if(alphabet.containsKey(e.name)){
				events.remove(e);
				if(e instanceof UserCspEvent){
					events.add(new RenamingEvent(e));
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
		return "REN("+getName()+")";
	}

	class RenamingEvent extends UserCspEvent {
		
		public RenamingEvent(CspEvent renamed) {
			super(alphabet.get(renamed.name),ProcessRenaming.this);
			delegates.add(renamed);
		}

		@Override
		public boolean isHidden() {
			return false;
		}
		
		CspEvent renamed() {
			return delegates.get(0);
		}
		
	}
}

