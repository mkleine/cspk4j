package org.cspk4j.wfsrv;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cspk4j.CspEventExecutor;
import org.cspk4j.CspProcess;
import org.cspk4j.ProcessInternalChoice;
import org.cspk4j.ProcessTimeout;


class WorkflowEventExecutor implements CspEventExecutor {
	
	static final Logger logger = Logger.getLogger(WorkflowEventExecutor.class.getName());
	
	static final String events = "poll,loadCSP,startCheck,checkResult,activateDef,loadClasses,reportError,"
		+"printStatsCmd,deactivateDefCmd,selectDef,deactivateDef,startWfCmd,startWf,init,shutdown,reportWfError";
	static final List<String> eventList = Arrays.asList(events.split(","));

	final WorkflowServer workflowServer;
	final HashMap<String,EventHandler> handlers = new HashMap<String,EventHandler>();
	final HashMap<String, InternalChoiceResolver> resolvers = new HashMap<String,InternalChoiceResolver>();
	
	public WorkflowEventExecutor(WorkflowServer workflowServer) {
		this.workflowServer = workflowServer;
		init();
	}

	void init() {
		 
		handlers.put("init", new InitWorkflowHandler(workflowServer));
		handlers.put("shutdown", new EventHandler() {

			public void handle(CspProcess process) {
				System.out.println("shutting down wf server...");
			}
			
		});
		final WorkflowDefinitions workflowDefinitions = workflowServer.workflowDefinitions;
		final ActivateDefinition activateDefinition = new ActivateDefinition(workflowDefinitions);
		handlers.put("activateDef", activateDefinition);
		
		final LoadDefinitions loadDefinitions = new LoadDefinitions(workflowDefinitions,activateDefinition);
		loadDefinitions.registerHandlers(handlers);
		resolvers.put("NewDefs", new InternalChoiceResolver(){

			public String resolve() {
				if(loadDefinitions.hasMoreJars())
					return "NewDef";
				return "Loader";
			}
			
		});
		resolvers.put("NewDef'", new InternalChoiceResolver(){

			public String resolve() {
				if(loadDefinitions.hasError())
					return "ReportError";
				return "CheckDef";
			}
			
		});
		resolvers.put("CheckDef''", new InternalChoiceResolver(){

			public String resolve() {
				if(loadDefinitions.isChecked())
					return "CheckedDef";
				return "CheckDef'''";
			}
			
		});
		resolvers.put("CheckedDef", new InternalChoiceResolver(){

			public String resolve() {
				if(loadDefinitions.isVerified())
					return "LoadClasses";
				return "ReportError";
			}
			
		});
		resolvers.put("LoadClasses'", new InternalChoiceResolver(){

			public String resolve() {
				if(loadDefinitions.hasError())
					return "ReportError";
				return "DefLoaded";
			}
			
		});
		
		final SelectDefinition selectDefinition = new SelectDefinition(workflowServer);
		handlers.put("selectDef", selectDefinition);
		final DeactivateDefinition deactivateDefinition = new DeactivateDefinition(workflowDefinitions,selectDefinition);
		handlers.put("deactivateDef", deactivateDefinition);
		final EventHandler resetSelectDefinition = new EventHandler(){
			public void handle(CspProcess process) {
				selectDefinition.reset();
				assert selectDefinition.getWorkflowDefinition() == null;
			}
		};
		handlers.put("deactivateDefCmd", resetSelectDefinition);
		handlers.put("printStatsCmd", new PrintStatsEventHandler(workflowServer));
		final StartWorkflowEventHandler startWorkflowEventHandler = new StartWorkflowEventHandler(workflowServer, selectDefinition);
		handlers.put("startWfCmd", resetSelectDefinition);
		handlers.put("startWf", startWorkflowEventHandler);
		handlers.put("reportWfError", new EventHandler() {

			public void handle(CspProcess process) {
				System.out.println("loading of definition failed:");
				startWorkflowEventHandler.error.printStackTrace();
				startWorkflowEventHandler.reset();
			}
			
		});
		resolvers.put("StartWf'''''", new InternalChoiceResolver() {

			public String resolve() {
				if(startWorkflowEventHandler.hasError())
					return "StartWf''''''";
				return "SKIP";
			}
		});
		
		//Available = SKIP |~| DeactivateDef'
		resolvers.put("Available", new InternalChoiceResolver(){

			public String resolve() {
				return workflowDefinitions.getActiveDefinitions().size() > 0 ? "DeactivateDef'" : "SKIP";
			}
			
		});
		
		//Available' = SKIP |~| StartWf'
		resolvers.put("Available'", new InternalChoiceResolver(){

			public String resolve() {
				return workflowDefinitions.getActiveDefinitions().size() > 0 ? "StartWf'" : "SKIP";
			}
			
		});

		
		for(String str : eventList){
			assert handlers.containsKey(str) : "no handler defined for event "+str;
		}
	}
	
	public void execute(CspProcess process, String event) {
		final EventHandler eventHandler = handlers.get(event);
		if(eventHandler != null)
			eventHandler.handle(process);
		else
			System.out.println("performing event "+event+" ... (no handler configured)");
	}

	public String resolve(ProcessInternalChoice internalChoice) {
		final String name = internalChoice.getName();
		final InternalChoiceResolver internalChoiceResolver = resolvers.get(name);
		assert internalChoiceResolver != null : "no resolver registered for "+name;
		final String resolve = internalChoiceResolver.resolve();
		logger.log(Level.INFO,"resolving InternalChoice "+internalChoice+" to "+resolve);
		return resolve;
	}

	public void timedOut(ProcessTimeout timeout) {
		logger.log(Level.WARNING,"TODO: "+timeout.getName());
	}
	
}


class InitWorkflowHandler implements EventHandler {

	final WorkflowServer workflowServer;

	public InitWorkflowHandler(WorkflowServer workflowServer) {
		super();
		this.workflowServer = workflowServer;
	}

	public void handle(CspProcess process) {
		workflowServer.init();
	}

}

class DefaultEventHandler implements EventHandler {

	public void handle(CspProcess process) {
		System.out.println("nothing to do for process "+process);
	}

}

class PrintStatsEventHandler implements EventHandler {

	final WorkflowServer workflowServer;

	public PrintStatsEventHandler(WorkflowServer workflowServer2) {
		this.workflowServer = workflowServer2;
	}

	public void handle(CspProcess process) {
		System.out.println("current stats: ");
		System.out.println("loaded workflow jars: "+ workflowServer.workflowDefinitions.listDefinitionJars().length);
		System.out.println("active workflow definitions: "+ workflowServer.workflowDefinitions.definitions.size());
	}

}

class StartWorkflowEventHandler implements EventHandler {

	final WorkflowServer workflowServer;
	final SelectDefinition selectDefinition;
	Exception error;

	public StartWorkflowEventHandler(WorkflowServer workflowServer, SelectDefinition selectDefinition) {
		this.workflowServer = workflowServer;
		this.selectDefinition = selectDefinition;
	}
	public boolean hasError() {
		return error != null;
	}
	public void reset() {
		error = null;
	}
	public void handle(CspProcess process) {
		final WorkflowDefinition workflowDefinition = selectDefinition.getWorkflowDefinition();
		try {
			workflowDefinition.startNewInstance();
		} catch (Exception e) {
			error = e;
		}
	}

}