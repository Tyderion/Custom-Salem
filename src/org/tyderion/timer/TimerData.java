package org.tyderion.timer;

public class TimerData implements Comparable<TimerData>{
	
	
	
	
	private String suffix;
	private long start;
	
	
	public TimerData(long start, String suffix) 
	{
		this.suffix = suffix;
		this.start = start;
	}

	
	public TimerData(String properties) {
		String[] props = properties.split(",");
		start = Long.parseLong(props[0]);
		suffix = props[1];
	}
	
	
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
		
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
	    // If identical start sort by suffix
	    if (this.getStart() == that.getStart()) { 
	    		return this.getSuffix().compareTo(that.getSuffix());
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
	       ( this.getSuffix() == that.getSuffix() );
	}

	
	@Override
	public String toString() {
		return String.valueOf(getStart())+","+getSuffix();
				}
	
}
