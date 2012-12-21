package org.ender.timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import haven.Coord;
import haven.Label;
import haven.UI;
import haven.Window;


public class Timer {
    public interface  Callback {
	public void run(Timer timer);
    }

    protected static final int SERVER_RATIO = 3;
    
    public static long server;
    public static long local;
    
    protected long start;
    
    private List<Long> additional_starts;
    protected long duration;


	protected String name;
    protected long mseconds;
    public Callback updcallback;
    
    
    
    
    public Timer(long start, long time, String name, List<Long> additional_starts){
    	setStart(start);
    	setDuration(time);
    	setName(name);
    	this.additional_starts = additional_starts;
    	TimerController.getInstance().add(this);
    }
    
    public Timer(long start, long time, String name){
    	this(start, time, name, new ArrayList<Long>());
    }
    
    public Timer(long time, String name){
	this(0, time, name, new ArrayList<Long>());
    }
    
    public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
    
    public boolean isWorking(){
	return getStart() != 0;
    }
    
    public void stop(){
	setStart(0);
	if (additional_starts.size() > 0) {
		setStart(additional_starts.remove(0));	
	}
	if(updcallback != null){
	    updcallback.run(this);
	}
	TimerController.getInstance().save();
    }
    
    public void start(){
	setStart(toServerTime(System.currentTimeMillis()));
	TimerController.getInstance().save();
    }
    
    
    public synchronized void add() {
   	    long new_start = toServerTime(System.currentTimeMillis());
   	    additional_starts.add(new_start);
   	    TimerController.getInstance().save();
   	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str = "Added new start at "+toString();
	    new Label(Coord.z, wnd, str);
	    wnd.justclose = true;
	    wnd.pack();
    }

	protected long toServerTime(long time) {
		return server + SERVER_RATIO*(time - local);
	}
	protected long timePassed(long time) {
		return duration - time + local - (server - start)/SERVER_RATIO;
	}
    
    public synchronized boolean update(){
	long now = System.currentTimeMillis();
	mseconds = timePassed(now);
	
	
	
	if(mseconds <= 0){
	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str;
	    //start = additional_starts.get(0);
	    if(mseconds < -1500){
	    	int number_of_finisheds = 0;
	    		for (Iterator<Long> it = additional_starts.iterator(); it.hasNext(); ) {
	    		    Long a_start = it.next();
	    		    if (timePassed(a_start) < -1500) {
	    		    	it.remove();
	    		    }
		    			number_of_finisheds++;
		    		}
	    		
		str = String.format("%s elapsed since timer named \"%s\"  finished it's work %s x", toString(), name, number_of_finisheds+1);
	    } else {
		str = String.format("Timer named \"%s\" just finished it's work", name);
	    }
	    new Label(Coord.z, wnd, str);
	    wnd.justclose = true;
	    wnd.pack();
	    return true;
	}
	if(updcallback != null){
	    updcallback.run(this);
	}
	return false;
    }


    
    public synchronized long getStart() {
        return start;
    }

    public synchronized void setStart(long start) {
        this.start = start;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized long getTime()
    {
	return duration;
    }
    
    public synchronized List<Long> getAdditionalStarts()
    {
    	return additional_starts;
    }

    @Override
    public String toString() {
	return Timer.timeToString(Math.abs(isWorking()?mseconds:duration)/1000);
    }
    
    public static String timeToString(long _time) {
    	long time = Math.abs(_time)/1000;
    	int h = (int) (time/3600);
    	int m = (int) ((time%3600)/60);
    	int s = (int) (time%60);
    	return String.format("%d:%02d:%02d", h,m,s);
    }
    
    public String debug() {
    	return "Timer: "+getName()+" , start: "+getStart()+", duration: "+getDuration();
    }
    
    public void destroy(){
	TimerController.getInstance().remove(this);
	updcallback = null;
    }
    
}