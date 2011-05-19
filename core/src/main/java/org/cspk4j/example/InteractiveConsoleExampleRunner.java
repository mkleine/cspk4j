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

import org.antlr.runtime.RecognitionException;
import org.cspk4j.example.Examples.Example;


public class InteractiveConsoleExampleRunner {
	
	final Examples examples = new Examples();

	/**
	 * @param args
	 * @throws RecognitionException 
	 */
	public static void main(String[] args)  {
		final InteractiveConsoleExampleRunner runner = new InteractiveConsoleExampleRunner();
		runner.runExample();
	}

	public void runExample() {
		while(true){
			try {
					Example example = examples.chooseExample();
					//example.simulator.setEventChooser(new SwingEventChooser("example event chooser"));
					
					System.out.println("\n##############################################################");
					System.out.println(example);
					System.out.println("##############################################################\n");
					
					example.run();
					
	//				example.simulator.run();
					System.out.println("\n ... example finished.");
	
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

}