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
import java.util.HashSet;
import java.util.logging.Level;

public class ProcessInterrupt2 extends CspProcess {

	final String left;
	final String right;
	CspProcess leftDelegate;
	CspProcess rightDelegate;
	final ArrayList<CspEvent> leftEvents = new ArrayList<CspEvent>();
	final ArrayList<CspEvent> rightEvents = new ArrayList<CspEvent>();
	
	final HashSet<CspEvent> performing = new HashSet<CspEvent>();
	
	CspProcess chosen;

	ProcessInterrupt2(String name, String left, String right, CspEnvironment simulator) {
		super(name, simulator);
		this.left = left;
		this.right = right;
	}

	@Override
	void computeInitials() {
		leftDelegate = simulator.getProcess(left);
		storeLeft();
		rightDelegate = simulator.getProcess(right);
		storeRight();
		computeEvents();
	}
	
	void computeEvents() {
		events.clear();
		if(chosen != null)
			events.addAll(chosen.events());
		else {
			// all events available
			ArrayList<CspEvent> delegateEvents = new ArrayList<CspEvent>();
			delegateEvents.addAll(leftDelegate.events());
			delegateEvents.addAll(rightDelegate.events());
			
			// filtered
			for(CspEvent e : delegateEvents){
				e.offeredBy(this);
				events.add(e);
			}
		}
	}
	
	@Override
	public void doStart(final CspEvent cspEvent) {
		boolean isRight = isRight(cspEvent);
		final boolean isTick = cspEvent instanceof TickEvent;
		if(!(isRight || isTick)){
			performing.add(cspEvent);
		}
		
		if(!cspEvent.isHidden() && // event must be visible on both side 
				(isRight || (isTick))) // and either right or a tick
			{
				logger.log(Level.INFO,cspEvent.getName()+" resolves "+this);
				chosen = cspEvent.offeredBy(this,-1);
				cspEvent.delegates.add(new UserCspEvent("$int_"+cspEvent.getName(),this){

					@Override
					void performUserDefinedFunction() {
						synchronized(environment){
							while(!performing.isEmpty())
								try {
									logger.log(Level.INFO,"waiting for events to finish: "+performing);
									environment.wait();
								} catch (InterruptedException e) {
									logger.log(Level.INFO,"interrupted",e);
								}
						}
						logger.log(Level.INFO,"performing interruption handler");
						environment.getCspEventExecutor().execute(cspEvent.first(), cspEvent.getName());
					}
					
				});
			}
		else {
			if(isRight){
				rightDelegate = cspEvent.offeredBy(this, -1);
				storeRight();
			} else {
				leftDelegate = cspEvent.offeredBy(this, -1);
				storeLeft();
			}
		}
		computeEvents();
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
		performing.remove(cspEvent);
		
		storeLeft();
		storeRight();
		computeEvents();
	}
	
	boolean isRight(CspEvent cspEvent){
		final boolean result = rightEvents.contains(cspEvent);
		assert result ^ leftEvents.contains(cspEvent);
		return result;
	}

	void storeRight() {
		rightEvents.clear();
		rightEvents.addAll(rightDelegate.events());
	}

	void storeLeft() {
		leftEvents.clear();
		leftEvents.addAll(leftDelegate.events());
	}

	@Override
	void doTick(TickEvent tickEvent) {
		doStart((CspEvent) tickEvent);
	}

	@Override
	public String toString() {
		return "INT("+getName()+")";
	}

}