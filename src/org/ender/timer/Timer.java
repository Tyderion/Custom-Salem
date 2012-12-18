package org.ender.timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.tyderion.timer.PropertiesGenerator;
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
    
    private static final String NAME = "name";
	private static final String DURATION = "duration";
	private static final String START = "start";
    private static final String SUFFIX = "suffix";
    
    
    
    public static long server;
    public static long local;
    
    private long start;
    
    private List<Long> additional_starts;
    private List<TimerData> data;
   
    
    private long duration;
    private String name;
    private long mseconds;
    public Callback updcallback;
    
    
    
    
    
    public Timer(long time, String name){
    	data =new ArrayList<TimerData>();
    	synchronized(data) {
    		
    		data.add(new TimerData(0, name));
        	this.setDuration(time);
        	this.setName(name);
        	
        	TimerController.getInstance().add(this);
    	}
    	

    }
    
   
    public long getDuration() {
		return duration;
	}


	public void setDuration(long duration) {
		this.duration = duration;
	}


	public Timer( List<String> properties )
    {
    	
    	data = new ArrayList<TimerData>();
    	for (String str : properties) {
    		String[] props = str.split(",");
    		if (props.length == 1) {
    			if (props[0].matches("[0-9]+")) {
    				duration = Long.parseLong(props[0]);
    			}
    			else
    			{
    				name = props[0];
    			}
    		} else {
    			data.add(new TimerData(str));
    		}
    	}
    }
    
    public List<String> toStringList() 
    {
    	ArrayList<String> properties = new ArrayList<String>();
    	properties.add(name);
    	properties.add(String.valueOf(duration));
    	for (TimerData astart : data) {
    		properties.add(astart.toString());
    	}
    	return properties;
    }
    
    

    
    public synchronized Properties toProperties(String prefix) 
    {
    	PropertiesGenerator gen = new PropertiesGenerator(prefix);
    	Properties props = new Properties();
    	props.putAll(gen.toProperty(NAME, name));
    	props.putAll(gen.toProperty(DURATION, duration));
    	int i = 0;
    	
    	for (TimerData dat : data) {
    		props.putAll(dat.toProperties(prefix+"."+START+i));
    		i++;
    	}
    	return props;
    }
    
    public Timer(Properties properties, String prefix) {
    	data = new ArrayList<TimerData>();
    	synchronized(data) {
    	
    	List<String> keys = PropertiesGenerator.getMatchingEntries(properties.keySet(), prefix+"\\.(("+NAME+"|"+DURATION+")|"+START+".*\\.start)");
    	for (String key : keys) 
    	{
    		String keyprops[] = key.split("\\.");
    		
    		
    		switch (keyprops[keyprops.length-1]) {
    				case NAME:
    					name = properties.getProperty(key);
    					break;
    				case DURATION:
    					duration = Long.valueOf(properties.getProperty(key));
    					break;
    				case START:
    					// If it is the start a subtask
						data.add(new TimerData(
								Long.valueOf(properties.getProperty(key)),
								properties.getProperty(key.subSequence(0, key.length()-5)+SUFFIX)
								));
						
    					
    					
    		}
    	}
    	Collections.sort(data);
    	}
    	int a = 2;
    	if (a == 2) {
    		//Do Nothing
    	}
    }
    

    
    public boolean isWorking(){
	return start != 0;
    }
    
    public void stop(){
	start = 0;
	additional_starts = new ArrayList<Long>();
	if(updcallback != null){
	    updcallback.run(this);
	}
	TimerController.getInstance().save();
    }
    
    public void start(){
	start = server + SERVER_RATIO*(System.currentTimeMillis() - local);
	data.get(0).setStart(start);
	TimerController.getInstance().save();
    }
    
    
    public synchronized void add() {
   	    long new_start = server + SERVER_RATIO*(System.currentTimeMillis() - local);
   	    //additional_starts.add(new_start);
    	data.add(new TimerData(new_start, "Suffix"));
   	    
   	    TimerController.getInstance().save();
   	    //Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str = "Added new start "+timeToString(new_start-start);
	    //new Label(Coord.z, wnd, str);
	    //wnd.justclose = true;
	    //wnd.pack();
    }
    
    public synchronized boolean update(){
	long now = System.currentTimeMillis();
	mseconds = (duration - now + local - (server - start)/SERVER_RATIO);
	long time_since_finish = mseconds;
	
	if(mseconds <= 0){
	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	    String str;
	    //start = additional_starts.get(0);
	    if(mseconds < -1500){
	    	int number_of_finished_timers = 0;
	    		for (Iterator<Long> it = additional_starts.iterator(); it.hasNext(); ) {
	    		    Long a_start = it.next();
	    		    if ((duration - now + local - (server - a_start)/SERVER_RATIO) < -1500) {
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
        return data.get(0).getStart();
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
    	return timeToString(isWorking()?mseconds:duration);
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
    
    public int compareTo(Timer that) {
		 final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;
	    // If identical start sort by suffix
	    
	    int basic_state = EQUAL;
	    if (this.getName() == that.getName()) 
	    {
	    	if (this.getStart() == that.getStart())
			{
	    		if (this.getDuration() == that.getDuration())
	    		{
	    			basic_state = EQUAL;
	    		} else {
	    			basic_state = ((Long)this.getDuration()).compareTo(that.getDuration());
	    		}
	    		
					
			} else 
			{
				basic_state = ((Long)this.getStart()).compareTo(that.getStart());
			}
	    } else {

	    	if (this.getStart() == that.getStart())
			{    		
	    		basic_state = ((Long)this.getDuration()).compareTo(that.getDuration());
			}else 
			{
				basic_state = ((Long)this.getStart()).compareTo(that.getStart());
			}
	    }
	    if (basic_state == EQUAL) 
	    {
	    	if (this.data.size() == that.data.size())
	    	{
	    		for (int i = 0; i < data.size();i++ )
	    		{
	    			basic_state = data.get(i).compareTo(that.data.get(i));
	    			if (basic_state != EQUAL) { return basic_state;}
	    		}
	    		
	    	}
	    }

		//assert this.equals(that) : "compareTo inconsistent with equals.";

	    return basic_state;
	}
    
    @Override
    public boolean equals(Object obj) {
    		try {
    			return compareTo((Timer)obj)==0?true:false;
    		}
    		catch(ClassCastException e) {
    			return false;
    		}
    		
    }
    
}