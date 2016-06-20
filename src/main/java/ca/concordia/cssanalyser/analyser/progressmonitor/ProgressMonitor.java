package ca.concordia.cssanalyser.analyser.progressmonitor;

public interface ProgressMonitor {
	
	public void progressed(int percent);
	
	public boolean shouldStop();
}
