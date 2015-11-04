package Parser;

import java.nio.charset.Charset;

public enum HttpState {
	STATUS_LINE {
		@Override
		protected State next(final byte[] buf, final int pos,final HttpMessage message) {
			 
			int i = pos;
			String sbuf = new String( buf, Charset.forName("UTF-8") );
			char c = sbuf.charAt(i);
			StringBuilder aux = new StringBuilder(); 
						
			while( c != '\n'){
				
				c = sbuf.charAt(i);
				
				if(c != ' ' && message.hasMethod()){
					aux.append(c);
					
				}
				
				
				
				i++;
				
			}
			
			
			return new State(HEADER,i);
		}
	},
	HEADER {
		@Override
		protected State next(final byte[] buf, final int pos,final HttpMessage message) {
			// TODO Auto-generated method stub
			// if (moreHeadersToCome) {
			//	return HEADER;
			//}
			int i = pos;
			return new State(EMPTY_LINE, i);
		}
	},
	EMPTY_LINE {
		@Override
		protected State next(final byte[] buf,final int pos,final HttpMessage message) {
			// TODO Auto-generated method stub
			int i = pos;
			return new State(BODY,i);
		}
	},
	BODY {
		@Override
		protected State next(final byte[] buf, final int pos,final HttpMessage message) {
			// TODO Auto-generated method stub
			int i = pos;
			return new State(DONE, i);
		}
	},
	DONE {
		@Override
		protected State next(final byte[] buf, final int pos,final HttpMessage message) {
			int i = pos;
			return new State(DONE,i);
		}
	};
	
	protected abstract State next(final byte[] buf,final int pos,final HttpMessage message);
	
	public final HttpState process(final byte[] buf, final HttpMessage message) {
		
		int remaining;
		State current = new State (this, 0);
		
		do {
			remaining = buf.length - current.position;
			current = current.state.next(buf, current.position, message);
		} while (remaining != buf.length - current.position && current.state != DONE);
		
		return current.state;
	}
	
	private class State{
		
		HttpState state;
		int position;
		
		State(HttpState state, int position){
			this.state = state;
			this.position = position;
		}
	
	}
}


