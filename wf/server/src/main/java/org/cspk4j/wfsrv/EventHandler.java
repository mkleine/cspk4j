package org.cspk4j.wfsrv;

import org.cspk4j.CspProcess;

public interface EventHandler {

	void handle(CspProcess process);
}