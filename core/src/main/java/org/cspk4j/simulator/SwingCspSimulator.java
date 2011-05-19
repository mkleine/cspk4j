/**
 *   This file is part of CSPk4J the CSP concurrency library for Java.
 *
 *   CSPk4J is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CSPk4J is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CSPk4J.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package org.cspk4j.simulator;

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

import org.cspk4j.CspEnvironment;
import org.cspk4j.CspEvent;
import org.cspk4j.DefaultFilter;
import org.cspk4j.Disposable;
import org.cspk4j.Filter;

@SuppressWarnings("serial")
public class SwingCspSimulator extends CspSimulator implements Disposable {

	static class PerformEventAction extends AbstractAction {
		final Logger logger = Logger.getLogger(getClass().getName());
		final CspEvent event;

		public PerformEventAction(CspEvent event) {
			super(event.toString());
			this.event = event;
			logger.log(Level.INFO,"created action for event "+event.getName());
		}


		public void actionPerformed(ActionEvent e) {
			logger.log(Level.INFO, "chosen event: "+event.getName());
			event.perform();

		}

	}

	final Frame frame;
	final JLabel processName = new JLabel("unknown process");
	final JSplitPane pane;

	public SwingCspSimulator(String title, final CspEnvironment environment){
		this(title, environment, new DefaultFilter());
	}
	
	public SwingCspSimulator(String title, final CspEnvironment environment, Filter filter) {
		super(environment, filter);
		frame = new Frame();
		JDialog dialog = new JDialog(frame,title);
		pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JPanel panel = new JPanel();
		panel.add(processName);
		panel.add(new JLabel("offered events:"));
		pane.add(panel);

		dialog.add(pane);
		dialog.validate();
		dialog.setMinimumSize(new Dimension(200,300));
		dialog.setVisible(true);
	}

	void internalEventChanged(Collection<CspEvent> events) {
		if(pane.getComponentCount() > 2)
			pane.remove(2);

		if(events.isEmpty()){
			logger.log(Level.INFO,"events changed to "+events);
		} else {

			final JPanel panel = new JPanel();
			pane.add(panel);

			ArrayList<CspEvent> sorted = new ArrayList<CspEvent>(events);
			java.util.Collections.sort(sorted);
			for(CspEvent event : sorted) {
				final JButton button = new JButton(new PerformEventAction(event));
				panel.add(button);
			}

			panel.updateUI();
		}
	}

	@Override
	public void run(String name) {
		if(name != null)
			processName.setText(name);
		super.run(name);
	}

	@Override
	protected
	final void internalRun() {
		if(environment.running()) {
			try {
				environment.wait();
			} catch (InterruptedException e) {
				logger.log(Level.INFO, "interrupted", e);
			}
		}
	}

	public void dispose() {
		logger.log(Level.INFO,"disposing frame...");
		frame.dispose();
	}
}
