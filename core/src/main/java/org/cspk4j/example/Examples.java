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
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import org.antlr.runtime.RecognitionException;
import org.cspk4j.CspProcessStore;
import org.cspk4j.CspEnvironment;
import org.cspk4j.simulator.ConsoleCspSimulator;
import org.cspk4j.simulator.CspSimulator;
import org.cspk4j.simulator.SwingCspSimulator;
import org.cspk4j.util.ConsoleUtils;


public class Examples {

	final HashMap<String,String> examples = new HashMap<String,String>();

	public Examples() {
		
		examples.put("div-invalid", "--invalid\nA = A");
		examples.put("div-terminate", "--valid\nA = a->ICHOICE\nICHOICE = A |~| C\nC = c -> SKIP\nD = ICHOICE\\{a}");

		examples.put("echoice1", "A = a -> D\nB = b -> D\nC = c -> SKIP\nD=[]i:{A,B,C}@i");
		
		examples.put("echoice2", "A = a -> SKIP\nB = b -> SKIP\nC = A ||| B\nD = d -> SKIP\nE = C [] D");
		examples.put("parallel-sequential", "A = a -> SKIP\nB = b -> SKIP\nC = A ||| B\nD = C;C\nE=D;C");
		examples.put("echoice2s", "A = a -> SKIP\nB = b -> SKIP\nC = A ||| B\nD = d -> SKIP\nE = C [] D\n" +
		"F = E;E");

		examples.put("echoice2rh", "A = a -> SKIP\nB = b -> SKIP\nC = A ||| B\nD = d -> SKIP\nE = C [] D\n" +
		"F = E;E\nG = F\\{a,b,d}");
		
		examples.put("echoice3","B = a -> STOP\nA = SKIP [] B");
		examples.put("echoice4", "A = a -> SKIP\nB = STOP [> A\nC = b -> SKIP\nD=B[]C");
		
		examples.put("hiding0",
				"A' = a ->SKIP\n"
				+"A = a -> A'\n"
				+"B = A\\{a}");
		examples.put("hiding-deadlock",
				"A' = a ->STOP\n"
				+"A = a -> A'\n"
				+"B = A\\{a}");
		examples.put("hiding-complex",
				"A' = a -> C\n"
				+"B' = b -> SKIP\n"
				+"A = a -> A'\n"
				+"B = b -> B'\n"
				+"C = c -> SKIP\n"
				+"D = A ||| B\n"
				+"E = D [|{c}|] C\n"
				+"F = E\\{a}");
		examples.put("hiding-complex-deadlock",
				"A' = a ->SKIP\n"
				+"B' = b -> SKIP\n"
				+"A = a -> A'\n"
				+"B = b -> B'\n"
				+"C = c -> STOP\n"
				+"D = A ||| B\n"
				+"E = D ||| C\n"
				+"F = E\\{a}");
		
		
		examples.put("interleaving0", "A = SKIP ||| SKIP\n");
		examples.put("interleaving1", "A' = a ->SKIP\n"
				+"B' = b -> SKIP\n"
				+"A = a -> A'\n"
				+"B = b -> B'\n"
				+"C = A ||| B\n");
		examples.put("interleaving2",
				"A = a -> SKIP\n"
				+"B = b -> SKIP\n"
				+"C = A ||| B\n"
				+"D = d -> SKIP\n"
				+"E = C ; D");
		examples.put("interleaving3", "A' = a ->SKIP\n"
				+"B' = b -> SKIP\n"
				+"A = A'\\{a}\n"
				+"B = B'\\{b}\n"
				+"C = A ||| B\n");	

		examples.put("ichoice0", "A = a -> SKIP\nB = b -> ICHOICE\nICHOICE= B |~|A");
		examples.put("ichoice1", "ICHOICE= ICHOICE |~|SKIP");
		examples.put("ichoice-deadlock0", "ICHOICE= ICHOICE |~|STOP");
		
		examples.put("interrupt0", "A = a -> SKIP\nB = b->SKIP\n"
				+ "C = A /\\ B");
		
		examples.put("interrupt", "A = a -> A'\nA' = ab -> SKIP\nB = b->C\n"
				+ "C = c-> SKIP\nD= B\\{b}\nE= A /\\ D");
		
		examples.put("loader", "PollAndLoad = poll -> PollAndLoad'\n"+
				"PollAndLoad' = NewDef |~| Loader\n"+
				"NewDef = loadCSP -> CheckDef\n"+
				"CheckDef = checkCSP -> CheckDef'\n"+
				"CheckDef' = ReportError |~| LoadClasses\n"+
				"LoadClasses = loadClasses -> LoadClasses'\n"+
				"LoadClasses' = ReportError |~| DefLoaded\n"+
				"ReportError = reportError -> Loader\n"+
				"DefLoaded = activateDef -> Loader\n"+
		"Loader = STOP [> PollAndLoad\n");
		
		examples.put("parallel0",
				"A = a ->B\n"
				+"B = b -> C\n"
				+"C = c->SKIP\n"
				+"D = d -> E\n"
				+"E = b -> SKIP\n"				
				+"X = A [|{b}|] D\n");
		
		examples.put("prefix0", "channel a\nA = a -> SKIP");
		examples.put("prefix1", "channel a\nA = a -> A");
		examples.put("prefix2", "channel a,b\nB = b -> SKIP\nA = a -> B\n");
		
		examples.put("renaming-terminate", "channel a,b\nA=SKIP[[a <- b]]");
		examples.put("renaming-deadlock", "channel a,B\nA=STOP[[a <- b]]");
		
		examples.put("renaming0", "channel a,b\nA = a -> A\n\nB=A[[a <- b]]");
		examples.put("renaming1", "channel a,b\nA = a -> SKIP\n\nB=A[[a <- b]]");

		examples.put("renaming2", "A = a -> B\nB = b -> SKIP\nC=A[[a <- c]]");
		examples.put("renaming3",
				"A = a -> SKIP\n"
				+"B = b -> SKIP\n"
				+"C = A [] B\n"
				+"D = C[[a <- c, b <- c]]");

		examples.put("renaming4",
				"A = a -> SKIP\n"
				+"B = b -> B\n"
				+"C = A ||| B\n"
				+"D = C[[a <- c, b <- c]]");
		examples.put("renaming5", "A' = a ->SKIP\n"
				+"A = A'\\{a}\n"
				+"B = A[[c <- d]]\n");

		examples.put("semaphore", "semaphore");
		examples.put("semaphoreh", "semaphoreh");
		
		examples.put("sequential0", "channel a,b\nA = a -> SKIP\nB = SKIP;A\n");
		examples.put("sequential1", "channel a,b\nA = a -> SKIP\nB = A;SKIP\n");
		examples.put("sequential2", "channel a,b\nA = a -> SKIP\nB = b -> SKIP\nC=A;B");
		examples.put("sequential3","channel a,b\nA = a->SKIP\nB = b->SKIP\nC=A;B\nD=C;D\n");
		
		examples.put("sequential-infinite", "channel a,b\nA = a -> B\nB = b -> C\nC=A;B");
		examples.put("sequential-deadlock1", "channel a\nA = a -> SKIP\nB = STOP;A");
		examples.put("sequential-deadlock2", "channel a\nA = a -> SKIP\nB = A;STOP");
		
		examples.put("skip", "A = SKIP");
		examples.put("stop", "A = STOP");

		examples.put("timeout","A = a -> A'\nA' = ab -> SKIP\nB = b->C\n"
				+ "C = c-> SKIP\nD= B\\{b}\nE= A [> D");
		examples.put("timeout1","A = a -> A'\nA' = ab -> SKIP\nB = b->C\n"
				+ "C = c-> SKIP\nD= d->C\nE= A [> D");

		examples.put("timeout2", "A = A'; A''\n"
				+"A'' = a -> SKIP\n"
				+"A' = A''\\{a}\n"
				+"B = b->C\n"
				+"C = c-> SKIP\n"
				+"D= B\\{b}\n"
				+"E= A [> D\n");
		examples.put("timeout3","A = a -> SKIP\nB=STOP [> A");

		examples.put("timeout4", "A = a -> SKIP\nB = STOP [> A");
		examples.put("timeout5", "A = a -> B\nB = STOP [> A");

	}
	
