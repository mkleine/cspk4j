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

import java.util.Arrays;

import org.cspk4j.CspProcessStore;
import org.cspk4j.CspProcessStoreFactory;
import org.cspk4j.util.ParseUtils;

final class ProcessFactoryImpl extends CspProcessStoreFactory {

	@Override
	public void registerProcesses(CspProcessStore result) {
		result.createPrefix("A","a","SKIP");
		result.createPrefix("B","b","SKIP");
		result.createSequential("C","A","C");
		result.createParallel("D","A","B",java.util.Collections.<String>emptySet());
		result.createParallel("E","C","B",Arrays.asList("a,b".split(",")));
		result.createExternalChoice("F","A","B");
		result.createInternalChoice("G","A","B");
		result.createTimeout("H","C","B");
		result.createInterrupt("I","C","B");
		result.createHiding("J","I",Arrays.asList("b"));
		result.createRenaming("K","A",ParseUtils.parseMap("d <- e, a <- b"));
		result.createSequential("L","K","A");
		result.createAlias("ALIAS_1","L");
		result.createAlias("ALIAS_2","E");
	}
}
