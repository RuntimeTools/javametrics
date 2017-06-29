package com.ibm.javametrics;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {

	/**
     * Split a string of JSON objects into multiple strings
     * Copied from {@link com.ibm.javametrics.web.DataHandler}
     * @param data
     * @return
     */
    public static List<String> splitIntoJSONObjects(String data) {
        List<String> strings = new ArrayList<String>();
        int index = 0;
        // Find first opening bracket
        while (index < data.length() && data.charAt(index) != '{') {
            index++;
        }
        int closingBracket = index + 1;
        int bracketCounter = 1;
        while (index < data.length() - 1 && closingBracket < data.length()) {
            // Find the matching bracket for the bracket at location 'index'
            boolean found = false;
            if (data.charAt(closingBracket) == '{') {
                bracketCounter++;
            } else if (data.charAt(closingBracket) == '}') {
                bracketCounter--;
                if (bracketCounter == 0) {
                    // found matching bracket
                    found = true;
                }
            }
            if (found) {
                strings.add(data.substring(index, closingBracket + 1));
                index = closingBracket + 1;
                // Find next opening bracket and reset counters
                while (index < data.length() && data.charAt(index) != '{') {
                    index++;
                }
                closingBracket = index + 1;
                bracketCounter = 1;
            } else {
                closingBracket++;
            }
        }
        return strings;
    }
	
}
