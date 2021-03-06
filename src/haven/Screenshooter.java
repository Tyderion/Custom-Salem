/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.image.BufferedImage;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import org.w3c.dom.*;
import java.io.*;
import java.net.*;

public class Screenshooter extends Window {
    public final URL tgt;
    public final TexI[] ss;
    private final TextEntry comment;
    private final CheckBox decobox;
    private final int w, h;
    private Label prog;
    private Coord btnc;
    private Button btn;
    private Button btnsave;
    
    public Screenshooter(Coord c, Widget parent, URL tgt, TexI[] ss) {
	super(c, Coord.z, parent, "Screenshot");
	this.tgt = tgt;
	this.ss = ss;
	this.w = Math.min(200 * ss[0].sz().x / ss[0].sz().y, 150);
	this.h = w * ss[0].sz().y / ss[0].sz().x;
	this.decobox = new CheckBox(new Coord(w, 5), this, "Include interface") {
		public void changed(boolean val) {}
	    };
	decobox.a = Config.ss_ui;
	btnc = new Coord(w + 5, h - 19);
	btn = new Button(btnc, 125, this, "Upload") {
	    public void click() {
		upload();
	    }
	};
	if(tgt == null){btn.visible = false;}
	btnsave = new Button(btnc.sub(0, 25), 125, this, "Save") {
	    public void click() {
		save();
	    }
	};
	Label clbl = new Label(new Coord(0, h + 5), this, "If you wish, leave a comment:");
	this.comment = new TextEntry(new Coord(0, clbl.c.y + clbl.sz.y + 5), new Coord(w + 130, 20), this, "");
	pack();
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == this) && (msg == "close")) {
	    ui.destroy(this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    public void cdraw(GOut g) {
	TexI tex = ss[this.decobox.a?1:0];
	g.image(tex, Coord.z, new Coord(w, h));
    }
    
    public class Uploader extends HackThread {
	private final TexI img;
	
	public Uploader(TexI img) {
	    super("Screenshot uploader");
	    this.img = img;
	}
	
	public void run() {
	    try {
		upload(img);
	    } catch(InterruptedIOException e) {
		setstate("Cancelled");
		synchronized(ui) {
		    ui.destroy(btn);
		    btn = new Button(btnc, 125, Screenshooter.this, "Retry") {
			    public void click() {
				Screenshooter.this.upload();
			    }
			};
		}
	    } catch(IOException e) {
		setstate("Could not upload image");
		synchronized(ui) {
		    ui.destroy(btn);
		    ui.destroy(btnsave);
		    btn = new Button(btnc, 125, Screenshooter.this, "Retry Upload") {
			public void click() {
			    Screenshooter.this.upload();
			}
		    };
		    if(tgt == null){btn.visible = false;}
		    btnsave = new Button(btnc.sub(0, 25), 125, Screenshooter.this, "Retry Save") {
			public void click() {
			    Screenshooter.this.save();
			}
		    };
		}
	    }
	}

	protected void setstate(String t) {
	    synchronized(ui) {
		if(prog != null)
		    ui.destroy(prog);
		prog = new Label(btnc.sub(0, 40), Screenshooter.this, t);
	    }
	}

	protected void writepng(OutputStream out, BufferedImage img, String comment) throws IOException {
	    ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
	    ImageWriter wr = ImageIO.getImageWriters(type, "PNG").next();
	    IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
	    if(comment != null) {
		Node root = dat.getAsTree("javax_imageio_1.0");
		Element cmt = new IIOMetadataNode("TextEntry");
		cmt.setAttribute("keyword", "Comment");
		cmt.setAttribute("value", comment);
		cmt.setAttribute("encoding", "utf-8");
		cmt.setAttribute("language", "");
		cmt.setAttribute("compression", "none");
		Node tlist = new IIOMetadataNode("Text");
		tlist.appendChild(cmt);
		root.appendChild(tlist);
		dat.setFromTree("javax_imageio_1.0", root);
	    }
	    ImageOutputStream iout = ImageIO.createImageOutputStream(out);
	    wr.setOutput(iout);
	    wr.write(new IIOImage(img, null, dat));
	}

	private void writejpeg(OutputStream out, BufferedImage img, String comment) throws IOException {
	    ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(img);
	    ImageWriter wr = ImageIO.getImageWriters(type, "JPEG").next();
	    IIOMetadata dat = wr.getDefaultImageMetadata(type, null);
	    if(comment != null) {
		Node root = dat.getAsTree("javax_imageio_1.0");
		Element cmt = new IIOMetadataNode("TextEntry");
		cmt.setAttribute("keyword", "comment");
		cmt.setAttribute("value", comment);
		Node tlist = new IIOMetadataNode("Text");
		tlist.appendChild(cmt);
		root.appendChild(tlist);
		dat.setFromTree("javax_imageio_1.0", root);
	    }
	    ImageOutputStream iout = ImageIO.createImageOutputStream(out);
	    wr.setOutput(iout);
	    wr.write(new IIOImage(img, null, dat));
	}

	public void upload(TexI ss) throws IOException {
	    setstate("Connecting...");
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    /* XXX: For some reason, JPEG doesn't seem to work properly. */
	    writepng(buf, ss.back, comment.text);
	    byte[] data = buf.toByteArray();
	    buf = null;
	    URLConnection conn = (URLConnection)tgt.openConnection();
	    conn.setDoOutput(true);
	    conn.addRequestProperty("Content-Type", "image/png");
	    Message auth = new Message(0);
	    auth.addstring2(ui.sess.username + "/");
	    auth.addbytes(ui.sess.sesskey);
	    conn.addRequestProperty("Authorization", "Haven " + Utils.base64enc(auth.blob));
	    conn.connect();
	    OutputStream out = conn.getOutputStream();
	    try {
		int off = 0;
		while(off < data.length) {
		    setstate(String.format("Uploading (%d%%)...", (off * 100) / data.length));
		    int len = Math.min(1024, data.length - off);
		    out.write(data, off, len);
		    off += len;
		}
	    } finally {
		out.close();
	    }
	    setstate("Awaiting response...");
	    InputStream in = conn.getInputStream();
	    final URL result;
	    try {
		if(!conn.getContentType().equals("text/x-target-url"))
		    throw(new IOException("Unexpected type of reply from server"));
		byte[] b = Utils.readall(in);
		try {
		    result = new URL(new String(b, "utf-8"));
		} catch(MalformedURLException e) {
		    throw((IOException)new IOException("Unexpected reply from server").initCause(e));
		}
	    } finally {
		in.close();
	    }
	    setstate("Done");
	    synchronized(ui) {
		ui.destroy(btn);
		ui.destroy(btnsave);
		btn = new Button(btnc, 125, Screenshooter.this, "Open in browser") {
			public void click() {
			    if(WebBrowser.self != null)
				WebBrowser.self.show(result);
			}
		    };
	    }
	}
    }
    
    public class Saver extends Uploader{

	public Saver(TexI img) {
	    super(img);
	}

	@Override
	public void upload(TexI ss) throws IOException {
	    setstate("Saving...");
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    /* XXX: For some reason, JPEG doesn't seem to work properly. */
	    writepng(buf, ss.back, comment.text);
	    byte[] data = buf.toByteArray();
	    buf = null;
	    File ssfolder = Config.getFile("screenshots");
	    if(!ssfolder.exists()){
		ssfolder.mkdirs();
	    }
	    File f = new File(ssfolder, String.format("shot_%d.png", System.currentTimeMillis()));
	    FileOutputStream fos = new FileOutputStream(f);
	    fos.write(data);
	    fos.close();
	    setstate("Done");
	    final URL result = f.toURI().toURL();
	    synchronized(ui) {
		ui.destroy(btn);
		ui.destroy(btnsave);
		btn = new Button(btnc, 125, Screenshooter.this, "Open") {
		    public void click() {
			if(WebBrowser.self != null)
			    WebBrowser.self.show(result);
		    }
		};
		ui.message("Screenshot saved");
	    }
	}

    }
    
    public void upload() {
	final Uploader th = new Uploader(Screenshooter.this.ss[decobox.a?1:0]);
	th.start();
	ui.destroy(btn);
	btn = new Button(btnc, 125, this, "Cancel") {
		public void click() {
		    th.interrupt();
		}
	    };
    }
    
    public void save() {
	final Uploader th = new Saver(Screenshooter.this.ss[decobox.a?1:0]);
	th.start();
	ui.destroy(btn);
	ui.destroy(btnsave);
	btn = new Button(btnc, 125, this, "Cancel") {
		public void click() {
		    th.interrupt();
		}
	    };
    }

    public static void take(final GameUI gameui, final URL tgt) {
	new Object() {
	    TexI[] ss = {null, null};
	    {
		if(gameui != null){
		    gameui.map.delay2(new MapView.Delayed() {
			public void run(GOut g) {
			    ss[0] = new TexI(g.getimage(Coord.z, g.sz));
			    ss[0].minfilter = javax.media.opengl.GL.GL_LINEAR;
			    checkcomplete();
			}
		    });
		    gameui.ui.drawafter(new UI.AfterDraw() {
			public void draw(GOut g) {
			    ss[1] = new TexI(g.getimage(Coord.z, g.sz));
			    checkcomplete();
			}
		    });
		}
	    }
	    
	    private void checkcomplete() {
		if((ss[0] != null) && (ss[1] != null)) {
		    Screenshooter s = new Screenshooter(new Coord(100, 100), gameui, tgt, ss);
		    if(Config.ss_slent){
			s.visible = false;
			s.save();
			s.wdgmsg(s, "close", null);
		    }
		}
	    }
	};
    }
}
