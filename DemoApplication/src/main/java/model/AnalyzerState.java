package model;

import java.util.ArrayList;
import java.util.Collection;

public class AnalyzerState {
	
	private Collection<VcfAnalyzerTask> openTasks = new ArrayList<>();
	private Collection<VcfAnalyzerTask> finishedTasks = new ArrayList<>();
	private VcfAnalyzerTask currentTask;
	
	public Collection<VcfAnalyzerTask> getOpenTasks() {
		return openTasks;
	}
	public void setOpenTasks(Collection<VcfAnalyzerTask> openTasks) {
		this.openTasks = openTasks;
	}
	public Collection<VcfAnalyzerTask> getFinishedTasks() {
		return finishedTasks;
	}
	public void setFinishedTasks(Collection<VcfAnalyzerTask> finishedTasks) {
		this.finishedTasks = finishedTasks;
	}
	public VcfAnalyzerTask getCurrentTask() {
		return currentTask;
	}
	public void setCurrentTask(VcfAnalyzerTask currentTask) {
		this.currentTask = currentTask;
	}
	
	

}
