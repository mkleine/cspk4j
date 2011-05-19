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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ParseUtils {
	
	private ParseUtils(){} // don't create instances
	
	/**
	 * a.1 <- b, a.2 <- b
	 * @param str
	 * @return
	 */
	public static Map<String,String> parseMap(String str){
		final String[] tokens = str.split(",");
		Map<String,String> result = new HashMap<String,String>();
		for(int i = 0; i < tokens.length; i++) {
			final String[] pair = tokens[i].split("<-");
			result.put(pair[0].trim(), pair[1].trim());
		}
		return result;
	}
	
	public static Set<String> parseSet(String str){
		final String[] tokens = str.split(",");
		Set<String> result = new HashSet<String>();
		for(int i = 0; i < tokens.length; i++) {
			result.add(tokens[i].trim());
		}
		return result;
	}

}
