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
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CspEvent implements Comparable<CspEvent> {
	
	final Logger logger = Logger.getLogger(getClass().getName());
	
	final ArrayList<CspProcess> offeredBy = new ArrayList<CspProcess>();
	final String name;
	final CspEnvironment environment;
	final ArrayList<CspEvent> delegates = new ArrayList<CspEvent>();
	
	CspEvent(String name, CspProcess process) {
		super();
		this.name = name;
		this.offeredBy.add(process);
		this.environment = process.simulator;
	}
	
	CspEvent(CspProcess process, CspEvent toHide){
		this("$tau["+toHide.name+"]", process);
		this.delegates.add(toHide);
	}
	
	CspEvent(CspProcess process, Collection<CspEvent> delegates){
		this(delegates.iterator().next().name, process);
		this.delegates.addAll(delegates);
	}
	
	public abstract void perform();
	
	CspProcess first() {
		return offeredBy.get(0);
	}
	
	CspProcess last() {
		return offeredBy.get(offeredBy.size() -1);
	}
	
	void offeredBy(CspProcess process){
		if(!offeredBy.contains(process))
			offeredBy.add(process);
		else
			logger.log(Level.WARNING,this+" already offered by "+process);
	}

	public int compareTo(CspEvent o) {
		return name.compareTo(o.name);
	}

	public String getName() {
		return name;
	}

	CspProcess offeredBy(CspProcess process, int i) {
		int index = offeredBy.indexOf(process);
		return offeredBy.get(index + i);
	}
	
	public boolean isHidden() {
		return delegates.size() == 1;
	}
	
	CspEvent hidden() {
		assert isHidden();
		return delegates.get(0);
	}
	
	boolean isSynchronized() {
		return delegates.size() > 1;
	}
	
	public final boolean isTick(){
		return this instanceof TickEvent;
	}

	@Override
	public String toString() {
		return getName()+"/"+offeredBy;
	}
	
}

class TickEvent extends CspEvent {
	
	public TickEvent(CspProcess process) {
		super("$tick", process);
	}
	
	public TickEvent(CspProcess process, TickEvent event){
		super(process,event);
	}

	@Override
	public
	final void perform() {
		synchronized(environment){
			if(environment.mayPerform(this)){
				environment.clearEvents();
				doTick();
				environment.offerEvents(last().events());
			} else {
				logger.log(Level.WARNING,this +" is not available anymore");
				return;
			}
		}
	}

	void doTick() {
		assert ! isSynchronized();
		for (CspEvent e : delegates) {
			((TickEvent)e).doTick();
		}
		for(CspProcess p : offeredBy){
			p.doTick(this);
		}
	}

}

class UserCspEvent extends CspEvent {
	
	public UserCspEvent(String name, CspProcess process) {
		super(name, process);
	}
	
	public UserCspEvent(CspProcess process, UserCspEvent userCspEvent) {
		super(process, userCspEvent);
	}
	
	public UserCspEvent(CspProcess process, Collection<CspEvent> delegates) {
		super(process,delegates);
	}

	void doStart(){
		for (CspEvent e : delegates) {
			((UserCspEvent)e).doStart();
//			for(CspProcess p : e.offeredBy){
//				p.doStart(e);
//			}
		}
		for(CspProcess p : offeredBy){
			p.doStart(this);
		}
	}

	void doEnd() {
		for (CspEvent e : delegates) {
			((UserCspEvent)e).doEnd();
//			for(CspProcess p : e.offeredBy){
//				p.doEnd(e);
//			}
		}
		for(CspProcess p : offeredBy){
			p.doEnd(this);
		}
	}
	
	void performUserDefinedFunction() {
		if(isHidden())
			((UserCspEvent)hidden()).performUserDefinedFunction();
		else
			environment.getCspEventExecutor().execute(first(), name);
	}

	@Override
	public
	final void perform() {
		// start event
		synchronized(environment){
			if(environment.mayPerform(this)){
				environment.startEvent();
				doStart();
				environment.offerEvents(last().events());
			} else {
				logger.log(Level.WARNING, this +" is not available anymore");
				return;
			}
		}
		
		// end event
		new Thread() {
			@Override
			public void run() {
				performUserDefinedFunction();
				synchronized(environment){
					environment.endEvent();
					doEnd();
					environment.offerEvents(last().events());
				}
				
			};
		}.start();
	}

}