package org.cspk4j;

import java.util.Collection;

public interface EventChangeListener {
	void eventsChanged(/*@ non_null @*/ Collection<CspEvent> events);
}