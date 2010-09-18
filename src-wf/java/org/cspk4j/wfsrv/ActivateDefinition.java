package org.cspk4j.wfsrv;

import java.io.File;

import org.cspk4j.CspProcess;


public class ActivateDefinition implements EventHandler {

	final WorkflowDefinitions workflowDefinitions;
	private WorkflowDefinition workflowDefinition;
	private File jar;

	public ActivateDefinition(WorkflowDefinitions workflowDefinitions) {
		this.workflowDefinitions = workflowDefinitions;
	}

	public void setJarAndDefinition(File file, WorkflowDefinition workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
		this.jar = file;
	}
	
	void activateDefinition() {
		workflowDefinitions.activateDefinition(jar, workflowDefinition);
	}

	public void handle(CspProcess process) {
		activateDefinition();
	}

}
