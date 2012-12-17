package org.ender.timer;

import java.util.ArrayList;
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
    	this.duration = time;
    	this.name = name;
    	TimerController.getInstance().add(this);
    }
    
    
    public Timer( List<String> properties )
    {
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
    
    

    
    public Properties toProperties(String prefix) 
    {
    	PropertiesGenerator gen = new PropertiesGenerator(prefix);
    	Properties props = new Properties();
    	props.putAll(gen.toProperty("name", name));
    	props.putAll(gen.toProperty("start", start));
    	props.putAll(gen.toProperty("duration", duration));
    	int i = 0;
    	for (TimerData dat : data) {
    		props.putAll(dat.toProperties(prefix+".substart"+i));
    	}
    	return props;
    }
    
    public Timer(Properties properties, String prefix) {
    	List<String> keys = PropertiesGenerator.getMatchingEntries(properties.keySet(), prefix+"\\.");
    	for (String key : keys) 
    	{
    		String keyprops[] = key.split(".");
    		data = new ArrayList<TimerData>();
    		switch (keyprops[0]) {
    				case "name":
    					name = properties.getProperty(key);
    					break;
    				case "duration":
    					duration = Long.valueOf(properties.getProperty(key));
    					break;
    				default:
    					// If it is the start a subtask
    					if (keyprops[1].matches("substart[0-9]*\\.start"))
    					{
    							data.add(new TimerData(properties, keyprops[0]+"."+keyprops[1]));
    					}
    					
    		}
    		
    		
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
    
}