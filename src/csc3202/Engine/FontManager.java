package csc3202.Engine;

import static csc3202.Engine.Globals.OVERLAY_FONT;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.ResourceLoader;


/**
 * Simple Font manager implemented as a Singleton
 * 
 * Instantiating a million font objects simply doesn't make sense.
 * Therefore, manage them with this class.
 * 
 * @author a9134046 - Sam Mitchell Finnigan
 * @version Nov 2012
 */
@SuppressWarnings("deprecation")
public class FontManager {

	private static FontManager instance;
	
	// Map of font size to font object
	private HashMap<Float, TrueTypeFont> fonts;
	private Font awtfont;
	
	
	/**
	 * Singleton Constructor
	 * Instantiate a FontManager, and set up the awt font to use
	 */
	private FontManager() {
		fonts = new HashMap<Float, TrueTypeFont>();
		
		try {
			InputStream inputStream = ResourceLoader.getResourceAsStream(OVERLAY_FONT);
			awtfont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Get (instantiating if need be) the font at size size_pt
	 * @param size_pt
	 * @return TrueTypeFont
	 */
	public TrueTypeFont getFont(float size_pt) {
		
		TrueTypeFont font = fonts.get(size_pt);
		
		if(font == null) {	// Then load the font!
			try {
				awtfont = awtfont.deriveFont(size_pt);
				font = new TrueTypeFont(awtfont, false);
				fonts.put(size_pt, font);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return font;
	}
	
	
	
	/**
	 * Reset all fonts (for example, on change to 
	 * fullscreen to prevent text rendering as blocks)
	 */
	public void resetFonts() {
		
		Iterator<Float> it = fonts.keySet().iterator();
		
		while(it.hasNext()) {
			Float size_pt = it.next();
			TrueTypeFont font;
			awtfont = awtfont.deriveFont(size_pt);
			font = new TrueTypeFont(awtfont, false);
			fonts.put(size_pt, font);
		}
	}
	
	
	/**
	 * Get the singleton font manager instance
	 * @return
	 */
	public static FontManager getManager() {
		
		if(instance == null) {
			instance = new FontManager();
		}
		
		return instance;
	}
}
