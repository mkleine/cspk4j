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

public class ProcessPrefix extends CspProcess {

	final String successorName;
	final String eventName;
	
	ProcessPrefix(String name, String eventName, String successorName, CspEnvironment simulator) {
		super(name, simulator);
		this.successorName = successorName;
		this.eventName = eventName;
		events.add(new UserCspEvent(eventName,this));
	}

	@Override
	void doStart(CspEvent cspEvent) {
		events.clear();
	}

	void doEnd(CspEvent cspEvent) {
		events.addAll(simulator.getProcess(successorName).events());
	}

	@Override
	void doTick(TickEvent tickEvent) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "PREF("+getName()+")";
	}

}