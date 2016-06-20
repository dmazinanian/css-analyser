package ca.concordia.cssanalyser.analyser.progressmonitor;

/**
 * Very simple progress monitor,
 * that should be implmeneted by the clients that run a time-consuming
 * progress (e.g., FPGrowth).
 * @author Davood Mazinanian
 *
 */
public class ProgressMonitorDelegator {
	
	private int state = 0;
	private int finalState;
	
	private final ProgressMonitor progressMonitor;
	
	public ProgressMonitorDelegator(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}
	
	public void setFinalState(int finalState) {
		this.finalState = finalState;
	}
	
	public void worked(int i) {
		state += i;
		if (state <= finalState) {
			progressMonitor.progressed((int)Math.ceil(state / (float)finalState * 100));
		}
	}
	
	public void finished() {
		worked(finalState - state);
	}
	
	public boolean shouldStop() {
		return progressMonitor.shouldStop();
	}
}