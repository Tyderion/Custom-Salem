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

public class AdvancedTimerController extends TimerController {
	private static File config;
	private Properties options;

	public AdvancedTimerController() {
		super();
		options = new Properties();
		timers = new ArrayList<Timer>();
		setDaemon(true);
		start();
	}

	public static void init(File folder, String server) {
		
		config = new File(folder, String.format("advanced_timer_%s.cfg", server));
		getInstance().load();
	}


	public void add(AdvancedTimer advancedTimer) {
		synchronized (timers) {
			timers.add(advancedTimer);
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

					for (Object key : options.keySet()) {
						String str = key.toString();
						String prefix = str.split("_")[0];
						
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
				for (Timer timer : timers) {
					options.putAll(((AdvancedTimer)timer).toProperties("Timer" + i));
					i++;
				}
			}
			try {
				options.store(new FileOutputStream(config), "Advanced Timers config");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				
			}
			catch (Exception e) {
				System.err.println("STUPID NULLPOINTERS IN CONFIG XD");
			}
		}

	}
}