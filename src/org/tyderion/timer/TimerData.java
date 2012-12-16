package org.tyderion.timer;

public class TimerData implements Comparable<TimerData>{

	private String suffix;
	private long start, duration;
	
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
		
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	@Override
	public int compareTo(TimerData that) {
		 final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    // If identical start sort by duration
	    if (this.getStart() == that.getStart()) { 
	    	//If identical duration sort by suffix
	    	if (this.getDuration() == that.getDuration()) { 
	    		return this.getSuffix().compareTo(that.getSuffix());
	    	}
	    	if (this.getDuration() < that.getDuration()) { return BEFORE; }
	    	if (this.getDuration() > that.getDuration()) { return AFTER; }
	    }
		if (this.getStart() < that.getStart()) { return BEFORE; }
		if (this.getStart() > that.getStart()) { return AFTER; }
		assert this.equals(that) : "compareTo inconsistent with equals.";

	    return EQUAL;
	}
	@Override public boolean equals( Object aThat ) {
	     if ( this == aThat ) return true;
	     if ( !(aThat instanceof TimerData) ) return false;

	     TimerData that = (TimerData)aThat;
	     return
	       ( this.getStart() == that.getStart() ) &&
	       ( this.getDuration() == that.getDuration() ) &&
	       ( this.getSuffix() == that.getSuffix() );
	}

	
	
	public String getProperty() {
		return String.valueOf(getStart())+","+String.valueOf(getDuration())+","+getSuffix();
				}
	
}
