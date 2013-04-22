package csc3202.Engine.Sound;

import org.vamp_plugins.*;

/**
 * Onset analysis test
 * @author sam
 *
 */
public class Analyser {

	public Analyser() {
		
		PluginLoader loader = PluginLoader.getInstance();
		String[] plugins = loader.listPlugins();
		
		if(plugins.length == 0)
			System.out.println("jVamp plugins missing!");
		
		for(String s : plugins)
			System.out.println(s);
		
		
	}

	/** Unit test **/
	public static void main(String[] args) {
		new Analyser();
	}
}
