package csc3202.Engine.Interfaces;

/**
 * Register texturable objects in the Engine so that they can be
 * reloaded on full-screen toggle
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Oct 2012
 */
public interface Texturable {

	/** Load (or re-load) the textures **/
	public abstract void loadTextures();
}