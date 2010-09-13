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

public class ProcessInternalChoice extends CspProcess {

	final Collection<String> delegateNames;
	
	final UserCspEvent choice = new UserCspEvent(this,new UserCspEvent("$resolve_"+name,this){

		@Override
		void doEnd() {
			assert chosen != null;
			events.addAll(environment.getProcess(chosen).events());
		}
		
		@Override
		void performUserDefinedFunction() {
			chosen = environment.getCspEventExecutor().resolve(ProcessInternalChoice.this);
		}

		@Override
		void doStart() {
			events.clear();
		}
		
	});
	String chosen;
	
	ProcessInternalChoice(String name, Collection<String> delegateNames, CspEnvironment simulator) {
		super(name, simulator);
		this.delegateNames = delegateNames;
		events.add(choice);
	}

//	public ProcessInternalChoice(String string, String string2, String string3,
//			CspSimulator simulator) {
//		this(string, Arrays.asList(new String[]{string2,string3}),simulator);
//	}
//
	@Override
	void doStart(CspEvent cspEvent) {
	}
	
	@Override
	void doEnd(CspEvent cspEvent){
	}
	
	public Collection<String> getDelegateNames() {
		return delegateNames;
	}

	@Override
	void doTick(TickEvent tickEvent) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return "ICHOICE("+getName()+")";
	}

}