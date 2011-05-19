package org.cspk4j.wfsrv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.RecognitionException;
import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEventExecutor;
import org.cspk4j.CspProcess;
import org.cspk4j.CspProcessStore;
import org.cspk4j.Disposable;
import org.cspk4j.simulator.CspSimulator;


public class LoadDefinitions {

	static final Logger logger = Logger.getLogger(LoadDefinitions.class.getName());

	final WorkflowDefinitions definitions;	
	final ActivateDefinition activateDefinition;

	// 1: poll
	List<File> jarsToLoad;
	int index = -1;

	// 2: loadCSP
	CheckJar checkJar;
	CheckWorkflowDefinition checker;
	
	Throwable error;

	public LoadDefinitions(WorkflowDefinitions definitions, ActivateDefinition activateDefinition) {
		this.definitions = definitions;
		this.activateDefinition = activateDefinition;
	}

	void resetCurrentJar() {
		checkJar = null;
		if(checker != null)
			checker.abort();
		checker = null;
		error = null;
	}

	void reset() {
		jarsToLoad = null;
		index = -1;
		resetCurrentJar();
	}

	public boolean hasMoreJars() {
		return jarsToLoad != null && index < jarsToLoad.size() -1;
	}

	void poll () {
		reset();
		jarsToLoad = new ArrayList<File>(Arrays.asList(definitions.listDefinitionJars()));
		final Collection<File> loadedJars = definitions.getLoadedJars();
		jarsToLoad.removeAll(loadedJars);
	}

	public void registerHandlers(Map<String, EventHandler> handlers){
		{handlers.put("poll", new EventHandler() {

			public void handle(CspProcess process) {
				poll();
			}

		});}

		{handlers.put("loadCSP", new EventHandler() {

			public void handle(CspProcess process) {
				assert !jarsToLoad.isEmpty() : "no jars to load!";
				try {
					checkJar = new CheckJar(jarsToLoad.get(++index),definitions);
					checker = checkJar.createChecker();
				} catch (Exception e) {
					error = e;
				} 
			}

		});}


		{handlers.put("startCheck", new EventHandler() {

			public void handle(CspProcess process) {
				try {
					checker.startCheck();
				} catch (IOException e) {
					error = e;
				} catch (InterruptedException e) {
					error = e;
				}
			}

		});}
		{handlers.put("checkResult", new EventHandler() {

			public void handle(CspProcess process) {
				try {
					checker.checkResult();
				} catch (Exception e) {
					error = e;
				}
			}

		});}
		{handlers.put("loadClasses", new EventHandler() {

			public void handle(CspProcess process) {
				try {
					activateDefinition.setJarAndDefinition(checkJar.jar, checkJar.createWorkflowDefinition());
				} catch(Exception e) {
					error = e;
				}
			}

		});}
		{handlers.put("reportError", new EventHandler() {

			public void handle(CspProcess process) {
				System.out.println("loading of definition failed:");
				error.printStackTrace();
				resetCurrentJar();
			}

		});}
	}

	public boolean hasError() {
		return error != null;
	}

	public boolean isChecked() {
		return checker != null && checker.checkFinished();
	}

	public boolean isVerified() {
		return checker != null && checker.isVerified();
	}

}

class CheckJar {
	
	static final Logger logger = Logger.getLogger(CheckJar.class.getName());
	
	final File jar;
	final WorkflowDefinitions definitions;
	
	final Properties properties = new Properties();
	final ClassLoader classLoader;
	String scriptName;
	
	final String definitionName;

	CspProcessStore store;

	public CheckJar(File jar, WorkflowDefinitions definitions) throws MalformedURLException {
		this.jar = jar;
		classLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()});
		this.definitions = definitions;
		final String jarName = jar.getName();
		definitionName = jarName.substring(0, jarName.length() - 4);
		logger.log(Level.INFO,"about to check "+jar);
	}
	
	CheckWorkflowDefinition createChecker() throws IOException, RecognitionException {
		final InputStream resource = classLoader.getResourceAsStream("wf.properties");			
		properties.load(resource);
		final String scriptName = properties.getProperty("org.cspk4j.wfsrv.script", "wf.csp");
		store = CspProcessStore.parseInputStream(classLoader.getResourceAsStream(scriptName));
		final InputStream stream = classLoader.getResourceAsStream(scriptName);		
		final File tmp = File.createTempFile(definitionName, "-"+scriptName);
		final FileOutputStream os = new FileOutputStream(tmp);
		while(true) {
			final int available = stream.available();
			if(available > 0) {
			byte[] bytes = new byte[available];
			stream.read(bytes);
			os.write(bytes);
			} else {
				logger.log(Level.INFO,"file dumped: "+tmp);
				break;
			}
		}
		final PrintWriter printWriter = new PrintWriter(os);
		printWriter.write("\n");
		printWriter.write("-- verification section --\n");
		printWriter.write("channel sound_wf_definition_\n");
		printWriter.write("SPEC = sound_wf_definition_ -> SPEC\n");
		printWriter.write("s(P) = P\\Events ; sound_wf_definition_-> s(P)\n");
		final String processName = store.getLastName();
		printWriter.write("assert SPEC [F= s("+processName+")\n");
		printWriter.write("assert s("+processName+") [F= SPEC\n");
		printWriter.flush();
		printWriter.close();
		
		return new CheckWorkflowDefinition(tmp, definitions.settings);
	}
	
	@SuppressWarnings("unchecked")
	public WorkflowDefinition createWorkflowDefinition() throws ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		final String executorClassName = properties.getProperty("org.cspk4j.wfsrv.EventExecutor");
		Class<CspEventExecutor> executorClass;
		executorClass = (Class<CspEventExecutor>) classLoader.loadClass(executorClassName);
		final String chooserClassName = properties.getProperty("org.cspk4j.wfsrv.Simulator");
		Class<CspSimulator> chooserClass;
		chooserClass = (Class<CspSimulator>) classLoader.loadClass(chooserClassName);

		Object o;
		try {
			Constructor<CspEventExecutor> constructor = executorClass.getConstructor(WorkflowServerSettings.class);
			o = constructor.newInstance(definitions.settings);
		} catch (NoSuchMethodException e) {		
			o = executorClass.newInstance();			
		}
		if (o instanceof Disposable)
			((Disposable)o).dispose();
		
		Constructor<CspSimulator> constructor = chooserClass.getConstructor(CspEnvironment.class);
		o = constructor.newInstance(new CspEnvironment(null,null));
		if (o instanceof Disposable)
			((Disposable)o).dispose();
		
		assert store != null;
		return new WorkflowDefinition(definitionName, store, executorClass, chooserClass, definitions); 
	}
	
}
