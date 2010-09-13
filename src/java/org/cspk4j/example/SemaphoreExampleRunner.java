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
import java.util.Collection;
import java.util.logging.Logger;

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEvent;
import org.cspk4j.CspEventExecutor;
import org.cspk4j.CspProcess;
import org.cspk4j.CspProcessStore;
import org.cspk4j.CspProcessStoreFactory;
import org.cspk4j.Filter;
import org.cspk4j.ProcessInternalChoice;
import org.cspk4j.ProcessTimeout;
import org.cspk4j.CspEnvironment.Listener;
import org.cspk4j.filter.OrFilter;
import org.cspk4j.filter.TauFilter;
import org.cspk4j.filter.TickFilter;
import org.cspk4j.simulator.ConsoleCspSimulator;
import org.cspk4j.simulator.CspEventConsumer;
import org.cspk4j.util.ParseUtils;

public class SemaphoreExampleRunner extends CspProcessStoreFactory {

	@Override
	public void registerProcesses(CspProcessStore result) {
		result.createPrefix("q0", "a","SKIP");
		result.createPrefix("claim0_0","claim.0","q1");
		result.createPrefix("claim0_1","claim.1", "SKIP");
		result.createPrefix("claim1_0","claim.0","SKIP");
		result.createPrefix("claim1_1","claim.1", "q1");
		result.createPrefix("release0","release.0","p0");
		result.createPrefix("release1","release.1","p1");
		result.createExternalChoice("claim0", Arrays.asList(new String[] {"claim0_0","claim0_1"}));
		result.createExternalChoice("claim1", Arrays.asList(new String[] {"claim1_0","claim1_1"}));

		result.createPrefix("q1_end", "write","SKIP");
		result.createPrefix("q1", "read", "q1_end");

		// P(0) = (Q0;claim0);release0
		result.createSequential("p0_start", "q0", "claim0");
		result.createSequential("p0","p0_start","release0");

		// P(1) = (Q0;claim1) ; release1
		result.createSequential("p1_start", "q0", "claim1");
		result.createSequential("p1","p1_start","release1");

		// P_sys = P(0) [|{|claim|}|] P(1)
		result.createParallel("P_sys","p0","p1", Arrays.asList(new String[]{"claim.0", "claim.1"}));

		// E = (read -> E) [] (write-> E)
		result.createExternalChoice("E",Arrays.asList(new String[]{"e_read", "e_write"}));
		result.createPrefix("e_read","read","E");
		result.createPrefix("e_write","write","E");
		result.createParallel("S", "P_sys","E", Arrays.asList(new String[]{"read","write"}));
	}
	
	public CspProcessStore createHidden() {
		final CspProcessStore store = createProcessStore();
		store.createHiding("Hide","S",ParseUtils.parseSet("read,write,a,claim.0,claim.1,release.0,release.1"));
		return store;
	}
	
	public void run(boolean hidden){
		System.out.println(hidden ? "running hidden semaphore example" : "running semaphore example");
		final CspProcessStore store = hidden ? createHidden() : createProcessStore();
		final SemaphoreExampleEventExecutor semaphoreExampleEventExecutor = new SemaphoreExampleEventExecutor();
		final CspEnvironment env = new CspEnvironment(store, semaphoreExampleEventExecutor);
		if(hidden){
			env.registerListener(new CspEventConsumer(new OrFilter(Arrays.asList(new Filter[]{new TauFilter(), new TickFilter()}))));
			env.registerListener(new Listener() {

				public void eventsChanged(Collection<CspEvent> events) {
					if(semaphoreExampleEventExecutor.sharedCounter > 500000){
						env.terminateEnvironment.perform();
						System.out.println("terminated with shared counter = "+semaphoreExampleEventExecutor.sharedCounter);
					}
				}
				
			});
		}
		final ConsoleCspSimulator sim = new ConsoleCspSimulator(env);
		sim.run();
	}
	
	public static void main(String[] args) {
		new SemaphoreExampleRunner().run(args.length > 0);
		System.out.println("\n ... example finished.");
	}

	static class SemaphoreExampleEventExecutor implements CspEventExecutor {

		static final Logger logger = Logger.getLogger(ExampleEventExecutor.class.getName());

		int sharedCounter = 0;
		int tmpCounter = 0;

		SynchronizedInt controlValue = new SynchronizedInt();

		public void execute(CspProcess process, String event) {
			System.out.println("##########        consuming: "+event);
			if("read".equals(event)){
				tmpCounter = sharedCounter;
				checkValues(event);
			} else if ("write".equals(event)){
				sharedCounter = tmpCounter+1;
				controlValue.increment();
				checkValues(event);
			}
		}

		private void checkValues(String event) {
			synchronized(this) {
				System.out.println("##########        executing code for event: "+event+" / shared counter: "+sharedCounter+ " / tmpCounter: "+tmpCounter);
				System.out.flush();
				assert (controlValue.read() == sharedCounter) : "shared counter: "+sharedCounter+" / expected: "+controlValue.read();
				assert (controlValue.read() == tmpCounter || controlValue.read() == (tmpCounter +1)) : "tmp counter: "+tmpCounter+" / expected: "+controlValue.read();
			}
		}

		public String resolve(ProcessInternalChoice internalChoice) {
			throw new UnsupportedOperationException();
		}

		public void timedOut(ProcessTimeout timeout) {
			throw new UnsupportedOperationException();			
		}
	}

	static final class SynchronizedInt {
		int i;
		synchronized int increment(){
			return ++i;
		}
		synchronized int read() {
			return i;
		}
	}


}