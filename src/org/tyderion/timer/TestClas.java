package org.tyderion.timer;

import java.io.File;
import java.util.Properties;

import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TestClas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AdvancedTimerController.init(new File("C:\\Users\\Archie\\Salem"), "jamestown.seatribe.se");
		System.out.println(AdvancedTimerController.getInstance().timers);
		AdvancedTimer t = new AdvancedTimer(6000,"name4", "suffix4");
		t.start();

		AdvancedTimerController.getInstance().save();
//		System.out.println(t.debug());
//		for (Timer e : TimerController.getInstance().timers)
//		{
//			System.out.println(((AdvancedTimer)e).toProperties("prefix"));
//		}
//		
//		t.start();
//		Properties s= t.toProperties("prefix");
//		
//		t.add();
//		t.add();
//		t.add();
//		s= t.toProperties("prefix");
//		Timer t2 = new Timer(s, "prefix");
//		System.err.println(t2.toProperties("prefix"));
//		System.out.println(s.toString());
//		System.out.println("Is the first timer the same as the second? "+t.equals(t2));

	}

}
