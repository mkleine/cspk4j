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

import java.io.IOException;

import org.antlr.runtime.RecognitionException;
import org.cspk4j.CspProcessStore;
import org.cspk4j.CspEnvironment;
import org.cspk4j.simulator.ConsoleCspSimulator;


public class ScriptExampleRunner {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RecognitionException 
	 */
	public static void main(String[] args) throws IOException, RecognitionException  {
		if(args.length != 1)
			System.out.println("give me a CSPm input file name");
		else {
			String fileName = args[0];
			
			final CspProcessStore store = CspProcessStore.parseFile(fileName);
			CspEnvironment simulator = new CspEnvironment(store, new ExampleEventExecutor());
			final ConsoleCspSimulator sim = new ConsoleCspSimulator(simulator);
			sim.run();
			System.out.println("\n ... example finished.");
		}
	}

}