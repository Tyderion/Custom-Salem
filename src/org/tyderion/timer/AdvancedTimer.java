package org.tyderion.timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.ender.timer.Timer;
import org.tyderion.timer.PropertiesGenerator;
import org.tyderion.timer.TimerData;

import haven.Coord;
import haven.Label;
import haven.ActAudio.Ambience.Glob;
import haven.Party.Member;
import haven.Session;
import haven.UI;
import haven.Window;


public class AdvancedTimer extends Timer{
    public interface  Callback {
	public void run(AdvancedTimer timer);
    }

//    private static final int SERVER_RATIO = 3;
    
    private static final String NAME = "name";
	private static final String DURATION = "duration";
	private static final String START = "start";
	private static final String SUFFIX = "suffix";
    private static final String COORD = "coords";
    
   
   
    private List<TimerData> data;
    
	public AdvancedTimer(long time, String name, String suffix){

	super();
	data =new ArrayList<TimerData>();
	synchronized(data) {
    	this.setDuration(time);
    	this.setName(name);
		data.add(new TimerData(0, suffix, new Coord(0,0)));

	}
	AdvancedTimerController.getInstance().add(this);
	}
	
    public boolean isWorking(){
    	synchronized(data) 
    	{
    		if (data.size() > 0) 
        	{
        		return data.get(0).getStart()  != 0;
        	}
        	else 
        	{
        		return false;
        	}
    	}
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
    
    public AdvancedTimer(Properties properties, String prefix) {
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
						data.add(new TimerData(properties, key.substring(0, key.length()-START.length()-1)));
						int a = 2;
						
    					
    					
    		}
    	}
    	Collections.sort(data);
    	if (data.size() > 0)
    	{
    		start = data.get(0).getStart();
    	}
    	int a = 2;
    	AdvancedTimerController.getInstance().add(this);
    	}

    	List<AdvancedTimer> t = AdvancedTimerController.getInstance().timers;
    }
    

   
    
    public void stop(){    	
    setStart(0);
    data = new ArrayList<TimerData>();
	AdvancedTimerController.getInstance().save();
    }
    
    
    public String askForString() {
    	//TODO Implement a window which asks for a string
    	return "";
    }
    
    public void start(){
    data = new ArrayList<TimerData>();
    data.add(new TimerData(toServerTime(System.currentTimeMillis()), askForString(), Session.glob.party.leader.c));
    start = data.get(0).getStart();
	AdvancedTimerController.getInstance().save();
    }
    
    
    public synchronized void add() {
   	    long new_start = toServerTime(System.currentTimeMillis());
   	    Window wnd = new Window(new Coord(250,100), Coord.z, UI.instance.root, name);
	   	new Label(Coord.z, wnd, Session.glob.party.leader.c.toString());
	    wnd.justclose = true;
	    wnd.pack();
   	    System.err.println(Session.glob.party.leader.c);
    	data.add(new TimerData(new_start, askForString(), Session.glob.party.leader.c));
    	AdvancedTimerController.getInstance().save();
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
	    		for (Iterator<TimerData> it = data.iterator(); it.hasNext(); ) {
	    		    TimerData a_start = it.next();
	    		    if (timePassed(a_start.getStart()) < -1500) {
	    		    	it.remove();
	    		    	number_of_finished_timers++;
	    		    }
		    			
		    		}
		str = String.format("%s elapsed since timer named \"%s\"  finished it's work %sx", Timer.timeToString(time_since_finish), name, number_of_finished_timers+1);
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
    

    public synchronized void setStart(long start, String suffix) {
    	super.setStart(start);
    	data = new ArrayList<TimerData>();
    	data.add(new TimerData(start, suffix, new Coord(0,0)));
    }
    public synchronized void setStart(long start) {
        setStart(start, "");
    }


    
    public int compareTo(AdvancedTimer that) {
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
    			return compareTo((AdvancedTimer)obj)==0?true:false;
    		}
    		catch(ClassCastException e) {
    			return false;
    		}
    		
    }
    
    public String toString() {
    	return data.get(0).toString();
    }
    
    
    public String debug() {
    	return "Name: "+getName()+", duration: "+getDuration()+", start: "+getStart()+", number of starts: "+data.size();
    }
    
}