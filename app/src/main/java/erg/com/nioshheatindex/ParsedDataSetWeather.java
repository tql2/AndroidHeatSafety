package erg.com.nioshheatindex;

import java.util.Vector;

public class ParsedDataSetWeather
{
    private final Vector veclocation = new Vector();
    private final Vector temperature = new Vector();
    private final Vector humidity = new Vector();
    private final Vector maxtime = new Vector();

    public void addlocation(String myVal)
    {
        veclocation.add(myVal);
    }

    public void addtemperature(String myVal)
    {
    	temperature.add(myVal);
    }
    
    public void addhumidity(String myVal)
    {
    	humidity.add(myVal);
    }

    public Vector getlocation()
    {
        return veclocation;
    }

    public Vector gettemperature()
    {
    	return temperature;
    }
    
    public Vector gethumidity()
    {
    	return humidity;
    }
    
    public void addmaxtime(String myVal)
    {
    	maxtime.add(myVal);
    }

    public Vector getmaxtime()
    {
    	return maxtime;
    }

    public String toString()
    {
	    String myString="";
	    myString = temperature.toString() + humidity.toString();       
	    return myString;
    }
}

