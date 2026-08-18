package erg.com.nioshheatindex;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class HandlerWeather extends DefaultHandler
{
    public static String TAG = HandlerWeather.class.getSimpleName();
    private boolean in_dwml = false;
    private boolean in_data = false;
    private boolean in_parameters = false;
    private boolean in_temperature = false;
    private boolean in_humidity = false;
    private boolean in_name = false;
    private boolean in_value = false;
    private boolean in_value_temp = false;
    private boolean in_value_humid = false;
    private boolean in_location = false;
    private boolean in_location_key = false;
    private boolean in_start_valid_time = false;
    private int mystarttemp = 0;
    private int mystarthumid = 0;
    private String myTimeString = "";
    private ParsedDataSetWeather myParsedExampleDataSetWeather = new ParsedDataSetWeather();

    public ParsedDataSetWeather getParsedData()
    {
        return this.myParsedExampleDataSetWeather;
    }
 
    @Override
    public void startDocument() throws SAXException
    {
        this.myParsedExampleDataSetWeather = new ParsedDataSetWeather();

    }

    @Override
    public void endDocument() throws SAXException
    {
        // Nothing to do
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        if (localName.equals("dwml"))
        {
            this.in_dwml = true;
	    }
        else if (localName.equals("description"))
        {
            this.in_location = true;
        }
        else if (localName.equals("area-description"))
        {
            this.in_location = true;
        }
        else if (localName.equals("data"))
        {
	        this.in_data = true;
        }
        else if (localName.equals("parameters"))
        {
	        this.in_parameters = true;
        }
	    else if (localName.equals("temperature"))
        {
	        this.in_temperature = true;
	        String attrValue = atts.getValue("type");
	        if (attrValue.equalsIgnoreCase("hourly"))
            {
	            mystarttemp = 1;
	        }
        }
	    else if (localName.equals("humidity"))
        {
            this.in_humidity = true;
            String attrValue = atts.getValue("type");
            if (attrValue.equalsIgnoreCase("relative"))
            {
                mystarthumid = 1;
            }
        }
        else if (localName.equals("name"))
        {
        	this.in_name = true;
	    }
        else if (localName.equals("value"))
        {
	       	this.in_value = true;
	    }
	    else if (localName.equals("start-valid-time"))
        {
            myTimeString = "";
	        this.in_start_valid_time = true;
        }
    }
        
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
       	if (localName.equals("dwml"))
        {
            this.in_dwml = false;
        }
        else if (localName.equals("description") || localName.equals("area-description"))
        {
            this.in_location = false;
        }
        else if (localName.equals("data"))
        {
            this.in_data = false;
        }
        else if (localName.equals("parameters"))
        {
            this.in_parameters = false;
        }
        else if (localName.equals("temperature"))
        {
            this.in_temperature = false;
            this.mystarttemp = 0;
        }
        else if (localName.equals("humidity"))
        {
            this.in_humidity = false;
            this.mystarthumid = 0;
        }
        else if (localName.equals("name"))
        {
        	this.in_name = false;
        }
        else if (localName.equals("value"))
        {
        	this.in_value = false;
        }
        else if (localName.equals("start-valid-time"))
        {
            myParsedExampleDataSetWeather.addmaxtime(myTimeString);
                myTimeString = "";
        		this.in_start_valid_time = false;
        }
    }

    @Override
    public void characters(char ch[], int start, int length)
    {
        if(in_location)
        {
            myParsedExampleDataSetWeather.addlocation(new String(ch, start, length));
        }

        if(this.in_value)
        {
            if (this.mystarttemp==1)
            {
                myParsedExampleDataSetWeather.addtemperature(new String(ch, start, length));
            }
            if (this.mystarthumid==1)
            {
                myParsedExampleDataSetWeather.addhumidity(new String(ch, start, length));
            }
        }
        if(this.in_start_valid_time)
        {
            myTimeString += (new String(ch, start, length));
        }
    }
}
