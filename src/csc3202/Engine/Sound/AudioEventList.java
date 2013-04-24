package csc3202.Engine.Sound;

import java.util.ArrayList;

/** 
 * Because parameterized generics can't be inferred by instanceof at 
 * runtime, wrap the whole HashMap in an object
 * @author a9134046
 */
class AudioEventList {
    private String name;
    private ArrayList<AudioEvent> events;
    
    public AudioEventList(String name, ArrayList<AudioEvent> events) {
    	this.name = name;
    	this.events = events;
    }

    public String getName() {
    	return name;
    }
    
	public ArrayList<AudioEvent> getEvents() {
		return events;
	}
}