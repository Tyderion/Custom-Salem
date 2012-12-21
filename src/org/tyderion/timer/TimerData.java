package org.tyderion.timer;

import haven.Coord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TimerData implements Comparable<TimerData> {

	// Todo: Store location for use in map markers...?
	private String suffix;
	private long start;
	private Coord coords;

	public TimerData(long start, String suffix, Coord coords) {
		this.suffix = suffix;
		this.start = start;
		if (coords == null) {
			this.coords = new Coord(3, 3);
		}
		else
		{
			this.coords = coords;
		}
	}

	public TimerData(String properties) {
		String[] props = properties.split(",");
		start = Long.parseLong(props[0]);
		suffix = props[1];
	}

	public TimerData(Properties properties, String prefix) {
		List<String> keys = PropertiesGenerator.getMatchingEntries(
				properties.keySet(), prefix + "\\.(start|suffix|coords)");
		for (String key : keys) {
			String keyprops[] = key.split("\\.");
			if (keyprops.length > 0) {
				switch (keyprops[keyprops.length - 1]) {
				case "start":
					start = Long.valueOf(properties.getProperty(key));
					break;
				case "suffix":
					suffix = properties.getProperty(key);
					break;
				case "coords":
					String[] coords_str = properties.getProperty(key).split(",");
					coords = new Coord(Integer.valueOf(coords_str[0]), Integer.valueOf(coords_str[1]));
					break;
				}
			}

		}
	}
	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;

	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	@Override
	public int compareTo(TimerData that) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		// If identical start sort by suffix
		if (this.getStart() == that.getStart()) {
			return this.getSuffix().compareTo(that.getSuffix());
		}
		if (this.getStart() < that.getStart()) {
			return BEFORE;
		}
		if (this.getStart() > that.getStart()) {
			return AFTER;
		}
		assert this.equals(that) : "compareTo inconsistent with equals.";

		return EQUAL;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat)
			return true;
		if (!(aThat instanceof TimerData))
			return false;

		TimerData that = (TimerData) aThat;
		return (this.getStart() == that.getStart())
				&& (this.getSuffix() == that.getSuffix());
	}

	@Override
	public String toString() {
		return "TimerData:( " + String.valueOf(getStart()) + "," + getSuffix()+", Coords: "+coords.toString()
				+ ")";
	}

	public Properties toProperties(String prefix) {
		PropertiesGenerator gen = new PropertiesGenerator(prefix);
		Properties props = new Properties();
		props.putAll(gen.toProperty("start", start));
		props.putAll(gen.toProperty("suffix", suffix));
		props.putAll(gen.toProperty("coords", coords.x+","+coords.y));
		return props;

	}

}
