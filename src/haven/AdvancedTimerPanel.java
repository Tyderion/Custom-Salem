package haven;


import org.ender.timer.Timer;
import org.tyderion.timer.AdvancedTimerController;

public class AdvancedTimerPanel extends Window {
	 static TimerPanel instance;
	    private Button btnnew;
	    
	    public static void toggle(){
		if(instance == null){
		    instance = new TimerPanel(UI.instance.gui);
		} else {
		    UI.instance.destroy(instance);
		}
	    }
	    
	    public AdvancedTimerPanel(Widget parent){
		super(new Coord(250, 100), Coord.z, parent, "Timers");
		justclose = true;
		btnnew = new Button(Coord.z, 100, this, "Add timer");
		
		synchronized (AdvancedTimerController.getInstance().timers){
		    for(Timer timer : AdvancedTimerController.getInstance().timers){
			new TimerWdg(Coord.z, this, timer);
		    }
		}
		pack();
	    }

	    @Override
	    public void pack() {
		int n, i=0, h = 0;
		synchronized (AdvancedTimerController.getInstance().timers){
		    n = AdvancedTimerController.getInstance().timers.size();
		}
		n = (int) Math.ceil(Math.sqrt((double)n/3));
		for(Widget wdg = child; wdg != null; wdg = wdg.next) {
		    if(!(wdg instanceof AdvancedTimerWdg))
			continue;
		    wdg.c = new Coord((i%n)*wdg.sz.x, ((int)(i/n))*wdg.sz.y);
		    h = wdg.c.y + wdg.sz.y;
		    i++;
		}
		
		btnnew.c = new Coord(0,h);
		super.pack();
	    }

	    @Override
	    public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == btnnew){
		    new AdvancedTimerAddWdg(c, ui.root, this);
		} else {
		    super.wdgmsg(sender, msg, args);
		}
	    }
	    
	    @Override
	    public void destroy() {
		instance = null;
		super.destroy();
	    }

	    public static void close() {
		if(instance != null){
		    instance.destroy();
		}
	    }
}
