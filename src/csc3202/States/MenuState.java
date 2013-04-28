
package csc3202.States;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

import csc3202.Engine.*;
import csc3202.Engine.Interfaces.GameState;

/**
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
@SuppressWarnings("deprecation")
public class MenuState implements GameState, Observer {

	/**
	 * A simple, updatable menu page
	 * @author a9134046 - Sam Mitchell Finnigan
	 * @version Nov 2012
	 */
	public interface MenuPage {
		
		MenuPage init();
		void render();
		void reset();
	}

	private static final int TEXT_OFFSET = -35;
	private static final int SPACING = 45;
	private static final int DELAY = 1000; 					// Model appearification delay (ms)
	private static final int TXT_DELAY = 500;				// Text delay after model appears
	
	private static final String START_TXT = "> Press Space to Start <";
	private static final String CHOOSING_TXT = "opening file dialog...";
	
	private static final long cycle_ms = 10000;				// 10 seconds
	
	private ArrayList<MenuPage> pages;
	private Engine engine;
	private GameData data;
	private int current_page;
	private long last_change;
	
	private String file;	// MP3 file to load
	private boolean asking;
	
	private TrueTypeFont f24 = null;
	private Texture logo = null;
	
	/**
	 * 
	 */
	public MenuState(GameData data) {
		pages = new ArrayList<MenuPage>();
		this.data = data;
	}

	
	
	@Override
	public GameState init(Engine engine) {

		this.engine = engine;
		this.last_change = System.currentTimeMillis();

		pages.add( new TitlePage().init() );
		pages.add( new HighScorePage(data.getHighScores()).init() );
		pages.add( new CreditsPage().init() );

		try {
			logo = TextureLoader.getTexture("PNG", new FileInputStream("res/logo.png"));
			f24 = FontManager.getManager().getFont(24f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	

	@Override
	public int update(long delta) {

		// Filechooser has returned- set up a new state
		if (file != null) {
			data.setMp3File(file);
			engine.changeState(new LoaderState(data).init(engine));
			return SUCCESS;
		}
		
		if(System.currentTimeMillis() > (last_change + cycle_ms)) {
			// Change to next menu
			current_page++;
			current_page %= pages.size();
			pages.get(current_page).reset();
			last_change = System.currentTimeMillis();
		}
		
		if(asking) {
			try {
				Thread.sleep(100);												// I don't know why FileChooser is so slow!
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return SUCCESS;
	}
	
	

	@Override
	public int render() {
		
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
			engine.set2D();

			glMatrixMode(GL_MODELVIEW);
			glPushMatrix();
			
				glScalef(1.0f, -1.0f, 1.0f);
				glTranslatef(0.0f, -Globals.window_height, 1.0f);
				
				TextureImpl.bindNone();											// Release bindings for other textures (i.e. models)
				glEnable(GL_TEXTURE_2D);
				glEnable(GL_BLEND);												// Enable alpha blending
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);	
				
				if(asking) {
					f24.drawString(												// Flash "Press Space To Start" every half-second
							Globals.window_width/2 - (CHOOSING_TXT.length() * Globals.FONT_24PT / 2),
							(Globals.window_height / 2) + 200, 
							CHOOSING_TXT,
							Color.darkGray
						);
				} 
				else {
					f24.drawString(												// Flash "Press Space To Start" every half-second
							Globals.window_width/2 - (START_TXT.length() * Globals.FONT_24PT / 2),
							(Globals.window_height / 2) + 200, 
							START_TXT,
							((System.currentTimeMillis() / 500) % 2 == 0) ? Color.orange : Color.darkGray
						);
				}
				
				render_logo();
				pages.get(current_page).render();
			glPopMatrix();
				
			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		return SUCCESS;
	}
	
	
	private void render_logo() {
		
		final float logo_scale = 0.5f;
		final float logo_x = Display.getWidth() /2 - (logo.getTextureWidth()*logo_scale) /2;	// Center
		final float logo_y = 80.0f;

		// Render Logo
		glPushMatrix();
			Color.white.bind();
			logo.bind();
	        
	        glBegin(GL_QUADS);
				glTexCoord2f(0,0);
				glVertex2f(
						logo_x,
						logo_y
					);
				glTexCoord2f(1,0);
				glVertex2f(
						logo_x + logo.getTextureWidth() * logo_scale, 
						logo_y
					);
				glTexCoord2f(1,1);
				glVertex2f(
						logo_x + logo.getTextureWidth()  * logo_scale, 
						logo_y + logo.getTextureHeight() * logo_scale
					);
				glTexCoord2f(0,1);
				glVertex2f(
						logo_x, 
						logo_y + logo.getTextureHeight() * logo_scale
					);
	        glEnd();
		glPopMatrix();
	}

	

	@Override
	public void keyInput(int key) {
		
		if (Keyboard.getEventKeyState()) {

			switch (key) {
			case Keyboard.KEY_SPACE:											// Start game
				if(!asking) {
					asking = true;
					
			        // create an event source 
					final EventSource eventSource = new EventSource();
					
					// subscribe the observer to the event source
					eventSource.addObserver(this);
	
					// start the event thread
					Thread thread = new Thread(eventSource);
					thread.setPriority(Thread.NORM_PRIORITY + 1);
					thread.start();
				}
				break;
			case Keyboard.KEY_ESCAPE:
				engine.stop();
				break;
			default:
				break;
			}
		}
	}
	
	
	
	@Override
	public void mouseInput(int x, int y, boolean leftDown, boolean rightDown) { }

	@Override
	public void pause() { }

	@Override
	public void resume() { }

	
	
	////////////////////////////////////////////////////////////////////////////
	// MENU PAGES
	
	/***************************************************************************
	 * Title page
	 */
	private class TitlePage implements MenuPage {
		
		private final String[] messages = {  
				"Controls", 
				"Move: WASD / Mouse", 
				"Fire: Space",
				"Pause: P" 
		};

		// Delay entry by n seconds
		private long trigger_time;
		
		
		@Override
		public MenuPage init() {
			
			trigger_time = System.currentTimeMillis();
			
			return this;
		}
		
		
		
		@Override
		public void render() {
			
			long now = System.currentTimeMillis();
			
			// RENDER TEXT
//			f24.drawString(Globals.window_width/2 - (TITLE.length() * Globals.FONT_48PT / 2), 
//							(Globals.window_height / 2) - 200, 
//							TITLE, Color.red
//						);
			
			for(int i=0; i<messages.length; i++) {
				if(now > (trigger_time + (DELAY * (i)) + TXT_DELAY)) {
					f24.drawString(
								Globals.window_width/2 - (messages[i].length() * Globals.FONT_24PT / 2),
								(Globals.window_height / 2) + (TEXT_OFFSET + (SPACING * i)), 
								messages[i],
								(i==0) ? Color.orange : Color.white
							);
				}
			}
		}



		@Override
		public void reset() {
			trigger_time = System.currentTimeMillis();
		}
	}
	
	
	
	/**************************************************************************
	 * High-score display
	 */
	private class HighScorePage implements MenuPage {

//		private static final String TITLE = "HIGH SCORES";

		private HighScores scores;
		private long trigger_time;
		
		public HighScorePage(HighScores scores) {
			this.scores = scores;
		}
		
		@Override
		public MenuPage init() {
			trigger_time = System.currentTimeMillis();
			return this;
		}

		
		
		@Override
		public void render() {
			
			long now = System.currentTimeMillis();
					    
				
//			FontManager.getManager().getFont(48f)
//				.drawString(Globals.window_width/2 - (TITLE.length() * Globals.FONT_48PT / 2), 
//							(Globals.window_height / 2) - 200, TITLE, Color.red);
			
			
			int i=0;
			int limit=4;
			if(now > (trigger_time + (cycle_ms / 2))) {
				i=4;
				limit=8;
			}
			
			for(; i<limit; i++) {
				
				f24.drawString(
						Globals.window_width/2 - 100, 
						(Globals.window_height / 2) + (TEXT_OFFSET + (SPACING * (i % 4))), 
						scores.getName(i)
					);
				
				if( now < (trigger_time + cycle_ms/2) ) {
					
					// First 4 scores
					if( now > (trigger_time + (DELAY/2) * (i+1)) ) {
						
						f24.drawString(
									Globals.window_width/2, 
									(Globals.window_height / 2) + TEXT_OFFSET + (SPACING * i), 
									Integer.toString(scores.getScore(i)),
									Color.orange
								);
					}
				} else {
					// Scores 5-8
					if( now > (trigger_time + (cycle_ms / 2) + (DELAY/2) * (i%4)) ) {
						
						f24.drawString(
									Globals.window_width/2, 
									(Globals.window_height / 2) + (TEXT_OFFSET + (SPACING * (i % 4))), 
									Integer.toString(scores.getScore(i)),
									Color.orange
								);
					}
				}
				
			}
				
		}



		@Override
		public void reset() {
			trigger_time = System.currentTimeMillis();
		}
	}
	
	
	
	/**************************************************************************
	 * Credits page
	 */
	private class CreditsPage implements MenuPage {

//		private static final String TITLE = "DEVELOPED BY";
		
		private final String[] TEXT = {
			"Sam Mitchell Finnigan",
			"Newcastle University",
			"2013",
		};

		private long trigger_time;
		
		@Override
		public MenuPage init() {
			trigger_time = System.currentTimeMillis();
			return this;
		}
		
		

		@Override
		public void render() {
			
			long now = System.currentTimeMillis();
			
//			FontManager.getManager().getFont(48f)
//				.drawString(Globals.window_width/2 - (TITLE.length() * Globals.FONT_48PT / 2), 
//							(Globals.window_height / 2) - 200, TITLE, Color.red);
			
			FontManager.getManager().getFont(36f)
			.drawString(
					Globals.window_width/2 - (TEXT[0].length() * Globals.FONT_36PT / 2),
					(Globals.window_height / 2) - 20, 
					TEXT[0], 
					Color.lightGray
				);
			
			for(int i=1; i<TEXT.length; i++) {
				if(now > (trigger_time + (DELAY * i)))
					f24.drawString(
							Globals.window_width/2 - (TEXT[i].length() * Globals.FONT_24PT / 2),
							(Globals.window_height / 2) + (TEXT_OFFSET + 30 + (SPACING * i)), 
							TEXT[i]
						);
			}
		}

		
		
		@Override
		public void reset() {
			trigger_time = System.currentTimeMillis();
		}
	}



	@Override
	public void cleanup() { }
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// File chooser observer pattern stuff

	
	public void update(Observable obj, Object arg) {
        if (arg instanceof String) {
            file = (String) arg;
        }
        asking = false;
	}
	
	
	private class EventSource extends Observable implements Runnable {
	    @Override
	    public void run() {
	    	System.out.println(">> FileChooser thread started");
			String response = showFileChooser();
			setChanged();
			notifyObservers(response);
	    	System.out.println("<< FileChooser thread exit");
	    }
		
		
		// Show swing dialog and get a file choice from the user
		private String showFileChooser() {
			
			final String lastDir = ".lastDir";
			try {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
			    }
				
				// Get last directory used
				File directory = null;
				try {
					BufferedReader fis = new BufferedReader(new InputStreamReader(
							new FileInputStream(lastDir), Charset.forName("UTF-8")));
					directory = new File(fis.readLine());
					fis.close();
				}
				catch (IOException e) {	System.out.println("No last dir"); } // OK no file
				
				// Show a file-chooser
				JFileChooser chooser = new JFileChooser(
						(lastDir == null) ? new java.io.File("~").getCanonicalPath()
								 			: directory.getCanonicalPath());
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("MP3 Files", "mp3");
			    
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    
			    // Select file and remember which directory
			    if(returnVal == JFileChooser.APPROVE_OPTION) { 
			    	File file = chooser.getSelectedFile();
			    	String dir = file.getParent();
			    	
			    	FileWriter fos = new FileWriter(lastDir);
					fos.write(dir);
					fos.close();
					
			    	return file.getCanonicalPath();
			    }
			    
			} catch ( ClassNotFoundException | InstantiationException
					| IllegalAccessException | UnsupportedLookAndFeelException 
					| IOException e) {

				e.printStackTrace();
			}
		    return null;
		}
	}
}
