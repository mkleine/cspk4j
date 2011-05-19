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

public class DefaultCspEventExecutor implements CspEventExecutor {

	public void execute(CspProcess process, String event) {
		System.out.println("performing "+event);
	}

	public String resolve(ProcessInternalChoice internalChoice) {
		return internalChoice.delegateNames.iterator().next();
	}

	public void timedOut(ProcessTimeout timeout) {
		System.out.println(timeout.getName()+" timed out");
	}
	
}