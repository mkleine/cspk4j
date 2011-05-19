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
package org.cspk4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.cspk4j.parser.CSPk4JLexer;
import org.cspk4j.parser.CSPk4JParser;


public final class CspProcessStore {
	
	final Map<String, CspProcessConfig> store = new HashMap<String, CspProcessConfig>();
	
	String lastName;
	
	public CspProcessStore() {
		new STOPConfig();
		new SKIPConfig();
	}
	
	@Override
	public String toString() {
		return "STORE["+lastName+"/"+store+"]";
	}

	public void createAlias(String alias, String ref){
		new AliasConfig(alias, ref);
	}
	
	public void createExternalChoice(String name, Collection<String> delegateNames) {
		new ExternalChoiceConfig(name, delegateNames);
	}

	public void createExternalChoice(String string, String string2, String string3) {
		createExternalChoice(string,Arrays.asList(new String[]{string2,string3}));
	}

	public void createHiding(String name, String toHide, Collection<String> alphabet) {
		new HidingConfig(name, toHide, alphabet);
	}
	
	public void createInternalChoice(String name, Collection<String> delegateNames) {
		new InternalChoiceConfig(name, delegateNames);
	}

	public void createInternalChoice(String string, String string2, String string3) {
		createInternalChoice(string, Arrays.asList(new String[]{string2,string3}));
	}
	
	public void createInterrupt(String name, String left, String right) {
		new InterruptConfig(name, left, right);
	}

	public void createParallel(String name, Collection<String> delegateNames, Collection<String> alphabet) {
		new ParallelConfig(name, delegateNames, alphabet);
	}

	public void createParallel(String string, String string2, String string3, Collection<String> alphabet) {
		createParallel(string,Arrays.asList(new String[]{string2,string3}),alphabet);
	}

	public void createPrefix(String name, String eventName, String successorName) {
		new PrefixConfig(name, eventName, successorName);
	}
	
	public void createRenaming(String name, String string, Map<String, String> map) {
		new RenamingConfig(name, string, map);
	}
	
	public void createSequential(String name, String first, String second){
		new SequentialConfig(name, first, second);
	}
	
	public void createTimeout(String name, String left, String right){
		new TimeoutConfig(name, left, right);
	}
	
	CspProcess get(String name, CspEnvironment simulator) {
		if(!store.containsKey(name))
			throw new RuntimeException("unknown process '"+name+"'");
		return store.get(name).createProcess(simulator);
	}

	abstract class CspProcessConfig {
		
		final String name;
		
		CspProcessConfig(String name){
			this.name = name;
			store.put(name, this);
			lastName = name;
		}
		
		abstract CspProcess createProcess(CspEnvironment simulator);
		
	}
	
	final class STOPConfig extends CspProcessConfig {

		STOPConfig() {
			super("STOP");
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return simulator.STOP;
		}
		
	}

	final class SKIPConfig extends CspProcessConfig {

		SKIPConfig() {
			super("SKIP");
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessSKIP(simulator);
		}
		
	}
	
	class AliasConfig extends CspProcessConfig {
		
		final String ref;

		public AliasConfig(String alias, String ref) {
			super(alias);
			if(alias.equals(ref))
				throw new RuntimeException("illegal divergent process "+name+" = "+ref);
			this.ref = ref;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return get(ref, simulator);
//			return new ProcessAlias(name, ref, simulator);
		}
		
	}
	
	class ExternalChoiceConfig extends CspProcessConfig {

		final Collection<String> delegateNames;

		public ExternalChoiceConfig(String name,
				Collection<String> delegateNames) {
			super(name);
			this.delegateNames = delegateNames;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessExternalChoice(name, delegateNames, simulator);
		}
		
	}
	
	class HidingConfig extends CspProcessConfig {

		final String toHide;
		final Collection<String> alphabet;

		public HidingConfig(String name, String toHide,
				Collection<String> alphabet) {
			super(name);
			this.toHide = toHide;
			this.alphabet = alphabet;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessHiding(name, toHide, alphabet, simulator);
		}
		
	}
	
	class InternalChoiceConfig extends CspProcessConfig {

		private Collection<String> delegateNames;

		public InternalChoiceConfig(String name,
				Collection<String> delegateNames) {
			super(name);
			this.delegateNames = delegateNames;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessInternalChoice(name, delegateNames, simulator);
		}
		
	}
	
	class InterruptConfig extends CspProcessConfig {

		final String left;
		final String right;

		public InterruptConfig(String name, String left, String right) {
			super(name);
			this.left = left;
			this.right = right;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessInterrupt2(name, left, right, simulator);
		}
		
	}
	
	class ParallelConfig extends CspProcessConfig {

		final Collection<String> delegateNames;
		final Collection<String> alphabet;

		public ParallelConfig(String name, Collection<String> delegateNames,
				Collection<String> alphabet) {
			super(name);
			this.delegateNames = delegateNames;
			this.alphabet = alphabet;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessParallel(name, delegateNames, alphabet, simulator);
		}
		
	}
	
	class PrefixConfig extends CspProcessConfig {

		final String successorName;
		final String eventName;

		public PrefixConfig(String name, String eventName, String successorName) {
			super(name);
			this.eventName = eventName;
			this.successorName = successorName;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessPrefix(name, eventName, successorName, simulator);
		}
		
	}
	
	class RenamingConfig extends CspProcessConfig {

		final String string;
		final Map<String, String> map;

		public RenamingConfig(String name, String string,
				Map<String, String> map) {
			super(name);
			this.string = string;
			this.map = map;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessRenaming(name, string, map, simulator);
		}
		
	}
	
	class SequentialConfig extends CspProcessConfig {

		final String first;
		final String second;

		public SequentialConfig(String name, String first, String second) {
			super(name);
			this.first = first;
			this.second = second;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessSequential(name, first, second, simulator);
		}
		
	}
	
	class TimeoutConfig extends CspProcessConfig {

		final String left;
		final String right;

		public TimeoutConfig(String name, String left, String right) {
			super(name);
			this.left = left;
			this.right = right;
		}

		@Override
		CspProcess createProcess(CspEnvironment simulator) {
			return new ProcessTimeout(name, left, right, simulator);
		}
		
	}

	public String getLastName() {
		return lastName;
	}
	
	static public CspProcessStore parseFile(String fileName) throws IOException {
		CharStream input = new ANTLRFileStream(fileName);
		return parse(input);
	}

	static CspProcessStore parse(CharStream input) {
		CSPk4JLexer lex = new CSPk4JLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		CSPk4JParser parser = new CSPk4JParser(tokens);
		try {
			parser.prog();
		} catch (RecognitionException e) {
			throw new RuntimeException("parsing threw recognition exception",e);
		}
		if(parser.failed())
			throw new RuntimeException("parsing failed");
		return parser.getCspProcessStore();
	}
	
	static public CspProcessStore parseSource(String source) {
		CharStream input = new ANTLRStringStream(source);
		return parse(input);
	}
	
	static public CspProcessStore parseInputStream(InputStream stream) throws IOException {
		CharStream input = new ANTLRInputStream(stream);
		return parse(input);
	}

	
}
