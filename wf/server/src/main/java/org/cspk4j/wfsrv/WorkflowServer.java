package org.cspk4j.wfsrv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEventExecutor;
import org.cspk4j.CspProcessStore;
import org.cspk4j.Filter;
import org.cspk4j.filter.AndFilter;
import org.cspk4j.filter.NameFilter;
import org.cspk4j.filter.NotFilter;
import org.cspk4j.filter.TauFilter;
import org.cspk4j.filter.TickFilter;
import org.cspk4j.filter.TimeoutFilter;
import org.cspk4j.simulator.CspEventConsumer;
import org.cspk4j.simulator.SwingCspSimulator;

public class WorkflowServer {

	static final Logger logger = Logger.getLogger(WorkflowServer.class.getName());
	
	final WorkflowDefinitions workflowDefinitions;
	final WorkflowServerSettings settings;

	CspEnvironment env;

	public WorkflowServer(WorkflowDefinitions workflowDefinitions, WorkflowServerSettings workflowServerSettings) {
		if(workflowDefinitions == null) throw new NullPointerException("workflowDefinitions");
		this.workflowDefinitions = workflowDefinitions;
		if(workflowServerSettings == null) throw new NullPointerException("workflowServerSettings");
		this.settings = workflowServerSettings;
	}

	/**
	 * @param args
	 * @throws RecognitionException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		WorkflowServerSettings workflowServerSettings = new WorkflowServerSettings();
		WorkflowDefinitions workflowDefinitions = new WorkflowDefinitions(workflowServerSettings);
		new WorkflowServer(workflowDefinitions, workflowServerSettings).run();
	}

	final void run() throws IOException {
		CspEventExecutor executor = new WorkflowEventExecutor(this);
		CspProcessStore store = CspProcessStore.parseInputStream(WorkflowServer.class.getResourceAsStream("wfserver.csp"));
		env = new CspEnvironment(store,executor);
		Filter nonTimeoutTau = new AndFilter(Arrays.asList(new Filter[]{new TauFilter(),new NotFilter(new TimeoutFilter())}));
		env.registerListener(new CspEventConsumer(nonTimeoutTau));
		env.registerListener(new CspEventConsumer(new TickFilter()));
//		env.registerListener(new CspEventConsumer(filter));
//		env.registerListener(new CspEventConsumer(new OrFilter(Arrays.asList(new Filter[]{new TauFilter(), new TickFilter()}))));

		final SwingCspSimulator s = new SwingCspSimulator("Kleiner Workflow Server", env,new NotFilter(new NameFilter(Arrays.asList("startWf"))));
		
//		CspEventChooser chooser = new TickConsumingEventChooser(new TauPriorityEventChooser(new SwingEventChooser("Kleiner Workflow Server")));
		s.run();
		s.dispose();
		System.out.println("Workflow Server terminated...");
	}

	public Collection<WorkflowDefinition> getDefinitions() {
		return workflowDefinitions.getActiveDefinitions();
	}
	
	public void init() {
		logger.log(Level.INFO, "initializing workflow server ...");
		final File fdr = new File(settings.pathToFDR);
		if(!fdr.exists())
			throw new RuntimeException(fdr+" does not exist!");
		if(!fdr.canExecute())
			throw new RuntimeException(fdr+" is not executable!");
		
		if(!settings.definitionsDirectory.exists()) {
			if(settings.definitionsDirectory.mkdirs()) {
				logger.log(Level.INFO,"definitions dir "+settings.definitionsDirectory+" created.");
			} else
				throw new RuntimeException("failed to initialize "+settings.definitionsDirectory);
		}
		
		logger.log(Level.INFO, "workflow server initialized");
	}


}
