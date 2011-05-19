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

public class RemoteCspEnvironment extends CspEnvironment {
	
	public RemoteCspEnvironment(CspProcessStore store, CspEventExecutor cspEventExecutor) {
		super(store,cspEventExecutor);
	}
	
	void offerEvents(Collection<CspEvent> events) {
		// TODO die Events an den übergeordneten RemoteProcess schicken
		// TODO verschicken vielleicht über EventChangeListener?
		logger.log(Level.INFO, "offered events changed to "+events);
		super.offerEvents(events);
	}
	
	void clearEvents() {
		// TODO entweder manuell weiterleiten oder über EventChangeListener erledigen lassen
		super.clearEvents();
	}

	public boolean mayPerform(CspEvent event) {
		boolean result = super.mayPerform(event);
		boolean remoteOk = false;
		if(result) {
			// hier ist jetzt schon synchronisier
			// es können also keine weiteren Events ausgeführt werden
			// TODO jetzt beim Remo
		}
		return result && remoteOk;
	}

	void startEvent() {
		// TODO inform Remote process / wait for start signal of remote process?
		super.startEvent();
	}

	void endEvent() {
		// TODO inform Remote process / wait for end signal of remote process?
		super.endEvent();
	}

}
