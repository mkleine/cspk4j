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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CspEnvironment {
	
	public static interface Listener {
		void eventsChanged(Collection<CspEvent> events);
	}
	
	final Logger logger = Logger.getLogger(getClass().getName());
	
	final CspProcess STOP = new ProcessSTOP(this);

	public final CspEvent terminateEnvironment = new CspEvent("terminate",STOP) {

		@Override
		public void perform() {
			synchronized(CspEnvironment.this) {
				reset();
				offerEvents(offeredEvents);
			}
		}
		
	};
	
	private final Collection<CspEvent> offeredEvents = new HashSet<CspEvent>();
	final ArrayList<Listener> listeners = new ArrayList<Listener>();

	private final CspProcessStore store;
	private final CspEventExecutor cspEventExecutor;

	private volatile int performing;
	private boolean running;
	
	public CspEnvironment(CspProcessStore store, CspEventExecutor cspEventExecutor) {
		this.store = store;
		this.cspEventExecutor = cspEventExecutor;		
	}
	
	public void registerListener(Listener listener){
		this.listeners.add(listener);
	}
	
	public synchronized boolean running() {
		return running && (offeredEvents.size() > 0 || performing > 0);
	}
	
	public void start(){
		start(null);
	}
	
	public synchronized void start(String name){
		if(listeners.isEmpty())
			throw new IllegalArgumentException("listeners must not be empty");
		
		if(running) throw new IllegalStateException("already running");
		running = true;
		
		if(name == null)
			name = store.getLastName();
		
		logger.log(Level.INFO,"starting process "+name);
		offerEvents(getProcess(name).events());
	}
	
	public Collection<CspEvent> getOfferedEvents(){
		return java.util.Collections.unmodifiableCollection(offeredEvents);
	}
	
	public void reset() {
		offeredEvents.clear();
		running = false;
	}
	
	CspProcess getProcess(String string) {
		return store.get(string, this);
	}

	int getDelay(ProcessTimeout processTimeout) {
		// TODO
		return 4000;
	}

	void offerEvents(Collection<CspEvent> events) {
		logger.log(Level.INFO, "offered events changed to "+events);
		offeredEvents.addAll(events);
		final Collection<CspEvent> result = getOfferedEvents();
		for(Listener listener : listeners){
			listener.eventsChanged(result);
		}
		if (offeredEvents.size() < 1 && performing < 1)
			running = false;
		notifyAll();
	}
	
	void clearEvents() {
		logger.log(Level.INFO, "offered events cleared");
		offeredEvents.clear();
	}

	public boolean mayPerform(CspEvent event) {
		return offeredEvents.contains(event);
	}

	CspEventExecutor getCspEventExecutor() {
		return cspEventExecutor;
	}

	void startEvent() {
		clearEvents();
		performing++;
		logger.log(Level.INFO, "currently performing events: "+performing);
	}

	void endEvent() {
		clearEvents();
		performing--;
		logger.log(Level.INFO, "currently performing events: "+performing);
	}

}
