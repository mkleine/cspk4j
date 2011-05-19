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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspProcessStore;
import org.cspk4j.Filter;
import org.cspk4j.filter.AndFilter;
import org.cspk4j.filter.NotFilter;
import org.cspk4j.filter.TauFilter;
import org.cspk4j.filter.TimeoutFilter;
import org.cspk4j.simulator.CspEventConsumer;
import org.cspk4j.simulator.GivenFailuresCspSimulator;
import org.cspk4j.util.ParseUtils;


public class SimpleGivenFailuresExampleRunner {
	public static void main(String[] args) {
		final CspProcessStore store = new CspProcessStore();
		CspEnvironment env = new CspEnvironment(store, new ExampleEventExecutor());
		Filter filter = new AndFilter(Arrays.asList(new Filter[]{new TauFilter(),new NotFilter(new TimeoutFilter())}));
		env.registerListener(new CspEventConsumer(filter));

		ArrayList<String> trace = new ArrayList<String>();
		ArrayList<Collection<String>> refusals = new ArrayList<Collection<String>>();
		final GivenFailuresCspSimulator s = new GivenFailuresCspSimulator(env, trace, refusals,5);
		store.createPrefix("P","a","SKIP");
		trace.addAll(Arrays.asList(new String[]{"a","$tick"}));
		refusals.add(Collections.singleton("$tick"));
		refusals.add(Collections.singleton("a"));
		s.run("P");
		
		store.createPrefix("C","c","SKIP");
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"c","$tick"}));
		refusals.clear();
		refusals.add(Collections.singleton("$tick"));
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		s.run("C");

		store.createPrefix("D","d","D");
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"d","d","d","d"}));
		refusals.clear();
		refusals.add(null);
		refusals.add(null);
		refusals.add(null);
		refusals.add(null);
		s.run("D");
		
		
		store.createPrefix("PREFIX","prefix","P");
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"prefix","a","$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		refusals.add(Arrays.asList(new String[]{"prefix","b","c"}));
		refusals.add(Arrays.asList(new String[]{"a","prefix","b","c"}));
		s.run("PREFIX");
		

		store.createRenaming("R_PREFIX", "PREFIX", ParseUtils.parseMap("prefi	x <- b, a <- b"));
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"b","b","$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"prefix","a","c"}));
		refusals.add(Arrays.asList(new String[]{"prefix","a","c"}));
		refusals.add(Arrays.asList(new String[]{"a","prefix","b","c"}));
		s.run("R_PREFIX");

		if(true) return;

		store.createSequential("SKIP;P","SKIP","P");
		trace.clear();
		refusals.clear();
		trace.addAll(Arrays.asList(new String[]{"a","$tick"}));
		refusals.add(Collections.singleton("$tick"));
		refusals.add(Collections.singleton("a"));
		s.run("SKIP;P");

		store.createSequential("P;P","P","P");
		trace.clear();
		refusals.clear();
		trace.addAll(Arrays.asList(new String[]{"a","a","$tick"}));
		refusals.add(Collections.singleton("$tick"));
		refusals.add(Collections.singleton("$tick"));
		refusals.add(Collections.singleton("a"));
		s.run("P;P");

		store.createHiding("HIDDEN_PREFIX","PREFIX",Collections.<String>emptyList());
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"prefix","a","$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		refusals.add(Arrays.asList(new String[]{"prefix","b","c"}));
		refusals.add(Arrays.asList(new String[]{"a","prefix","b","c"}));
		s.run("HIDDEN_PREFIX");

		store.createHiding("HIDE","C",Arrays.asList(new String[]{"c"}));
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		s.run("HIDE");

		store.createSequential("HIDE;C","HIDE","C");
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"c","$tick"}));
		refusals.clear();
		refusals.add(Collections.singleton("$tick"));
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		s.run("HIDE;C");

		store.createInternalChoice("ICHOICE",Arrays.asList(new String[]{"ICHOICE'","STOP"}));
		store.createInternalChoice("ICHOICE'",Arrays.asList(new String[]{"SKIP","STOP"}));
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		s.run("ICHOICE");

		store.createTimeout("TIMEOUT","HIDE;C","P");
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"c","$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"b","$tick"}));
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		s.run("TIMEOUT");
		trace.clear();
		trace.addAll(Arrays.asList(new String[]{"a","$tick"}));
		refusals.clear();
		refusals.add(Arrays.asList(new String[]{"b","$tick"}));
		refusals.add(Arrays.asList(new String[]{"a","b","c"}));
		s.run("TIMEOUT");
		if(true) return;

		store.createParallel("PAR",Arrays.asList(new String[]{"PREFIX", "PREFIX", "PREFIX"}), Arrays.asList(new String[]{"prefix"}));
		//		s.run("PAR");

		store.createParallel("PAR_HIDDEN",Arrays.asList(new String[]{"HIDE;C", "HIDE;C", "HIDE;C"}), Arrays.asList(new String[]{"c"}));
		//		s.run("PAR_HIDDEN");

		store.createExternalChoice("Q",Arrays.asList(new String[]{"P","P'","SKIP"}));
		store.createPrefix("P'","b","Q");
		//		s.run("P'");

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
		//		s.run("SEQ");

		store.createExternalChoice("EXT",Arrays.asList(new String[]{"SEQ","C"}));
		//		s.run("EXT");

		store.createInterrupt("INTERRUPT","EXT","HIDE;C");
		while(true)
			s.run("INTERRUPT");
	}

}