	public Example chooseExample() throws RecognitionException {
		String example = Examples.selectValue(examples.keySet());
		return getExample(example);
	}

	public Example getExample(String example) throws RecognitionException {
		final String source = examples.get(example);
		final CspProcessStore store = CspProcessStore.parseSource(source);
		return new Example(example,source,store);
	}
	
	/**
	 * Offers the user the sorted collection of keys to chose one among them.
	 * @param keys
	 * @return the chosen value or null if none.
	 */
	static String selectValue(Collection<String> keys){
		// TODO Leere Collection verbieten?
		assert keys != null : "keys must not be null";
		if(keys.isEmpty()) ConsoleUtils.logger.log(Level.WARNING,"nothing to select from, will deadlock!");
		System.out.println("choose one of "+ConsoleUtils.sorted(keys));
	
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
					System.out.println("retry: "+ConsoleUtils.sorted(keys)+" (given: "+name+")");
			} catch (IOException e) {
				ConsoleUtils.logger.log(Level.SEVERE,"error selecting value",e);
				return null;
			}	
		}
		assert name != null : "name should be initialized!";
		return name;
	}

	public static final class Example {
		final String name;
		final String source;
		public final CspProcessStore store;		
		
		public Example(String name,
				String source,
				CspProcessStore store) {
			this.name = name;
			this.source = source;
			this.store = store;
		}
		
		@Override
		public String toString() {
			return "name: "+name+"\nsource:"+source+"\nprocess: "+store.getLastName();
		}

		public void run() {
			CspEnvironment simulator = new CspEnvironment(store, new ExampleEventExecutor());
			final CspSimulator sim = new ConsoleCspSimulator(simulator);
			sim.run();
		}
		
	}
	
}
