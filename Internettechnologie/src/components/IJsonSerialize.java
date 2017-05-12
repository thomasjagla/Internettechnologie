package components;

import java.util.*;

public interface IJsonSerialize {
	// These add Objects to the JSON String generated with getString
		public void addString(String key, String str);
		public void addInteger(String key, int num) ;
		public void addDouble(String key, double num);
		
		// Check if Object is one of String, Integer, Double
		public void addArray(String key, Map<String,Object> array);
		
		// Serialize all saved Objects and return JSON String
		public String getString();
		
		// Parse a JSON String and save all contained types internally
		public void parseString(String str);
		
		// Gets the objects contained in parseString
		public Map<String,Object> getObjects();
		
		// Gets the object of a certain JSON key
		public Object getKey(String key);
}
