package org.cspk4j.wfsrv;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.cspk4j.CspEvent;
import org.cspk4j.CspProcess;
import org.cspk4j.Disposable;
import org.cspk4j.EventChangeListener;


public class SelectDefinition implements EventHandler {

	static final Logger logger = Logger.getLogger(SelectDefinition.class.getName());

	public class DefinitionChooser implements EventChangeListener, Disposable {

		final Frame frame;
		final JSplitPane pane;
		CspEvent startWfEvent;
		@SuppressWarnings("serial")
		final JButton startWf = new JButton(new AbstractAction("start workflow"){

			public void actionPerformed(ActionEvent e) {
				startWfEvent.perform();
				logger.log(Level.INFO,"selected definition "+workflowDefinition.getName());
				DefinitionChooser.this.dispose();
			}});

		DefinitionChooser() {
			frame = new Frame();
			JDialog dialog = new JDialog(frame,"definition chooser");
			pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			final JPanel panel = new JPanel();
			panel.add(new JLabel("offered definitions"));
			pane.add(panel);

			dialog.add(pane);
			dialog.validate();
			dialog.setMinimumSize(new Dimension(200,300));
			dialog.setVisible(true);
		}

		public void dispose() {
			logger.log(Level.INFO,"disposing frame...");
			frame.dispose();
		}

		@SuppressWarnings("serial")
		void offer(Collection<WorkflowDefinition> definitions) {
			if(pane.getComponentCount() > 2)
				pane.remove(2);

			final JPanel panel = new JPanel();
			pane.add(panel);

			ArrayList<WorkflowDefinition> sorted = new ArrayList<WorkflowDefinition>(definitions);
			java.util.Collections.sort(sorted);
			for(WorkflowDefinition def : sorted) {
				final WorkflowDefinition d = def;
				final JButton button = new JButton(new AbstractAction(d.getName()){
					public void actionPerformed(ActionEvent e) {
						workflowDefinition = d;
						synchronized(this){
							if(startWfEvent != null)
								startWf.setEnabled(true);
						}
//						final SelectDefinition o = SelectDefinition.this;
//						synchronized (o) {
//							o.notify();
//						}
					}

				});
				panel.add(button);
			}
			startWf.setEnabled(false);
			panel.add(startWf);
			panel.updateUI();
		}

		public void eventsChanged(Collection<CspEvent> events) {
			for(CspEvent e : events)
				if("startWf".equals(e.getName())){
					startWfEvent = e;
					synchronized(this){
						if(workflowDefinition != null)
							startWf.setEnabled(true);
					}
				}
		}
	}

	final WorkflowServer server;

	WorkflowDefinition workflowDefinition;

	public SelectDefinition(WorkflowServer server) {
		super();
		this.server = server;
	}

	void selectDefinition() {
		assert workflowDefinition == null : "command already running!";
		final Collection<WorkflowDefinition> definitions = server.getDefinitions();
		if(definitions.isEmpty()){
			// TODO user reportError event!
			System.out.println("No definitions to choose!");
		} else {
			DefinitionChooser chooser = new DefinitionChooser();
			server.env.registerListener(chooser);
			chooser.offer(definitions);
		}
	}

	public void reset() {
		logger.log(Level.INFO,"resetting deactivate definition cmd");
		workflowDefinition = null;
	}

	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	public void handle(CspProcess process) {
		selectDefinition();
	}

}
