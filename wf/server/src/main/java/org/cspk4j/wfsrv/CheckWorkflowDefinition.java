package org.cspk4j.wfsrv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CheckWorkflowDefinition {

	static final Logger logger = Logger.getLogger(CheckWorkflowDefinition.class.getName());

	private Process checkerProcess;
	final File file;
	final WorkflowServerSettings settings;
	
	boolean verified;

	public CheckWorkflowDefinition(File file, WorkflowServerSettings settings) {
		this.file = file;
		this.settings = settings;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws VerificationFailedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, VerificationFailedException {
		WorkflowServerSettings settings = new WorkflowServerSettings();
		File file = new File(args[0]);
		final CheckWorkflowDefinition wfChecker = new CheckWorkflowDefinition(file, settings);
		wfChecker.startCheck();
		synchronized(wfChecker) {
			while(!wfChecker.checkFinished())
				wfChecker.wait(100l);
		}
		wfChecker.checkResult();
	}

	void checkResult() throws IOException, VerificationFailedException {
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(this.checkerProcess.getInputStream());
		while(true) {
			final int available = bufferedInputStream.available();
			if(available > 0) {
				byte[] bytes = new byte[available];
				bufferedInputStream.read(bytes);
				System.out.write(bytes);
				if(new String(bytes).indexOf("false\n") > -1) {
					throw new VerificationFailedException();
				}
			} else {
				break;
			}
		}
		logger.log(Level.INFO,file+" verified");
		verified = true;
	}

	boolean isVerified() {
		return verified;
	}
	
	void startCheck() throws IOException, InterruptedException {
		checkerProcess = Runtime.getRuntime().exec
		(settings.getPathToFDR()+" batch "+file.getAbsolutePath());
	}

	boolean checkFinished(){
		try {
			checkerProcess.exitValue();
			return true;
		} catch(IllegalThreadStateException e) {			
			return false;
		}
	}
	
	void abort() {
		if(checkerProcess != null)
			checkerProcess.destroy();
	}

}
