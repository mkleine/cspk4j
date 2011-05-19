package org.cspk4j.wfsrv;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.antlr.runtime.RecognitionException;
import org.cspk4j.CspEventExecutor;
import org.cspk4j.CspProcessStore;
import org.cspk4j.CspEnvironment;
import org.cspk4j.simulator.CspSimulator;


public class WorkflowDefinition implements Comparable<WorkflowDefinition> {
	
	final String name;
	final CspProcessStore store;
	final Class<CspEventExecutor> executorClass;
	final Class<CspSimulator> chooserClass;
	final WorkflowDefinitions definitions;
	public WorkflowDefinition(String definitionName, CspProcessStore store, Class<CspEventExecutor> executorClass, Class<CspSimulator> chooserClass, WorkflowDefinitions definitions) {
		this.name = definitionName;
		this.store = store;
		this.executorClass = executorClass;
		this.chooserClass = chooserClass;
		this.definitions = definitions;
	}

	void startNewInstance() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, IOException, RecognitionException, NoSuchMethodException {
		System.out.println("starting new workflow instance '"+name+"' "+store);
		
		CspEventExecutor executor;
		try {
			Constructor<CspEventExecutor> constructor = executorClass.getConstructor(WorkflowServerSettings.class);
			executor = constructor.newInstance(definitions.settings);
		} catch (NoSuchMethodException e) {		
			executor = executorClass.newInstance();			
		}
		
		final CspEnvironment environment = new CspEnvironment(store, executor);

		CspSimulator simulator;
		Constructor<CspSimulator> constructor;
		constructor = chooserClass.getConstructor(CspEnvironment.class);
		simulator = constructor.newInstance(environment);
		new Thread(simulator).start();
	}

	public String getName() {
		return name;
	}

	public int compareTo(WorkflowDefinition o) {
		return name.compareTo(o.name);
	}

}
