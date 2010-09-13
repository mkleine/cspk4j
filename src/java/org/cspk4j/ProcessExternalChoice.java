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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public class ProcessExternalChoice extends CspProcess {

	final Collection<String> delegateNames;
	final private HashMap<CspProcess,Collection<CspEvent>> initials = new HashMap<CspProcess,Collection<CspEvent>>();

	CspProcess chosen;
	
	ProcessExternalChoice(String name, Collection<String> delegateNames, CspEnvironment simulator) {
		super(name, simulator);
		this.delegateNames = delegateNames;
	}

//	public ProcessExternalChoice(String string, String string2, String string3,
//			CspSimulator simulator) {
//		this(string,Arrays.asList(new String[]{string2,string3}), simulator);
//	}

	@Override
	void computeInitials() {
		for(String s : delegateNames){
			final CspProcess p = simulator.getProcess(s);
			storeInitials(p);
		}
		computeEvents();
	}
	
	void storeInitials(final CspProcess p) {
		initials.put(p,new ArrayList<CspEvent>(p.events()));
	}
	
	void computeEvents() {
		events.clear();
		if(chosen != null)
			events.addAll(chosen.events());
		else {
			// all events available
			ArrayList<CspEvent> delegateEvents = new ArrayList<CspEvent>();
			for(CspProcess p : initials.keySet()){
				delegateEvents.addAll(p.events());
			}
			
			// filtered
			for(CspEvent e : delegateEvents){
				e.offeredBy(this);
				events.add(e);
			}
		}
	}

	@Override
	public void doStart(CspEvent cspEvent) {
		if(!cspEvent.isHidden())
			chosen = cspEvent.offeredBy(this,-1);
		else {
			CspProcess source = null;
			for(Entry<CspProcess, Collection<CspEvent>> entry : initials.entrySet()){
				if(entry.getValue().contains(cspEvent)){
					source = entry.getKey();
					break;
				}
			}
			assert source != null;
			initials.remove(source);
			final CspProcess p = cspEvent.offeredBy(this, -1);
			storeInitials(p);
		}
		computeEvents();
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
		for(CspProcess p : initials.keySet()){
			storeInitials(p);
		}
		computeEvents();
	}

	@Override
	void doTick(TickEvent tickEvent) {
		doStart((CspEvent) tickEvent);
	}

	@Override
	public String toString() {
		return "ECHOICE("+getName()+")";
	}

}