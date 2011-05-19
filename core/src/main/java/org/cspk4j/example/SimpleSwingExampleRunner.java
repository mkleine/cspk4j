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

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspProcessStore;
import org.cspk4j.Filter;
import org.cspk4j.filter.AndFilter;
import org.cspk4j.filter.NotFilter;
import org.cspk4j.filter.OrFilter;
import org.cspk4j.filter.TauFilter;
import org.cspk4j.filter.TickFilter;
import org.cspk4j.filter.TimeoutFilter;
import org.cspk4j.simulator.CspEventConsumer;
import org.cspk4j.simulator.SwingCspSimulator;


public class SimpleSwingExampleRunner {
	public static void main(String[] args) {
		final CspProcessStore store = new CspProcessStore();
		CspEnvironment env = new CspEnvironment(store, new ExampleEventExecutor());
		Filter filter = new AndFilter(Arrays.asList(new Filter[]{new TauFilter(),new NotFilter(new TimeoutFilter())}));
//		env.registerListener(new CspEventConsumer(filter));
		env.registerListener(new CspEventConsumer(new OrFilter(Arrays.asList(new Filter[]{new TauFilter(), new TickFilter()}))));
		final SwingCspSimulator s = new SwingCspSimulator("simple example", env);
		SimpleConsoleExampleRunner.run(store, s);
		s.dispose();
		System.out.println("examples finished");
	}

}
