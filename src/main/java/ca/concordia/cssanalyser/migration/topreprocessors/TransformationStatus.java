package ca.concordia.cssanalyser.migration.topreprocessors;

import java.util.ArrayList;
import java.util.List;

public class TransformationStatus {
	
	public enum TransformationStatusFlag {
		ERROR,
		FATAL,
		INFO,
		WARNING
	}
	
	public static class TransformationStatusEntry {
		private final String beforeSourceCode;
		private final String afterSourceCode;
		private final TransformationStatusFlag statusFlag;
		private final String optionalMessage;
		
		private TransformationStatusEntry(String beforeSourceCode, String afterSourceCode, 
				TransformationStatusFlag statusFlag, String optionalMessage) {
			this.beforeSourceCode = beforeSourceCode;
			this.afterSourceCode = afterSourceCode;
			this.statusFlag = statusFlag;
			this.optionalMessage = optionalMessage;
		}

		public String getBeforeSourceCode() {
			return beforeSourceCode;
		}

		public String getAfterSourceCode() {
			return afterSourceCode;
		}
		
		public String getOptionalMessage() {
			return optionalMessage;
		}
		
		public TransformationStatusFlag getStatusFlag() {
			return statusFlag;
		}
		
		@Override
		public String toString() {
			String toReturn = getStatusFlag().toString();
			if (!"".equals(optionalMessage.trim()))
				toReturn += ": " + optionalMessage;
			return toReturn;
		}

	}

	private final List<TransformationStatusEntry> entries;
	
	public TransformationStatus() {
		entries = new ArrayList<>();
	}
	
	public void addStatusEntry(String before, String after, TransformationStatusFlag statusFlag) {
		entries.add(new TransformationStatusEntry(before, after, statusFlag, ""));
	}
	
	public void addStatusEntry(String before, String after, TransformationStatusFlag statusFlag, String optionalMessage) {
		entries.add(new TransformationStatusEntry(before, after, statusFlag, optionalMessage));
	}
	
	public boolean isOK() {
		return entries.isEmpty();
	}

	public List<TransformationStatusEntry> getStatusEntries() {
		return new ArrayList<>(entries);
	}
	
}
