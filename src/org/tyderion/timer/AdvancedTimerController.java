package org.tyderion.timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.ender.timer.Timer;

import org.ender.timer.TimerController;
import org.tyderion.timer.AdvancedTimer;

public class AdvancedTimerController extends Thread {
	private static AdvancedTimerController instance;
	public List<AdvancedTimer> timers;
	private static File config;
	private Properties options;

	public static AdvancedTimerController getInstance() {
		if (instance == null ) {
			instance = new AdvancedTimerController();
		}
		return instance;
	}

	public AdvancedTimerController() {
		super("Advanced Timer Thread");
		options = new Properties();
		timers = new ArrayList<AdvancedTimer>();
		setDaemon(true);
		start();
	}
	
	// Thread main process
    @Override
    public void run() {
	while(true) {
	    synchronized (timers) {
		for(AdvancedTimer timer : timers){
		    if((timer.isWorking())&&(timer.update())){
			timer.stop();
		    }
		}
	    }	    
	    try {
		sleep(1000);
	    } catch (InterruptedException e) {}
	}
    }

	public static void init(File folder, String server) {
		config = new File(folder, String.format("advanced_timer_%s.cfg", server));
		AdvancedTimerController.getInstance().load();
	}


	public void add(AdvancedTimer advancedTimer) {
		synchronized (timers) {
			timers.add(advancedTimer);
			int a = 2;
		}
	}

	public void remove(AdvancedTimer timer) {
		synchronized (timers) {
			timers.remove(timer);
		}
	}
	
	


	public void load() {
		synchronized (options) {
			try {
				options.load(new FileInputStream(config));
				synchronized (timers) {
					timers.clear();

						for (String key : PropertiesGenerator.getMatchingEntries(options.keySet(), "Timer[0-9*]\\.name"))
						{
							System.out.println("Loading with key: "+key);
							new AdvancedTimer(options, key.split("\\.")[0]);
							System.out.println("Timers: "+timers);
						}
				}
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}
	
	public void save() {
		int i = 0;
		synchronized (options) {
			options.clear();
			synchronized (timers) {
				for (AdvancedTimer timer : timers) {
					
					options.putAll(timer.toProperties("Timer" + i));
					i++;
				}
			}
			try {
				options.store(new FileOutputStream(config), "Advanced Timers config");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				
			}
		}

	}
}