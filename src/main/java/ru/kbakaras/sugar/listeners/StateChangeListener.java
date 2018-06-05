package org.butu.sugar.listeners;

public interface StateChangeListener {
	public static final String EVT_stateChanged = "stateChanged";
	public void stateChanged();
}