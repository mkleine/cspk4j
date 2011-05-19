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
import org.cspk4j.CspEnvironment;
import org.cspk4j.simulator.ConsoleCspSimulator;
import org.cspk4j.simulator.SwingCspSimulator;


public class BufferExample {
	
	final CspProcessStore store = new CspProcessStore();
	
	public BufferExample() {
		store.createPrefix("InputA","left.apples","SKIP");
		store.createPrefix("InputO","left.oranges","SKIP");
		store.createPrefix("InputP","left.pears","SKIP");
		store.createPrefix("OutputA","right.apples","SKIP");
		store.createPrefix("OutputO","right.oranges","SKIP");
		store.createPrefix("OutputP","right.pears","SKIP");
		store.createSequential("CopyA","InputA","CopyA'");
		store.createSequential("CopyO","InputO","CopyO'");
		store.createSequential("CopyP","InputP","CopyP'");
		store.createSequential("CopyA'","OutputA","COPY");
		store.createSequential("CopyO'","OutputO","COPY");
		store.createSequential("CopyP'","OutputP","COPY");
		store.createExternalChoice("COPY",Arrays.asList(new String[]{"CopyA","CopyO","CopyP"}));
		store.createPrefix("MidA","mid.apples","SKIP");
		store.createPrefix("MidO","mid.oranges","SKIP");
		store.createPrefix("MidP","mid.pears","SKIP");
		store.createPrefix("AckSend","ack","SEND");
		store.createSequential("SendA'","MidA","AckSend");
		store.createSequential("SendO'","MidO","AckSend");
		store.createSequential("SendP'","MidP","AckSend");
		store.createSequential("SendA","InputA","SendA'");
		store.createSequential("SendO","InputO","SendO'");
		store.createSequential("SendP","InputP","SendP'");
		store.createExternalChoice("SEND",Arrays.asList(new String[]{"SendA","SendO","SendP"}));
		store.createPrefix("AckRec","ack","REC");
		store.createSequential("RecA'","OutputA","AckRec");
		store.createSequential("RecO'","OutputO","AckRec");
		store.createSequential("RecP'","OutputP","AckRec");
		store.createSequential("RecA","MidA","RecA'");
		store.createSequential("RecO","MidO","RecO'");
		store.createSequential("RecP","MidP","RecP'");
		store.createExternalChoice("REC",Arrays.asList(new String[]{"RecA","RecO","RecP"}));
		store.createParallel("SendAndReceive","SEND","REC",Arrays.asList(new String[]{"mid.apples", "mid.oranges", "mid.pears", "ack"}));
		store.createHiding("SYSTEM","SendAndReceive",Arrays.asList(new String[]{"mid.apples","mid.oranges", "mid.pears", "ack"}));
	}
	
	public CspProcessStore getStore() {
		return store;
	}
	
	void run() {
		final CspEnvironment cspSimulator = new CspEnvironment(store, new ExampleEventExecutor());
		new SwingCspSimulator("buffer example",cspSimulator).run();
		cspSimulator.reset();
		new ConsoleCspSimulator(cspSimulator).run();
	}
	
	public static void main(String[] args){
		new BufferExample().run();
	}

}
