package org.cspk4j.wfsrv;

import java.io.File;

public class WorkflowServerSettings {

	final File definitionsDirectory = new File(System.getProperty("org.cspk4j.wfsrv.dir", "/tmp/wfsrv"));

	final String pathToFDR = System.getProperty("fdr.path", "/usr/bin/fdr2");

	public File getDefinitionsDirectory() {
		return definitionsDirectory;
	}

	public String getPathToFDR() {
		return pathToFDR;
	}

}
