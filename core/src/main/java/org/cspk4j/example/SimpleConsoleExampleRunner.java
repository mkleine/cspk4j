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
import java.util.Collections;

import org.cspk4j.CspProcessStore;
import org.cspk4j.CspEnvironment;
import org.cspk4j.Filter;
import org.cspk4j.filter.AndFilter;
import org.cspk4j.filter.NotFilter;
import org.cspk4j.filter.OrFilter;
import org.cspk4j.filter.TauFilter;
import org.cspk4j.filter.TickFilter;
import org.cspk4j.filter.TimeoutFilter;
import org.cspk4j.simulator.ConsoleCspSimulator;
import org.cspk4j.simulator.CspEventConsumer;
import org.cspk4j.simulator.CspSimulator;
import org.cspk4j.util.ParseUtils;


public class SimpleConsoleExampleRunner {
	public static void main(String[] args) {
		final CspProcessStore store = new CspProcessStore();
		CspEnvironment env = new CspEnvironment(store, new ExampleEventExecutor());
		Filter filter = new AndFilter(Arrays.asList(new Filter[]{new TauFilter(),new NotFilter(new TimeoutFilter())}));
		env.registerListener(new CspEventConsumer(filter));
//		env.registerListener(new CspEventConsumer(new OrFilter(Arrays.asList(new Filter[]{new TauFilter(), new TickFilter()}))));

		final ConsoleCspSimulator s = new ConsoleCspSimulator(env);
		run(store, s);
	}

	static void run(CspProcessStore store, CspSimulator s) {
		
		store.createPrefix("P","a","SKIP");
		store.createPrefix("C","c","SKIP");
		s.run("P");		

		store.createPrefix("PREFIX","prefix","P");
		s.run("PREFIX");
		
		store.createRenaming("Rename", "P", ParseUtils.parseMap("a <- b"));
		s.run("Rename");
		
		store.createSequential("SKIP;P","SKIP","P");
		s.run("SKIP;P");
		
		store.createSequential("P;P","P","P");
		s.run("P;P");
		
		store.createHiding("HIDDEN_PREFIX","PREFIX",Collections.<String>emptyList());
		s.run("HIDDEN_PREFIX");
		
		store.createHiding("HIDE","C",Arrays.asList(new String[]{"c"}));
		s.run("HIDE");
		
		store.createSequential("HIDE;C","HIDE","C");
		s.run("HIDE;C");
		
		store.createInternalChoice("ICHOICE",Arrays.asList(new String[]{"ICHOICE'","STOP"}));
		store.createInternalChoice("ICHOICE'",Arrays.asList(new String[]{"SKIP","STOP"}));
		s.run("ICHOICE");
		
		store.createTimeout("TIMEOUT","HIDE;C","P");
		s.run("TIMEOUT");
		s.run("TIMEOUT");
		
		store.createParallel("PAR",Arrays.asList(new String[]{"PREFIX", "PREFIX", "PREFIX"}), Arrays.asList(new String[]{"prefix"}));
		s.run("PAR");

		store.createParallel("PAR_HIDDEN",Arrays.asList(new String[]{"HIDE;C", "HIDE;C", "HIDE;C"}), Arrays.asList(new String[]{"c"}));
		s.run("PAR_HIDDEN");
		
		store.createExternalChoice("Q",Arrays.asList(new String[]{"P","P'","SKIP"}));
		store.createPrefix("P'","b","Q");
		s.run("P'");
		
		/**
		 * Q = P [] P' [] SKIP
		 * P = a -> SKIP
		 * C = c -> SKIP
		 * P' = b -> Q
		 * SEQ = Q ; ICHOICE
		 * ICHOICE = (SKIP |~| STOP) |~| STOP
		 * EXT = SEQ [] C
		 * HIDE = C \{c}
		 * INTERRUPT = EXT /\ HIDE;C
		 */
		store.createSequential("SEQ","Q","ICHOICE");
		s.run("SEQ");

		store.createExternalChoice("EXT",Arrays.asList(new String[]{"SEQ","C"}));
		s.run("EXT");
		
		store.createInterrupt("INTERRUPT","EXT","HIDE;C");
		while(true)
			s.run("INTERRUPT");
		
	}

}
