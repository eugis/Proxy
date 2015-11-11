package ParserResponse;

import java.util.LinkedList;

public class StateResponse {

	private boolean isFinished;
	private LinkedList<Character> queue;
	private LinkedList<String> openedTags;
	private int onMethod; //1-statusline; 2-headers; 3-body
	private StringBuilder lastLine;
	private boolean onComment;
	
	
	public StateResponse(){
		this.isFinished = true;
		this.onMethod = 0;
		this.setOpenedTags(new LinkedList<String>());
		this.setQueue(new LinkedList<Character>());
		this.setOnComment(false);
	}
	
	public void setIsFinished(boolean value){
		this.isFinished = value;
		if(value){
			this.onMethod = 0;
			this.setOpenedTags(new LinkedList<String>());
			this.setQueue(new LinkedList<Character>());
			this.setOnComment(false);
		}
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

	public boolean getOnComment() {
		return onComment;
	}

	public void setOnComment(boolean onTag) {
		this.onComment = onTag;
	}
	
}
