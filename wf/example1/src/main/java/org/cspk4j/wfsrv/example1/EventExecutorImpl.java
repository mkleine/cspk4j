package org.cspk4j.wfsrv.example1;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.cspk4j.CspProcess;
import org.cspk4j.ProcessInternalChoice;
import org.cspk4j.ProcessTimeout;
import org.cspk4j.TracePrintingEventExecutor;
import org.cspk4j.wfsrv.WorkflowServerSettings;

public class EventExecutorImpl extends TracePrintingEventExecutor {

	int x;
	
	public EventExecutorImpl(WorkflowServerSettings settings){		
		super(createFileWriter(settings.getDefinitionsDirectory()));
	}
	
	static PrintWriter createFileWriter(File dir){
		File tmp;
		try {
			tmp = File.createTempFile(EventExecutorImpl.class.getName().replace('.', '_'), "trace",dir);
			return new PrintWriter(tmp);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void execute(CspProcess process, String event) {
		super.execute(process, event);
		System.out.println("executing event "+event+" for process "+process.getName()+" (count: "+(x++)+")");
	}

	public String resolve(ProcessInternalChoice internalChoice) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void timedOut(ProcessTimeout timeout) {
		throw new UnsupportedOperationException("not implemented");
	}

}
