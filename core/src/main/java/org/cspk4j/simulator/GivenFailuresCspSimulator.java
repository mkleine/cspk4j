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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEvent;


public class GivenFailuresCspSimulator extends SynchronousCspSimulator {

	private final List<String> trace;
	private final List<Collection<String>> refusals;

	private final int attempts;

	private int index = 0;
	private int curAttempts = 0;

	public GivenFailuresCspSimulator(CspEnvironment environment, List<String> trace, List<Collection<String>> refusals, int attempts) {
		super(environment);
		this.trace = trace;
		this.refusals = refusals;
		this.attempts = attempts;
	}

	@Override
	public void run(String name) {
		index = 0;
		curAttempts = 0;
		super.run(name);
		logger.log(Level.INFO,"trace "+trace+" successfully replayed for process "+name);
	}

	@Override
	protected void internalRun() {
		if(index < trace.size()) {
			super.internalRun();
		}
		else {
			environment.reset();
		}
	}

	@Override
	protected CspEvent chooseEvent() {
		CspEvent result = null;
		String name = trace.get(index);
		Collection<String> refusal = refusals.get(index);
		final Collection<CspEvent> events = new ArrayList<CspEvent>(offeredEvents);
		for(CspEvent e : events){
			// look for expected event
			if(result == null && e.getName().equals(name)){
				result = e;
			}
			// check refusal
			if(refusal != null && refusal.contains(name)){
				environment.terminateEnvironment.perform();
				throw new RuntimeException("event '"+name+"' should be refused after trace "+trace.subList(0, index));
			}
		}
		if(result != null){
			index++;
			curAttempts = 0;
		} else {
			if(curAttempts == attempts){
				environment.terminateEnvironment.perform();
				throw new RuntimeException("didn't find expected '"+name+"' in "+events);
			} else {
				curAttempts++;
				try {
					environment.wait(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		return result;	
	}
	
}
