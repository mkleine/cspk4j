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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.Timer;

public class ProcessTimeout extends CspProcess {

	public static final String TIMEOUT_EVENT_PREFIX = "$timeout_";
	final String left;
	final String right;
	final CspEvent timeout;
	
	Timer timer;
	
	ProcessTimeout(String name, String left, final String right, CspEnvironment simulator) {
		super(name, simulator);
		this.left = left;
		this.right = right;
		timeout = new UserCspEvent(this,new UserCspEvent(TIMEOUT_EVENT_PREFIX+name,this){

			@Override
			void doEnd() {
				events.clear();
				events.addAll(environment.getProcess(right).events());
			}

			@Override
			void performUserDefinedFunction() {
				environment.getCspEventExecutor().timedOut(ProcessTimeout.this);
			}

			@Override
			void doStart() {
				logger.log(Level.INFO, "process '"+name+"' timed out");
				events.clear();
				cancelTimer();
			}
			
		});
	}
	
	@Override
	public void doStart(CspEvent cspEvent) {
		// ignore timeout
		if(cspEvent != timeout){
			computeEvents(cspEvent.offeredBy(this, -1));
		}
		if(!cspEvent.isHidden()){
			cancelTimer();
		} else {
			if(timer.isRunning())
				events.add(timeout);
		}
	}

	private void cancelTimer() {
		assert (timer != null);
		timer.stop();
		logger.log(Level.INFO,"timer cancelled");
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
		if(cspEvent != timeout) {
			computeEvents(cspEvent.offeredBy(this, -1));
			if(timer.isRunning())
				events.add(timeout);
		}
	}

	@Override
	void doTick(TickEvent tickEvent) {
		cancelTimer();
		events.clear();
	}
	
	@Override
	void computeInitials() {
		computeEvents(simulator.getProcess(left));
		events.add(timeout);
		scheduleTimerTask();
	}
	
	void computeEvents(CspProcess source){
		events.clear();
		for(CspEvent e  : source.events()){
			if(!e.offeredBy.contains(this))
				e.offeredBy(this);
			events.add(e);
		}
	}

	private void scheduleTimerTask() {
		if(timer == null){
			timer = new Timer(simulator.getDelay(this), new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if(!simulator.running()) {
						cancelTimer();
						return;
					}
					if(timer.isRunning()){
						logger.log(Level.INFO,"performing timeout event");
						timeout.perform();
					} 
				}
				
			});
			timer.start();
		}
	}

	@Override
	public String toString() {
		return "TIMEOUT("+getName()+")";
	}

	
}
