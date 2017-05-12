package components;

import java.util.*;

public class JsonSerializer implements IJsonSerialize {

	Map<String, String> stringObjects = null;
	Map<String, Integer> integerObjects = null;
	Map<String, Double> doubleObjects = null;
	Map<String, Map<String, Object>> arrayObjects = null;

	public JsonSerializer() {
		stringObjects = new HashMap();
		integerObjects = new HashMap();
		doubleObjects = new HashMap();
		arrayObjects = new HashMap();
	}

	private boolean uniqueKey(String key) {	//Check if key is free in existing json
		boolean valid = true;
			for (Map.Entry<String, String> entry : stringObjects.entrySet()) {
				if (entry.getKey().equals(key))
					valid = false;
			}
			for (Map.Entry<String, Integer> entry : integerObjects.entrySet()) {
				if (entry.getKey().equals(key))
					valid = false;
			}
			for (Map.Entry<String, Double> entry : doubleObjects.entrySet()) {
				if (entry.getKey().equals(key))
					valid = false;
			}
			for (Map.Entry<String, Map<String, Object>> entry : arrayObjects.entrySet()) {
				if (entry.getKey().equals(key))
					valid = false;
			}
		return valid;
	}

	private boolean correctArrayTypes(Map<String, Object> array) {	//checks values in a Map: fine if String/Integer/Double 
		boolean valid = true;
		for (Map.Entry<String, Object> entry : array.entrySet()) {
			if (entry.getValue().getClass() != String.class && entry.getValue().getClass() != Integer.class
					&& entry.getValue().getClass() != Double.class)
				valid = false;
		}
		return valid;
	}

