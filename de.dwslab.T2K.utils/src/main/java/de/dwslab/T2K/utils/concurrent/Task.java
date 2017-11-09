package de.dwslab.T2K.utils.concurrent;

import de.dwslab.T2K.utils.concurrent.Parallel.ITask;

public abstract class Task implements ITask {
	private Object userData;
	public Object getUserData() {
		return userData;
	}
	public void setUserData(Object userData) {
		this.userData = userData;
	}
	public abstract void execute();
}
