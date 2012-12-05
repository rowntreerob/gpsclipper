package com.b2bpo.media.geophoto;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StringPullHandler extends DefaultHandler {

	private final StringBuilder sb = new StringBuilder();

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		sb.append(ch, start, length);
	}

	protected String getCharacters() {
		return sb.toString().trim();
	}

	protected long getLong() {
		try {
			return Long.parseLong(getCharacters());
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Not a number: |" + getCharacters()
					+ '|');
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		sb.setLength(0);
		startElement2(uri, localName, qName, attributes);
	}

	protected void startElement2(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		endElement2(uri, localName, qName);
		sb.setLength(0);
	}

	public void endElement2(String uri, String localName, String qName)
			throws SAXException {
	}
}