package org.cspk4j.wfsrv;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.JarFilter;

public class WorkflowDefinitions {
	
	static final Logger logger = Logger.getLogger(WorkflowDefinitions.class.getName());
		
	final List<File> jars = new ArrayList<File>(32);
	final List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>(32);
	final File definitionsDirectory;
	final WorkflowServerSettings settings;

	public WorkflowDefinitions(WorkflowServerSettings workflowServerSettings) {
		definitionsDirectory = workflowServerSettings.definitionsDirectory;
		settings = workflowServerSettings;
	}
	
	public File[] listDefinitionJars() {
		return definitionsDirectory.listFiles(new JarFilter());
	}
	
	public Collection<WorkflowDefinition> getActiveDefinitions() {
		assert jars.size() >= definitions.size() : "too many definitions!";
		return Collections.unmodifiableCollection(definitions);
	}
	
	public void activateDefinition(File jar, WorkflowDefinition workflowDefinition){
		logger.log(Level.INFO,"activating definition "+workflowDefinition+ " for jar "+jar);
		this.definitions.add(workflowDefinition);
		this.jars.add(jar);
	}
	
	public void deactivateDefinition(WorkflowDefinition workflowDefinition){
		logger.log(Level.INFO,"deactivating definition "+workflowDefinition);
		this.definitions.remove(workflowDefinition);
	}

	public Collection<File> getLoadedJars() {
		return Collections.unmodifiableCollection(jars);
	}

	public WorkflowServerSettings getWorkflowServerSettings() {
		return settings;
	}

}
