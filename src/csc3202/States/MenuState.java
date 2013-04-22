
package csc3202.States;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.TextureImpl;

import csc3202.Engine.*;
import csc3202.Engine.Interfaces.GameState;

/**
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
@SuppressWarnings("deprecation")
public class MenuState implements GameState {

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

	private static final int TEXT_OFFSET = -65;
	private static final int SPACING = 45;
	private static final int DELAY = 1000; 					// Model appearification delay (ms)
	private static final int TXT_DELAY = 500;				// Text delay after model appears
	
	private static final String START_TXT = "Press Space to start";
	
	private static final long cycle_ms = 10000;				// 10 seconds
	
	private ArrayList<MenuPage> pages;
	private Engine engine;
	private GameData data;
	private int current_page;
	private long last_change;
	
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
		
		return this;
	}
	
	

	@Override
	public int update(long delta) {
		
		if(System.currentTimeMillis() > (last_change + cycle_ms)) {
			// Change to next menu
			current_page++;
			current_page %= pages.size();
			pages.get(current_page).reset();
			last_change = System.currentTimeMillis();
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
				
				TextureImpl.bindNone();								// Release bindings for other textures (i.e. models)
				glEnable(GL_TEXTURE_2D);
				glEnable(GL_BLEND);									// Enable alpha blending
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);	
				
				// Flash "Press Space To Start" every half-second
				if((System.currentTimeMillis() / 500) % 2 == 0)
					FontManager.getManager().getFont(24f)
						.drawString(Globals.window_width/2 - (START_TXT.length() * Globals.FONT_24PT / 2),
									(Globals.window_height / 2) + 200, START_TXT);
				
				pages.get(current_page).render();
			glPopMatrix();
				
			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		return SUCCESS;
	}

	

	@Override
	public void keyInput(int key) {
		
		if (Keyboard.getEventKeyState()) {

			switch (key) {
			case Keyboard.KEY_SPACE:								// Start game
				engine.changeState(new RunState(data).init(engine));
				engine.pushState(new OverlayState(data).init(engine));
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

	
	
	///////////////////////////////////////////////////////////////////////////////////
	// MENU PAGES
	
	/**
	 * Title page
	 */
	private class TitlePage implements MenuPage {

		private static final String TITLE = "A WASTE OF SPACE";
		
		private final String[] messages = {  
				"Controls", 
				"Move: WASD / Arrow keys", 
				"Fire: Space", 
				"Change View: V",
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
			FontManager.getManager().getFont(48f)
				.drawString(Globals.window_width/2 - (TITLE.length() * Globals.FONT_48PT / 2), 
							(Globals.window_height / 2) - 200, 
							TITLE, Color.red
						);
			
			for(int i=0; i<messages.length; i++) {
				if(now > (trigger_time + (DELAY * (i)) + TXT_DELAY)) {
					FontManager.getManager().getFont(24f)
						.drawString(
								Globals.window_width/2 - (messages[i].length() * Globals.FONT_24PT / 2),
								(Globals.window_height / 2) + (TEXT_OFFSET + (SPACING * i)), 
								messages[i],
								(i==0) ? Color.green : Color.white
							);
				}
			}
		}



		@Override
		public void reset() {
			trigger_time = System.currentTimeMillis();
		}
	}
	
	
	
	/**
	 * High-score display
	 */
	private class HighScorePage implements MenuPage {

		private static final String TITLE = "HIGH SCORES";

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
					    
				
			FontManager.getManager().getFont(48f)
				.drawString(Globals.window_width/2 - (TITLE.length() * Globals.FONT_48PT / 2), 
							(Globals.window_height / 2) - 200, TITLE, Color.red);
			
			
			int i=0;
			int limit=4;
			if(now > (trigger_time + (cycle_ms / 2))) {
				i=4;
				limit=8;
			}
			
			for(; i<limit; i++) {
				
				FontManager.getManager().getFont(24f).drawString(
						Globals.window_width/2 - 100, 
						(Globals.window_height / 2) + (TEXT_OFFSET + (SPACING * (i % 4))), 
						scores.getName(i)
					);
				
				if( now < (trigger_time + cycle_ms/2) ) {
					
					// First 4 scores
					if( now > (trigger_time + (DELAY/2) * (i+1)) ) {
						
						FontManager.getManager().getFont(24f)
							.drawString(
									Globals.window_width/2, 
									(Globals.window_height / 2) + TEXT_OFFSET + (SPACING * i), 
									Integer.toString(scores.getScore(i)),
									Color.green
								);
					}
				} else {
					// Scores 5-8
					if( now > (trigger_time + (cycle_ms / 2) + (DELAY/2) * (i%4)) ) {
						
						FontManager.getManager().getFont(24f)
							.drawString(
									Globals.window_width/2, 
									(Globals.window_height / 2) + (TEXT_OFFSET + (SPACING * (i % 4))), 
									Integer.toString(scores.getScore(i)),
									Color.green
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
	
	
	
	/**
	 * Credits page
	 */
	private class CreditsPage implements MenuPage {

		private static final String TITLE = "DEVELOPED BY";
		private final String[] TEXT = {
			"Sam Mitchell Finnigan",
			"091340463 - Newcastle University",
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
			
			FontManager.getManager().getFont(48f)
				.drawString(Globals.window_width/2 - (TITLE.length() * Globals.FONT_48PT / 2), 
							(Globals.window_height / 2) - 200, TITLE, Color.red);
			
			FontManager.getManager().getFont(36f)
			.drawString(Globals.window_width/2 - (TEXT[0].length() * Globals.FONT_36PT / 2),
						(Globals.window_height / 2) - 80, TEXT[0], Color.green);
			
			for(int i=1; i<TEXT.length; i++) {
				if(now > (trigger_time + (DELAY * i)))
					FontManager.getManager().getFont(24f)
						.drawString(Globals.window_width/2 - (TEXT[i].length() * Globals.FONT_24PT / 2),
									(Globals.window_height / 2) + (TEXT_OFFSET + 30 + (SPACING * i)), TEXT[i]);
			}
		}

		
		
		@Override
		public void reset() {
			trigger_time = System.currentTimeMillis();
		}
	}



	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
}
