package org.tyderion.timer;

import haven.Button;
import haven.Coord;
import haven.Label;
import haven.TextEntry;
import haven.Widget;
import haven.Window;

public class AdvancedTimerAddWdg extends Window{

private TextEntry name, hours, minutes, seconds, suffix;
private Button btnadd;
private AdvancedTimerPanel panel;

public AdvancedTimerAddWdg(Coord c, Widget parent, AdvancedTimerPanel panel) {
    super(c, Coord.z, parent, "Add timer");
    justclose = true;
    this.panel = panel;
    name = new TextEntry(Coord.z, new Coord(150,18), this, "timer");
    
    
    new Label(new Coord(0, 25),this,"hours");
    new Label(new Coord(50, 25),this,"min");
    new Label(new Coord(100, 25),this,"sec");
    hours = new TextEntry(new Coord(0, 40), new Coord(45,18), this, "0");
    minutes = new TextEntry(new Coord(50, 40), new Coord(45,18), this, "00");
    seconds = new TextEntry(new Coord(100, 40), new Coord(45,18), this, "00");
    
    new Label(new Coord(0, 60),this,"Suffix");
    suffix = new TextEntry(new Coord(50, 60), new Coord(100,18), this, "suffix");
    
    btnadd = new Button(new Coord(0, 80), 100, this, "Add");
    pack();
}

@Override
public void wdgmsg(Widget sender, String msg, Object... args) {
    if(sender == btnadd){
	try{
	    long time = 0;
	    time += Integer.parseInt(seconds.text);
	    time += Integer.parseInt(minutes.text)*60;
	    time += Integer.parseInt(hours.text)*3600;
	    AdvancedTimer timer = new AdvancedTimer(1000*time, name.text, suffix.text);
	    AdvancedTimerController.getInstance().save();
	    new AdvancedTimerWdg(Coord.z, panel, timer);
	    panel.pack();
	    ui.destroy(this);
	} catch(Exception e){
	    System.out.println(e.getMessage());
	    e.printStackTrace();
	}
    } else {
	super.wdgmsg(sender, msg, args);
    }
}

@Override
public void destroy() {
    AdvancedTimerPanel.instance = null;
    panel = null;
    super.destroy();
}

}