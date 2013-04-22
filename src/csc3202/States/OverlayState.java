package csc3202.States;

import static org.lwjgl.opengl.GL11.*;
import static csc3202.Engine.Globals.*;

import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.TextureImpl;

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
	
	private GameData data;
	
	private Engine engine;
	
	/**
	 * Construct the overlay
	 */
	public OverlayState(GameData data) {
		
		this.data = data;
	}
	
	@Override
	public GameState init(Engine engine) {
		
		this.engine = engine;
		
		return this;
	}
	
	
	@Override
	public int update(long delta) {
		return SUCCESS;
	}
	
	

	@Override
	public int render() {

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
			engine.set2D();
			
			glMatrixMode(GL_MODELVIEW);

		    glDisable(GL_TEXTURE_2D);
		    
			renderBorders();
			renderLives();
	
			// Render text
		    glEnable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);									// Enable alpha blending
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			
			TextureImpl.bindNone();								// Release bindings for other textures (i.e. models) in Slick
		    
		    glPushMatrix();
			    glScalef(1.0f, -1.0f, 1.0f);
				glTranslatef(0.0f, -Globals.window_height, 1.0f);
				
				FontManager.getManager().getFont(24f)
						.drawString(Globals.window_width-300, BORDER_WIDTH/2 -10, "SCORE: " + data.getScore(), Color.white);
				FontManager.getManager().getFont(24f)
						.drawString(BORDER_WIDTH + 10, BORDER_WIDTH/2 -10, "LIVES: ");
			    
			    if(data.getLives() >= 0) {
			    	FontManager.getManager().getFont(24f)
			    			.drawString(BORDER_WIDTH + 135, BORDER_WIDTH/2 -10, "" + data.getLives());
			    	FontManager.getManager().getFont(24f)
			    			.drawString(Globals.window_width-300, BORDER_WIDTH/2 -10, "SCORE: " + data.getScore(), Color.white);
			    } else {
			    	FontManager.getManager().getFont(24f)
			    		.drawString(BORDER_WIDTH + 135, BORDER_WIDTH/2 -10, "0");	// Prevent -1 lives being seen by user
			    }
			    
			    if(data.isPaused()) {
			    	FontManager.getManager().getFont(24f)
			    		.drawString(
			    				Globals.window_width/2 - ("paused".length() * FONT_24PT) /2, 
			    				BORDER_WIDTH/2 -10, 
			    				"paused", 
			    				Color.red
			    			);

			    	final String cont1 = "Press 'P' to resume";
			    	FontManager.getManager().getFont(18f)
			    		.drawString(
			    				Globals.window_width/2 - (cont1.length() * FONT_18PT) /2, 
			    				Globals.window_height - BORDER_WIDTH, 
			    				cont1, 
			    				Color.green
			    			);
			    }
			    
			    
				glDisable(GL_TEXTURE_2D);
			glPopMatrix();
			
			
			glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		
	    
		return SUCCESS;
	}
	
	
	
	public void renderBorders() {

		// Coloured border edges (blue/green)
		glPushMatrix();
			// Render the top separator (blue)
			glColor3f( 0, 0, 255 );
			glTranslatef(BORDER_WIDTH, Globals.window_height-BORDER_WIDTH, 1.0f);
			glBegin( GL_LINES );
				glVertex3f( 0.0f, 0.0f, 0.0f );
				glVertex3f((Globals.window_width - BORDER_WIDTH * 2), 0.0f, 0.0f);
			glEnd();
		glPopMatrix();

		glPushMatrix();
			// Render the bottom separator (green)
			glColor3f( 0, 255, 0 );
			glTranslatef(BORDER_WIDTH, BORDER_WIDTH, 1.0f);
			glBegin( GL_LINES );
				glVertex3f( 0.0f, 0.0f, 0.0f );
				glVertex3f( (Globals.window_width - BORDER_WIDTH*2), 0.0f, 0.0f);
			glEnd();
		glPopMatrix();
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
