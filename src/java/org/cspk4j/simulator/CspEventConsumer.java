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

import java.util.Collection;

import org.cspk4j.CspEvent;
import org.cspk4j.Filter;
import org.cspk4j.CspEnvironment.Listener;

public final class CspEventConsumer implements Listener {
	
	final Filter filter;
	
	public CspEventConsumer(Filter filter) {
		if(filter == null) throw new NullPointerException("filter");
		this.filter = filter;
	}

	public void eventsChanged(Collection<CspEvent> events) {
		final Collection<CspEvent> filtered = filter.filter(events);
		if(!filtered.isEmpty()) {
			CspEvent e = filtered.iterator().next();
			e.perform();
		}
	}

}
