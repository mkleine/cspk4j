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
package org.cspk4j.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cspk4j.CspEvent;

public abstract class ConsoleUtils {

	static final Logger logger = Logger.getLogger(ConsoleUtils.class.getName());

	private ConsoleUtils() {}

	static <T extends Comparable<T>> ArrayList<T> sorted(Collection<T> keys){
		final ArrayList<T> keyList = new ArrayList<T>(keys);
		Collections.sort(keyList);
		return keyList;
	}

	/**
	 * Offers the user the sorted collection of keys to chose one among them.
	 * @param keys
	 * @return the chosen value or null if none.
	 */
	public static String selectValue(Collection<String> keys){
		// TODO Leere Collection verbieten?
		assert keys != null : "keys must not be null";
		if(keys.isEmpty()) logger.log(Level.WARNING,"nothing to select from, will deadlock!");
		System.out.println("choose one of "+sorted(keys));

		String name = null;
		while (true) {
			try {
				byte[] buffer = new byte[128];
				System.in.read(buffer);

				// terminate on ctlr^d
				if(buffer[0] == 0){
					System.out.println("canceling CMD");
					System.exit(2);
				}
				name = new String(buffer).trim();
				if(keys.contains(name))
					break;
				else						
					System.out.println("retry: "+sorted(keys)+" (given: "+name+")");
			} catch (IOException e) {
				logger.log(Level.SEVERE,"error selecting value",e);
				return null;
			}	
		}
		assert name != null : "name should be initialized!";
		return name;
	}

	/**
	 * Offers the user the sorted collection of keys to chose one among them.
	 * @param events
	 * @return the chosen value or null if none.
	 */
	public static CspEvent selectValue(Collection<CspEvent> events){
		assert events != null : "events must not be null";
		assert !events.isEmpty() :"events must not be empty";

		final ArrayList<CspEvent> sorted = sorted(events);
		final ArrayList<String> names = new ArrayList<String>(sorted.size());
		for(CspEvent offeredEvent:sorted){
			names.add(names.size()+": "+offeredEvent.toString());
		}

		CspEvent result = null;
			try {
				System.out.println("choose one of "+names);
				byte[] buffer = new byte[128];
				System.in.read(buffer);

				// terminate on ctlr^d
				if(buffer[0] == 0){
					System.out.println("terminating ...");
					System.exit(2);
				}
				result = sorted.get(Integer.parseInt(new String(buffer).trim()));
			} catch (IOException e) {
				logger.log(Level.SEVERE,"error selecting value",e);
			} catch (NumberFormatException e){
				logger.log(Level.WARNING,"not a number: "+e.getMessage());
			} catch (IndexOutOfBoundsException e){
				logger.log(Level.WARNING,"index out of bounds: "+e.getMessage());
			}
		return result;
	}

}
