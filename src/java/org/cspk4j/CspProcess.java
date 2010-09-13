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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CspProcess {
		
	final Logger logger = Logger.getLogger(getClass().getName());
	
	final CspEnvironment simulator;
	final String name;
	final ArrayList<CspEvent> events = new ArrayList<CspEvent>();
	private boolean initialized;

	public CspProcess(String name, CspEnvironment simulator){
		this.simulator = simulator;
		this.name = name;
//		simulator.register(name, this);
		logger.log(Level.INFO,"created "+this);
	}
	
	final Collection<CspEvent> events(){
		if(!initialized){
			initialized= true;
			computeInitials();
		}
		return java.util.Collections.unmodifiableCollection(events);
	}
	
	void computeInitials() {
		
	}
	
	abstract void doStart(CspEvent cspEvent);
	
	abstract void doEnd(CspEvent cspEvent);
	
	abstract void doTick(TickEvent tickEvent);

//	abstract CspProcess freshCopy();
	final CspProcess freshCopy() {
		return simulator.getProcess(name);
	}
	
	static Map<String,String> parseMap(String str){
		final String[] tokens = str.split("<-");
		Map<String,String> result = new HashMap<String,String>();
		for(int i = 0; i < tokens.length; i++) {
			result.put(tokens[i].trim(), tokens[++i].trim());
		}
		return result;
	}
	
	static Set<String> parseSet(String str){
		final String[] tokens = str.split(",");
		Set<String> result = new HashSet<String>();
		for(int i = 0; i < tokens.length; i++) {
			result.add(tokens[i].trim());
		}
		return result;
	}

	public String getName() {
		return name;
	}
	

}


