package July;

import java.util.LinkedList;

public class StateResponse {

	private boolean isFinished;
	private LinkedList<Character> queue;
	private LinkedList<String> openedTags;
	private int onMethod; //1-statusline; 2-headers; 3-body
	private StringBuilder lastLine;
	private boolean onTag;
	
	
	public StateResponse(){
		this.isFinished = true;
		this.onMethod = 0;
		this.setOpenedTags(new LinkedList<String>());
		this.setQueue(new LinkedList<Character>());
		this.setOnTag(false);
	}
	
	public void setIsFinished(boolean value){
		this.isFinished = value;
	}
	
	public boolean getIsFinished(){
		return this.isFinished;
	}
	
	public void setOnMethod(int method){
		this.onMethod = method;
	}
	
	public int onMethod(){
		return this.onMethod;
	}

	public StringBuilder getLastLine() {
		return lastLine;
	}

	public void setLastLine(StringBuilder lastLine) {
		this.lastLine = lastLine;
	}

	public LinkedList<String> getOpenedTags() {
		return openedTags;
	}

	public void setOpenedTags(LinkedList<String> openedTags) {
		this.openedTags = openedTags;
	}

	public LinkedList<Character> getQueue() {
		return queue;
	}

	public void setQueue(LinkedList<Character> queue) {
		this.queue = queue;
	}

	public boolean getOnTag() {
		return onTag;
	}

	public void setOnTag(boolean onTag) {
		this.onTag = onTag;
	}
	
}
