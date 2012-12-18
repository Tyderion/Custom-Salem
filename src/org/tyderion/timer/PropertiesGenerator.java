package org.tyderion.timer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;



public class PropertiesGenerator {
	
	private String prefix;
	
	public PropertiesGenerator(String prefix) {
		this.prefix = prefix;
	}
	
	
    public String propertyKeyOf(String str)
    {
    	if (prefix == null) {
    		return str;
    	} else {
    		return prefix+"."+str;
    	}
    }
    
    
    public Properties toProperty(Object key, Object value)
    {
    	String rkey, rvalue = "";
    	if (key.getClass().equals(String.class)) {
           rkey = propertyKeyOf((String)key);
        } else
        {
        	rkey = propertyKeyOf(key.toString());
        }
    	
    	if (value.getClass().equals(String.class)) {
    		rvalue = (String)value;
    	} 
    	else if (value.getClass().equals(Long.TYPE))
    	{
    		rvalue = String.valueOf((long)value);
    	} 
    	else if (value.getClass().equals(Integer.TYPE))
    	{
    		rvalue = String.valueOf((int)value);
    	} 

    	Properties props = new Properties();
    	props.setProperty(rkey, rvalue);
    	return props;
    }
    
    


    /**
     * Finds the index of all entries in the list that matches the regex
     * @param list The list of strings to check
     * @param regex The regular expression to use
     * @return list containing the indexes of all matching entries
     */
    public static List<String> getMatchingEntries(Set<Object> set, String regex) 
    {
      Iterator<Object> it =set.iterator();

      List<String> strings = new ArrayList<String>();
      while(it.hasNext()) 
      {
        String next = (String)it.next();
        
        if(Pattern.matches(regex, next)) {
        	strings.add(next);
        }
      }

      return strings;
    }

}
