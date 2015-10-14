package Parser;

import java.nio.ByteBuffer;

public enum HttpState {
	STATUS_LINE {
		@Override
		protected HttpState next(final ByteBuffer bb, final HttpMessage message) {
			// TODO Auto-generated method stub
			return HEADER;
		}
	},
	HEADER {
		@Override
		protected HttpState next(final ByteBuffer bb, final HttpMessage message) {
			// TODO Auto-generated method stub
			// if (moreHeadersToCome) {
			//	return HEADER;
			//}
			
			return EMPTY_LINE;
		}
	},
	EMPTY_LINE {
		@Override
		protected HttpState next(final ByteBuffer bb, final HttpMessage message) {
			// TODO Auto-generated method stub
			return BODY;
		}
	},
	BODY {
		@Override
		protected HttpState next(final ByteBuffer bb, final HttpMessage message) {
			// TODO Auto-generated method stub
			return DONE;
		}
	},
	DONE {
		@Override
		protected HttpState next(final ByteBuffer bb, final HttpMessage message) {
			return DONE;
		}
	};
	
	protected abstract HttpState next(final ByteBuffer bb, final HttpMessage message);
	
	public final HttpState process(final ByteBuffer bb, final HttpMessage message) {
		// Should never happen, but let's be defensive
		if (bb.remaining() == 0) {
			return this;
		}
		
		int remaining;
		HttpState current = this;
		do {
			remaining = bb.remaining();
			current = current.next(bb, message);
		} while (remaining != bb.remaining() && current != DONE);
		
		return current;
	}
}
