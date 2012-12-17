package org.ender.timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tyderion.timer.TimerData;

import haven.Coord;
import haven.Label;
import haven.UI;
import haven.Window;


public class Timer {
    public interface  Callback {
	public void run(Timer timer);
    }

    private static final int SERVER_RATIO = 3;
    
    public static long server;
    public static long local;
    
    private long start;
    
    private List<Long> additional_starts;
    private List<TimerData> data;
   
    
    private long time;
    private String name;
    private long mseconds;
    public Callback updcallback;
    
    
    
    
    public Timer(long start, long time, String name, List<Long> additional_starts){
    	this.start = start;
    	this.time = time;
    	this.name = name;
    	this.additional_starts = additional_starts;
    	TimerController.getInstance().add(this);
    	
    }
    
    public Timer(long start, long time, String name){
    	this(start, time, name, new ArrayList<Long>());
    }
    
    public Timer(long time, String name){
	this(0, time, name, new ArrayList<Long>());
    }
    
    public boolean isWorking(){
	return start != 0;
    }
    
    public void stop(){
	start = 0;
	if (additional_starts.size() > 0) {
		start = additional_starts.remove(0);	
	}
	if(updcallback != null){
	    updcallback.run(this);
	}
	TimerController.getInstance().save();
    }
    
    public void start(){
	start = server + SERVER_RATIO*(System.currentTimeMillis() - local);
	TimerController.getInstance().save();
    }
    
    
    public synchronized void add() {
   	    long new_start = server + SERVER_RATIO*(System.currentTimeMillis() - local);
   	    additional_starts.add(new_start);
   	    TimerController.getInstance().save();
   	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str = "Added new start at "+toString();
	    new Label(Coord.z, wnd, str);
	    wnd.justclose = true;
	    wnd.pack();
    }
    
    public synchronized boolean update(){
	long now = System.currentTimeMillis();
	mseconds = (time - now + local - (server - start)/SERVER_RATIO);
	long time_since_finish = mseconds;
	
	if(mseconds <= 0){
	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str;
	    //start = additional_starts.get(0);
	    if(mseconds < -1500){
	    	int number_of_finished_timers = 0;
	    		for (Iterator<Long> it = additional_starts.iterator(); it.hasNext(); ) {
	    		    Long a_start = it.next();
	    		    if ((time - now + local - (server - a_start)/SERVER_RATIO) < -1500) {
	    		    	it.remove();
	    		    	number_of_finished_timers++;
	    		    }
		    			
		    		}
		str = String.format("%s elapsed since timer named \"%s\"  finished it's work %sx", timeToString(time_since_finish), name, number_of_finished_timers+1);
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
	return time;
    }
    
    public synchronized List<Long> getAdditionalStarts()
    {
    	return additional_starts;
    }

    @Override
    public String toString() {
    	return timeToString(isWorking()?mseconds:time);
    }
    
    private String timeToString(long _time) {
    	long time = Math.abs(_time)/1000;
    	int h = (int) (time/3600);
    	int m = (int) ((time%3600)/60);
    	int s = (int) (time%60);
    	return String.format("%d:%02d:%02d", h,m,s);
    }
    
    public void destroy(){
	TimerController.getInstance().remove(this);
	updcallback = null;
    }
    
}