package utils;

import java.util.ArrayList;

public class Queue<Element> {
	private ArrayList<Element> elements = new ArrayList<Element>();
	
	public void push(Element e) {
		elements.add(e);
	}
	
	public Element pop() {
		if(elements.size()==0)
			return null;
		else {
			Element result = elements.get(0);
			elements.remove(0);
			return result;
		}
	}
}
