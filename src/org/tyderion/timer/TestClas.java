package org.tyderion.timer;

import java.util.Properties;

import org.ender.timer.Timer;

public class TestClas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Timer t = new Timer(1000, "test");
		Properties s= t.toProperties("prefix");
		System.err.println(s);
		Timer t2 = new Timer(s, "prefix");
		System.err.println(t2.toProperties("other"));

	}

}
