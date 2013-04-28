package csc3202.States;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

import csc3202.Engine.*;
import csc3202.Engine.Interfaces.GameState;


/**
 * A score/lives display overlay
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
@SuppressWarnings("deprecation")
public class OverlayState implements GameState {
	
	DecimalFormat scoreFormatter = new DecimalFormat("0000");
	
	private GameData data;
	
	private Engine engine;
	
	private Date remaining;
	
	private Texture bomb = null;

	private long elapsed;

	private long startTime;
	
	/**
	 * Construct the overlay
	 */
	public OverlayState(GameData data) {
		
		this.data = data;
		this.remaining = new Date();	// For formatting time remaining
	}
	
	@Override
	public GameState init(Engine engine) {
		
		startTime = System.currentTimeMillis();
		this.engine = engine;
		try {
			bomb = TextureLoader.getTexture("PNG", new FileInputStream("res/bomb.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	
	@Override
	public int update(long delta) {
		
		if(data.isPaused()) {
			// Maintain song start time in paused state
			if(elapsed == 0l) {
				elapsed = System.currentTimeMillis() - startTime;
			} else {
				data.setStartTime(System.currentTimeMillis() - elapsed);				// Maintain step with MP3 by incrementing start by time paused 
			}
		} else {
			elapsed = 0l;
		}
		return SUCCESS;
	}
	
	

	@Override
	public int render() {

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
			engine.set2D();
			
			glMatrixMode(GL_MODELVIEW);

		    glDisable(GL_TEXTURE_2D);
		    
			renderProgress();
			renderLives();
	
			// Render text
		    glEnable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);									// Enable alpha blending
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			
			TextureImpl.bindNone();								// Release bindings for other textures (i.e. models) in Slick
		    
			TrueTypeFont f24 = FontManager.getManager().getFont(24f);
			
		    glPushMatrix();
			    glScalef(1.0f, -1.0f, 1.0f);
				glTranslatef(0.0f, -Globals.window_height, 1.0f);
				
				// Draw Score and Lives
				f24.drawString(
						Globals.window_width-200,
						BORDER_WIDTH/2 -10, 
						"SCORE: " + scoreFormatter.format(data.getScore())
					);
				f24.drawString(
						BORDER_WIDTH + 10, 
						BORDER_WIDTH/2 -10, 
						"LIVES: " + ((data.getLives() >= 0) ? data.getLives() : 0)
					);
				f24.drawString(
						Globals.window_width-350,
						BORDER_WIDTH/2 -10, 
						"MULT: x" + data.getMultiplier()
					);
				
				// Calculate and format time remaining
				if(data.getStartTime() > 0) {
				    
				    // Draw artist and title of song
					f24.drawString(
							BORDER_WIDTH,
							Globals.window_height - BORDER_WIDTH + 10, 
							data.getAnalysis().get(0).getArtist() + " - " +
							data.getAnalysis().get(0).getTitle()
						);
					if(! data.isPaused())
						remaining.setTime(
								data.getAnalysis().get(0).getDuration()
								 - (System.currentTimeMillis()
								 - data.getStartTime())
							);
					f24.drawString(
							Globals.window_width - 120,
							Globals.window_height - BORDER_WIDTH + 10,
							new SimpleDateFormat("mm:ss").format(remaining)
						);
				}
				
			    // Draw paused
			    if(data.isPaused()) {
			    	f24.drawString(
		    				Globals.window_width/2 - ("PAUSED".length() * FONT_24PT) /2, 
		    				BORDER_WIDTH/2 -10, 
		    				"PAUSED", 
		    				Color.red
		    			);

			    	final String cont1 = "Press 'P' to resume";
			    	FontManager.getManager().getFont(18f)
			    		.drawString(
			    				Globals.window_width/2 - (cont1.length() * FONT_18PT) /2, 
			    				Globals.window_height - BORDER_WIDTH + 10, 
			    				cont1, 
			    				Color.orange
			    			);
			    }

			    
			    // Bomb text
			    f24.drawString(
						Globals.window_width - 350,
						Globals.window_height - BORDER_WIDTH + 10,
						"Bombs:"
					);
				
			    // Draw scaled bombs
				final float scale = 0.2f;
				final float x_start = Globals.window_width - 260;
				final float spacing = 5f;
				final float y = Globals.window_height - BORDER_WIDTH + 10;
			    for(int i=0; i<data.getBombs(); i++) {
			    	float x = x_start + (i * bomb.getTextureWidth() * scale) + spacing;
			    	
					// Render Logo
					glPushMatrix();
						Color.white.bind();
						bomb.bind();
				        
				        glBegin(GL_QUADS);
							glTexCoord2f(0,0);
							glVertex2f(
									x,
									y
								);
							glTexCoord2f(1,0);
							glVertex2f(
									x + bomb.getTextureWidth() * scale, 
									y
								);
							glTexCoord2f(1,1);
							glVertex2f(
									x + bomb.getTextureWidth()  * scale, 
									y + bomb.getTextureHeight() * scale
								);
							glTexCoord2f(0,1);
							glVertex2f(
									x, 
									y + bomb.getTextureHeight() * scale
								);
				        glEnd();
					glPopMatrix();
			    }
			    
				glDisable(GL_TEXTURE_2D);
			glPopMatrix();
			
			
			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		
	    
		return SUCCESS;
	}
	
	
	
	public void renderProgress() {

		float length = (Globals.window_width - BORDER_WIDTH*2);
		
		glPushAttrib(GL_LINE_BIT);
		// Render elapsed time (orange over white)
		glPushMatrix();
			// Render the background for the meter
			Color.darkGray.bind();
			glTranslatef(BORDER_WIDTH, BORDER_WIDTH, 1.0f);
			glLineWidth(1f);
			glBegin( GL_LINES );
				glVertex3f( 0.0f, 0.0f, -1.0f );
				glVertex3f((Globals.window_width - BORDER_WIDTH * 2), 0.0f, -1.0f);
			glEnd();
		glPopMatrix();

		if(data.getStartTime() > 0) {
			long elapsed = (System.currentTimeMillis() - data.getStartTime());
			long duration = data.getAnalysis().get(0).getDuration();
			float progress = (elapsed / (float) duration);
	
			glPushMatrix();
				// Render the progress meter (orange)
				Color.orange.bind();
				glLineWidth(2f);
				glTranslatef(BORDER_WIDTH, BORDER_WIDTH-2, 1.0f);
				glBegin( GL_LINES );
					glVertex3f( 0.0f, 0.0f, 0.0f );
					glVertex3f( progress * length , 0.0f, 0.0f);
				glEnd();
			glPopMatrix();
		}
		glPopAttrib();
	}
	
	
	/**
	 * Render ship model as lives remaining
	 */
	private void renderLives() {
		
		if(data.getLives() > 0) {
			// Render lives on-screen
			for(int i=1; i<=data.getLives(); i++) {
				glPushMatrix();
					glTranslatef(220, Globals.window_height - 40.0f, 0.0f);
					glScalef(0.4f, 0.4f, 1.0f);
//					ship_model.render((ship_model.getWidth()+20.0f) * i, 0.0f, 1.0f);
				glPopMatrix();
			}
		}
	}

	
	
	@Override
	public void keyInput(int key) { }

	
	
	@Override
	public void mouseInput(int x, int y, boolean leftDown, boolean rightDown) { }

	
	
	@Override
	public void pause() { }

	
	
	@Override
	public void resume() { }



	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}
	
}
