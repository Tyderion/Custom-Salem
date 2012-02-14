package haven;

import java.awt.event.KeyEvent;
import java.util.*;

class FlatnessTool extends Window implements MapView.Grabber {
    static final String title = "Area selection";
    static final String defaulttext = "Select area";
    
    static public float minheight = 0;
    static public float maxheight = 1;
    
    private final Label text;
    private final MapView mv;
    boolean dm = false;
    Coord sc;
    Coord c1, c2;
    MCache.Overlay ol;
    final List<MCache.Overlay> lowestol = new ArrayList<MCache.Overlay>();
    final MCache map;
    private Button btnToggle;
    private boolean grabbed = false;
    
    private static FlatnessTool instance; 

    public FlatnessTool(MapView mv, String text, Coord c, Widget parent) {
        super(c, new Coord(150, 50), parent, title);
        this.map = this.ui.sess.glob.map;
        this.text = new Label(Coord.z, this, defaulttext);
        this.mv = mv;
        this.mv.enol(MapView.WFOL);
        btnToggle = new Button(new Coord(0, 20), 75, this, "");
        //toggle();
        this.pack();
    }
    
    public static FlatnessTool instance(UI ui) {
	if(instance == null){
	    instance = new FlatnessTool(ui.gui.map, null,  new Coord(100, 100), ui.root);
	}
	return instance;
    }
    
    public void toggle(){
	grabbed = !grabbed;
	if(grabbed){
	    mv.grab(this);
	    btnToggle.change("Release");
	} else {
	    mv.release(this);
	    btnToggle.change("Grab");
	}
    }
    
    private void checkflatness(Coord c1, Coord c2) {
        if (c1.equals(this.c1) && c2.equals(this.c2))
            return;
        c2 = c2.add(1,1);
        this.c1 = c1;
        this.c2 = c2;
        
        minheight = Float.MAX_VALUE;
        maxheight = -minheight;
        float h = 0;
        boolean flat = true;
        float prevheight = Float.NaN;
        Coord c = new Coord();
        flat = true;
        for (c.x = c1.x; c.x <= c2.x; c.x++) {
            for (c.y = c1.y; c.y <= c2.y; c.y++) {
        	h = map.getcz(c.mul(MCache.tilesz));
        	if (Math.abs(h - prevheight) > 0.0001) {
        	    flat = false;
        	}
        	prevheight = h;
        	if (h < minheight) {
        	    minheight = h;
        	}
        	if (h > maxheight) {
        	    maxheight = h;
        	}
            }
        }

        String text = "";
        if (flat)
            text += "Area is flat.";
        else {
            text += "Area isn't flat.";
        }
        text += String.format(" Lowest: [%.2f], Highest: [%.2f].", minheight, maxheight);

        settext(text);

        this.pack();
    }

    public final void close() {
        this.cbtn.click();
    }

    @Override
    public void destroy() {
        if (this.ol != null)
            this.ol.destroy();
        for (MCache.Overlay ol : lowestol)
            ol.destroy();
        this.mv.disol(MapView.WFOL);
        this.mv.release(this);
        instance = null;
        super.destroy();
    }

    @Override
    public boolean mmousedown(Coord mc, int button) {
        Coord c = mc.div(MCache.tilesz);
        if (this.ol != null)
            this.ol.destroy();
        this.ol = map.new Overlay(c, c, 1<<MapView.WFOL);
        this.sc = c;
        this.dm = true;
        this.ui.grabmouse(this.mv);
        
        checkflatness(c, c);
        
        return true;
    }

    @Override
    public boolean mmouseup(Coord mc, int button) {
        this.dm = false;
        this.ui.grabmouse(null);
        return true;
    }

    @Override
    public void mmousemove(Coord mc) {
        if (!this.dm)
            return;
        Coord c = mc.div(MCache.tilesz);
        Coord c1 = new Coord(0, 0);
        Coord c2 = new Coord(0, 0);
        if (c.x < this.sc.x) {
            c1.x = c.x;
            c2.x = this.sc.x;
        } else {
            c1.x = this.sc.x;
            c2.x = c.x;
        }
        if (c.y < this.sc.y) {
            c1.y = c.y;
            c2.y = this.sc.y;
        } else {
            c1.y = this.sc.y;
            c2.y = c.y;
        }
        this.ol.update(c1, c2);
        checkflatness(c1, c2);
    }
    
    @Override
    public void uimsg(String msg, Object... args) {
        if (msg == "reset") {
            this.ol.destroy();
            this.ol = null;
            this.c1 = (this.c2 = null);
        }
    }

    public boolean type(char key, java.awt.event.KeyEvent ev) {
        if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE) {
            close();
            return true;
        }
        return super.type(key, ev);
    }

    @Override
    public void wdgmsg(Widget wdg, String msg, Object... args) {
        if (wdg == cbtn) {
            ui.destroy(this);
        } else if(wdg == btnToggle){
            toggle();
        } else {
            super.wdgmsg(wdg, msg, args);
        }
    }

    private final void settext(String text) {
        this.text.settext(text);
    }
}