package org.tyderion.timer;

import java.util.Properties;

import org.ender.timer.Timer;

public class TestClas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Timer t = new Timer(5000,"name");
		t.start();
		Properties s= t.toProperties("prefix");
		
		t.add();
		t.add();
		t.add();
		s= t.toProperties("prefix");
		Timer t2 = new Timer(s, "prefix");
		System.err.println(t2.toProperties("prefix"));
		System.out.println(s.toString());
		System.out.println("Is the first timer the same as the second? "+t.equals(t2));

	}

}
