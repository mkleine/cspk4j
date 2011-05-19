package org.cspk4j.wfsrv;

import org.cspk4j.CspProcess;

public class DeactivateDefinition implements EventHandler {

	final WorkflowDefinitions workflowDefinitions;
	final SelectDefinition selectDefinition;
	
	public DeactivateDefinition(WorkflowDefinitions workflowDefinitions, SelectDefinition selectDefinition) {
		this.workflowDefinitions = workflowDefinitions;
		this.selectDefinition = selectDefinition;
	}

	void deactivateDefinition() {
		final WorkflowDefinition workflowDefinition = selectDefinition.getWorkflowDefinition();
		assert workflowDefinition != null : "definition missing!";
		workflowDefinitions.deactivateDefinition(workflowDefinition);
	}

	public void handle(CspProcess process) {
		deactivateDefinition();
	}

}
