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
import java.util.List;
import java.util.Map.Entry;

public class ProcessParallel extends CspProcess {

	final Collection<String> delegateNames;
	final private Collection<String> alphabet;
	final private HashMap<CspProcess,Collection<CspEvent>> initials = new HashMap<CspProcess,Collection<CspEvent>>();
	final private HashMap<String, HashMap<CspProcess,List<CspEvent>>> syncEvents = new HashMap<String, HashMap<CspProcess,List<CspEvent>>>();
	
	private int terminated;
	
	ProcessParallel(String name, Collection<String> delegateNames, Collection<String> alphabet, CspEnvironment simulator) {
		super(name, simulator);
		this.delegateNames = delegateNames;
		this.alphabet = alphabet;
		resetSyncEvents();
	}

//	public ProcessParallel(String string, String string2, String string3,
//			Collection<String> alphabet, CspSimulator simulator) {
//		this(string,Arrays.asList(new String[]{string2,string3}),alphabet, simulator);
//	}
//
	@Override
	public void doStart(CspEvent cspEvent) {
		if(cspEvent.first() == this){
			for(CspEvent e : cspEvent.delegates){
				CspProcess source = null;
				for(Entry<CspProcess, Collection<CspEvent>> entry : initials.entrySet()){
					if(entry.getValue().contains(e)){
						source = entry.getKey();
						break;
					}
				}
				assert source != null;
				initials.remove(source);
				final CspProcess p = e.last();
				storeInitials(p);

			}
		} else {
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

	void storeInitials(final CspProcess p) {
		initials.put(p,new ArrayList<CspEvent>(p.events()));
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
		for(CspProcess p : initials.keySet()){
			storeInitials(p);
		}
		computeEvents();
	}
//
//	@Override
//	CspProcess freshCopy() {
//		return new ProcessParallel(name, delegateNames, alphabet, simulator);
//	}

	@Override
	void doTick(TickEvent tickEvent) {
		if(tickEvent.first() == this)
			terminated ++;
		doStart(tickEvent);
	}
	
	@Override
	void computeInitials() {
		for(String s : delegateNames){
			final CspProcess p = simulator.getProcess(s);
			storeInitials(p);
		}
		computeEvents();
	}
	
	void computeEvents() {
		events.clear();
		if(delegateNames.size() == terminated){
			events.add(new TickEvent(this));
			return; 
		}
		if(delegateNames.size() > terminated){
		
		// all events available
		ArrayList<CspEvent> delegateEvents = new ArrayList<CspEvent>();
		for(CspProcess p : initials.keySet()){
			delegateEvents.addAll(p.events());
		}
		
		// filtered
			for(CspEvent e : delegateEvents){
				
				if(!e.isHidden()){
					// hide tick events
					if(e instanceof TickEvent){
						e = new TickEvent(this, (TickEvent) e);
						events.add(e);
						continue;
					} else if (alphabet.contains(e.name)) {
						toBeSynchronized(e);
						continue;
					}
					// visible non-tick, unsynchronized fall thru
				}
				// hidden falls thru
				if(!e.offeredBy.contains(this))
					e.offeredBy(this);
				events.add(e);
			}
			addSynchronized(events);
		}
	}

	private void addSynchronized(Collection<CspEvent> result) {

		// try to synchronize
		for(HashMap<CspProcess, List<CspEvent>> map : syncEvents.values()){
			if(map.size() == delegateNames.size()) {
				
				// synchronize
				int[] indexes = new int[map.size()];
				while(true){
					boolean moved = false;
					ArrayList<CspEvent> synced = new ArrayList<CspEvent>();
					int i = 0;
					for(Entry<CspProcess, List<CspEvent>> entry : map.entrySet()){
						assert i < map.size();
						final List<CspEvent> list = entry.getValue();
						synced.add(list.get(indexes[i]));
						// move index
						if(!moved){
							if(indexes[i] < list.size() -1){
								indexes[i]++;
								// reset lower indexes
								for(int j = 0 ; j < i; j++){
									if(indexes[j] > 0 )
										indexes[j] = 0;
								}
								moved = true;
							}
						}
						i++;
					}
					assert synced.size() == indexes.length;
					// add to result
					result.add(new UserCspEvent(this,synced));
					if(!moved)
						break;
				}
			}
		}
		
		// clear 
		resetSyncEvents();
	}

	private void toBeSynchronized(CspEvent e) {
		final HashMap<CspProcess, List<CspEvent>> map = syncEvents.get(e.name);
		final CspProcess source = e.last();
		List<CspEvent> events = map.get(source);
		if(events == null){
			events = new ArrayList<CspEvent>();
			map.put(source, events);
		}
		events.add(e);
	}
	
	private void resetSyncEvents(){
		for(String s : alphabet){
			HashMap<CspProcess, List<CspEvent>> map = syncEvents.get(s);
			if(map == null){
				syncEvents.put(s, new HashMap<CspProcess, List<CspEvent>>());
			} else {
				map.clear();
			}
		}
	}
	
	@Override
	public String toString() {
		return "PAR("+getName()+")";
	}


}