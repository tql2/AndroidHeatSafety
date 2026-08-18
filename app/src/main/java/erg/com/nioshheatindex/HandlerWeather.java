package erg.com.nioshheatindex;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/** Parses the subset of NWS DWML used by the app. */
class HandlerWeather extends DefaultHandler {
    private final StringBuilder locationText = new StringBuilder();
    private final StringBuilder valueText = new StringBuilder();
    private final StringBuilder timeText = new StringBuilder();
    private boolean inLocation;
    private boolean inValue;
    private boolean inHourlyTemperature;
    private boolean inRelativeHumidity;
    private boolean inStartValidTime;
    private ParsedDataSetWeather parsedData = new ParsedDataSetWeather();

    ParsedDataSetWeather getParsedData() {
        return parsedData;
    }

    @Override
    public void startDocument() {
        parsedData = new ParsedDataSetWeather();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) {
        String name = elementName(localName, qName);
        switch (name) {
            case "description":
            case "area-description":
                inLocation = true;
                locationText.setLength(0);
                break;
            case "temperature":
                inHourlyTemperature = "hourly".equalsIgnoreCase(valueOf(attributes, "type"));
                break;
            case "humidity":
                inRelativeHumidity = "relative".equalsIgnoreCase(valueOf(attributes, "type"));
                break;
            case "value":
                if (inHourlyTemperature || inRelativeHumidity) {
                    inValue = true;
                    valueText.setLength(0);
                }
                break;
            case "start-valid-time":
                inStartValidTime = true;
                timeText.setLength(0);
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        String name = elementName(localName, qName);
        switch (name) {
            case "description":
            case "area-description":
                addIfNotBlank(locationText, parsedData::addlocation);
                inLocation = false;
                break;
            case "temperature":
                inHourlyTemperature = false;
                break;
            case "humidity":
                inRelativeHumidity = false;
                break;
            case "value":
                if (inValue) {
                    String value = valueText.toString().trim();
                    if (!value.isEmpty()) {
                        if (inHourlyTemperature) {
                            parsedData.addtemperature(value);
                        } else if (inRelativeHumidity) {
                            parsedData.addhumidity(value);
                        }
                    }
                    inValue = false;
                }
                break;
            case "start-valid-time":
                addIfNotBlank(timeText, parsedData::addmaxtime);
                inStartValidTime = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) {
        if (inLocation) {
            locationText.append(chars, start, length);
        }
        if (inValue) {
            valueText.append(chars, start, length);
        }
        if (inStartValidTime) {
            timeText.append(chars, start, length);
        }
    }

    private static String elementName(String localName, String qName) {
        return localName == null || localName.isEmpty() ? qName : localName;
    }

    private static String valueOf(Attributes attributes, String name) {
        return attributes == null ? null : attributes.getValue(name);
    }

    private static void addIfNotBlank(StringBuilder text, java.util.function.Consumer<String> consumer) {
        String value = text.toString().trim();
        if (!value.isEmpty()) {
            consumer.accept(value);
        }
    }
}
