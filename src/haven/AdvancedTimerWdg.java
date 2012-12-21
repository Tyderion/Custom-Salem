package haven;

import org.ender.timer.Timer;
import org.ender.timer.TimerController;
import org.tyderion.timer.AdvancedTimer;

public class AdvancedTimerWdg extends Widget {
	 static Tex bg = Resource.loadtex("gfx/hud/bosq");
	    private Timer timer;
	    public Label time, name;
	    private Button start, stop, delete, add;
	    
	    public AdvancedTimerWdg(Coord c, Widget parent, AdvancedTimer timer) {
		super(c, bg.sz(), parent);

		this.timer = timer;
		timer.updcallback =  new Timer.Callback() {
		    
		    @Override
		    public void run(Timer timer) {
			synchronized (time) {
			    time.settext(timer.toString());
			    updbtns();
			}
			
		    }
		};
		name = new Label(new Coord(5,5), this, timer.getName());
		time = new Label(new Coord(5, 25), this, timer.toString());
		
		start = new Button(new Coord(90,2), 50, this, "start");
		stop = new Button(new Coord(90,2), 50, this, "stop");
		delete = new Button(new Coord(90,21), 50, this, "delete");
		add = new Button(new Coord(150,2), 50, this, "add");

		updbtns();
	    }
	    
	    private void updbtns(){
		start.visible = !timer.isWorking();
		stop.visible = timer.isWorking();
		add.visible = timer.isWorking();
	    }
	    
	    @Override
	    public void destroy() {
		unlink();
		if(parent instanceof TimerPanel){
		    ((TimerPanel) parent).pack();
		}
		timer.updcallback = null;
		timer = null;
		super.destroy();
	    }

	    @Override
	    public void draw(GOut g) {
		g.image(bg, Coord.z);
		super.draw(g);
	    }

	    @Override
	    public void wdgmsg(Widget sender, String msg, Object... args) {
		if(sender == start){
		    timer.start();
		    updbtns();
		} else if(sender == stop){
		    timer.stop();
		    updbtns();
		} else if(sender == delete){
		    timer.destroy();
		    TimerController.getInstance().save();
		    ui.destroy(this);
		} else if(sender == add) {
			timer.add();
			
			
		} else {
		    super.wdgmsg(sender, msg, args);
		}
	    }    
	    

}