	private int countOccurence(String text, char toCount) {	//Counts the character occurences in a String
		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == toCount)
				count++;
		}
		return count;
	}

	@Override
	public void addString(String key, String str) {
		if (uniqueKey(key))
			stringObjects.put(key, str);
	}

	@Override
	public void addInteger(String key, int num) {
		if (uniqueKey(key))
			integerObjects.put(key, num);
	}

	@Override
	public void addDouble(String key, double num) {
		if (uniqueKey(key))
			doubleObjects.put(key, num);
	}

	@Override
	public void addArray(String key, Map<String, Object> array) {
		if (correctArrayTypes(array) && uniqueKey(key))
			arrayObjects.put(key, array);

	}

	@Override
	public String getString() {
		String json = "JSON:\n{";

		// Strings
		for (Map.Entry<String, String> entry : stringObjects.entrySet()) {
			String newEntry = "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"";
			json = json + newEntry;
			json = json + ", ";
		}

		// Integer
		for (Map.Entry<String, Integer> entry : integerObjects.entrySet()) {
			String newEntry = "\"" + entry.getKey() + "\": " + entry.getValue();
			json = json + newEntry;
			json = json + ", ";
		}

		// Double
		for (Map.Entry<String, Double> entry : doubleObjects.entrySet()) {
			String newEntry = "\"" + entry.getKey() + "\": " + entry.getValue();
			json = json + newEntry;
			json = json + ", ";
		}

		// Array
		for (Map.Entry<String, Map<String, Object>> entry : arrayObjects.entrySet()) {
			String newEntry = "\"" + entry.getKey() + "\": {";
			for (Map.Entry<String, Object> innerEntry : entry.getValue().entrySet()) {
				newEntry = newEntry + "\"" + innerEntry.getKey() + "\": ";
				if (innerEntry.getValue().getClass() == String.class) {
					newEntry = newEntry + "\"" + innerEntry.getValue() + "\"";
				} else {
					newEntry = newEntry + innerEntry.getValue();
				}
				newEntry = newEntry + ", ";
			}
			newEntry = newEntry.substring(0, newEntry.length() - 2);
			newEntry = newEntry + "}";
			json = json + newEntry;
			json = json + ", ";
		}

		json = json.substring(0, json.length() - 2);
		json = json + " }";
		return json;
	}

	@Override
	public void parseString(String str) {
		
		str = str.trim();
		str = str.replace(" ", "").replace("\n", "").replace("\r", "");
		str = str.substring(1, str.length() - 1);
		Scanner sc = new Scanner(str);
		sc.useDelimiter(",");	//Splits formatted String
		while (sc.hasNext()) {	
			String nextPart = sc.next();
			if (nextPart.contains("{")) {	//contains { -> must be an array
				while (!nextPart.contains("}")) {
					nextPart = nextPart + "," + sc.next();	//combines parts of an array
				}
			}
			String key = nextPart.substring(0, nextPart.indexOf(":") - 1);	
			String value = nextPart.substring(nextPart.indexOf(":") + 1, nextPart.length());
			key = key.replace("\"", "");
			if (!value.contains("{")) {		//not an array
				if (value.contains(".")) {	//must be double
					doubleObjects.put(key, Double.valueOf(value));
				} else if (countOccurence(nextPart, '"') == 2) {	//must be integer
					integerObjects.put(key, Integer.valueOf(value));
				} else {	//else string
					stringObjects.put(key, value.replace("\"", ""));
				}
			} else {	//array
				value = value.replace("{", "").replace("}", "");
				Map<String, Object> newArray = new HashMap();
				Scanner arrayScanner = new Scanner(value);
				arrayScanner.useDelimiter(",");	//splits array in Map entries 

				while (arrayScanner.hasNext()) {	//same detection as without arrays
					String nextArray = arrayScanner.next();
					String arrayKey = nextArray.substring(1, nextArray.indexOf(":") - 1);
					String arrayValue = nextArray.substring(nextArray.indexOf(":") + 1, nextArray.length());
					if (arrayValue.contains(".")) {
						newArray.put(arrayKey, Double.valueOf(arrayValue));
					} else if (countOccurence(arrayValue, '"') < 2) {
						newArray.put(arrayKey, Integer.valueOf(arrayValue));
					} else {

						newArray.put(arrayKey, arrayValue.replace("\"", ""));
					}
				}
				arrayObjects.put(key, newArray);
			}

		}
	}

	@Override
	public Map<String, Object> getObjects() {
		Map<String, Object> allObjects = new HashMap();
		for(Map.Entry<String, String> entry: stringObjects.entrySet()) {
			allObjects.put(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<String, Integer> entry: integerObjects.entrySet()) {
			allObjects.put(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<String, Double> entry: doubleObjects.entrySet()) {
			allObjects.put(entry.getKey(), entry.getValue());
		}
		for(Map.Entry<String, Map<String, Object>> entry: arrayObjects.entrySet()) {
			allObjects.put(entry.getKey(), entry.getValue());
		}
		return allObjects;
	}

	@Override
	public Object getKey(String key) {
		for(Map.Entry<String, Object> entry: getObjects().entrySet()) {
			if(entry.getKey().equals(key))return entry.getValue();
		}
		return null;
	}

	///////////////////////////////////TESTING////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		System.out.println("JSON-TEST:");
		JsonSerializer js = new JsonSerializer();
//		 Map<String, Object> m = new HashMap();
//		 Map<String, Object> m2 = new HashMap();
//		 m.put("array1", "fun1");
//		 m.put("array2", 4);
//		 m.put("array3", 3.2);
//		 m2.put("array1", "fun1");
//		 m2.put("array2", 4);
//		 m2.put("array3", 3.2);
//		 js.addString("nummer", "blob");
//		 js.addString("nummer2", "blob");
//		 js.addArray("arrayparty", m);
//		 js.addArray("arrayparty2", m2);
//		 js.addInteger("nummer3", 123);
//		 js.addDouble("nummer4", 12.3);
//		 System.out.println(js.getString());

		js.parseString("{\"string1\": \"blob\", \"string2\": \"blob\", \"int1\": 123, \"double1\": 12.3, \"array1\": {\"i1\": 4, \"s1\": \"fun1\", \"d1\": 3.2}, \"array2\": {\"i1\": 4, \"s1\": \"fun1\", \"d1\": 3.2} }");
		System.out.println(js.getString());
	}

}
