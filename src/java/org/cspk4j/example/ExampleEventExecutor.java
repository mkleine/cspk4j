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
package org.cspk4j.example;

import java.util.Iterator;

import org.cspk4j.CspEventExecutor;
import org.cspk4j.CspProcess;
import org.cspk4j.ProcessInternalChoice;
import org.cspk4j.ProcessTimeout;


public final class ExampleEventExecutor implements CspEventExecutor {
	
	int count_ICHOICE;
	
	public synchronized void execute(CspProcess process, String event) {
		System.out.println("#########################################");
		System.out.println("process "+process.getName()+" consumes event "+event);
		System.out.println("#########################################\n");
	}

	public synchronized String resolve(ProcessInternalChoice internalChoice) {
		final Iterator<String> iterator = internalChoice.getDelegateNames().iterator();
		final String leftName = iterator.next();
		if("ICHOICE".equals(internalChoice.getName()) && (count_ICHOICE++) == 10)
			return iterator.next();
		System.out.println("#########################################");
		System.out.println("resolving internal choice on "+internalChoice.getName()+" to "+leftName);
		System.out.println("#########################################\n");	
		return leftName;
	}

	public synchronized void timedOut(ProcessTimeout timeout) {
		System.out.println("#########################################");
		System.out.println("process "+timeout.getName()+" timed out");
		System.out.println("#########################################\n");		
	}
	
}