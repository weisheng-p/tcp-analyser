package util;
import java.util.ArrayList;


class MergeResult
{
	public boolean duplicate;
	public boolean merged;
}
public class SlidingWindow {

	// always sorted by leftEdge
	private ArrayList <Window> filled;
	
	public long lastAck;
	
	public SlidingWindow()
	{
		filled = new ArrayList<Window>();
	}
	// return true if is a duplicate, else otherwise
	public boolean addFilledWindow(long leftEdge, long rightEdge)
	{
		Window w = new Window(leftEdge, rightEdge);
		
		Window current; Window previous = null;
		boolean duplicate = false;
		MergeResult mr;
		// if empty window, just add
		if(filled.size() == 0)
		{
			filled.add(w);
			return duplicate;
		}
		for(int i = 0; i < filled.size(); i ++)
		{
			current = filled.get(i);
			mr = current.merge(w);
			// check if we can merge with the current (duplicate)
			if(mr.merged)
			{
				duplicate = mr.duplicate;
				// check if we are the end or not
				if(i + 1 < filled.size())
				{
					Window next = filled.get(i + 1);
					if(current.merge(next).merged)
					{
						filled.remove(i + 1);
					}
				}
				if(previous != null)
				{
					// try to merge with previous
					if(current.merge(previous).merged)
					{
						filled.remove(i - 1);
					}
				}
				
				break;
			}
			// check if we can add it without merging
			else
			{
				// add at the tail
				if(i == filled.size() - 1)
				{
					filled.add(w);
					break;
				}
				// add at the front
				else if(previous == null)
				{
					if(current.leftEdge > w.rightEdge)
					{
						filled.add(0,w);
						break;
					}
				}
				// add at the middle
				else
				{
					if(previous.rightEdge > w.leftEdge && w.rightEdge < current.leftEdge)
					{
						filled.add(i,w);
						break;
					}
				}
			}
			
			previous = current;
		}
		return duplicate;
	}
	
	public void clear()
	{
		filled.clear();
	}
	
	/**
	 * 
	 * @return next expected sequence number from the sender
	 */
	public long getNextExpectedSeqNum()
	{
		if(filled.size() == 0)
			return 0;
		return filled.get(0).leftEdge;
	}
	public long getLastExpectedSeqNum()
	{
		if(filled.size() == 0)
		{
			return 0;
		}
		return filled.get(0).rightEdge - filled.get(0).leftEdge;	
	}
	
	public String toString() {
		String buf = "";
		for(Window w : filled)
		{
			if(buf.isEmpty())
				buf = w.toString();
			else
				buf += ", " + w.toString();
		}
		return buf;
	}
}

class Window
{
	long leftEdge;
	long rightEdge;
	
	public Window(long leftEdge, long rightEdge)
	{
		this.leftEdge = leftEdge;
		this.rightEdge = rightEdge;
	}
	
	
	public boolean isOverlappingWith(Window that)
	{
		return (
				(that.rightEdge > this.leftEdge && that.rightEdge < this.rightEdge) || 
				(that.leftEdge > this.leftEdge && that.leftEdge < this.rightEdge) ||
				(that.leftEdge < this.leftEdge && that.rightEdge > this.rightEdge)
				
				);
	}
	
	public MergeResult merge(Window that)
	{
		MergeResult rtn = new MergeResult();
//		System.out.print(this.toString() + " merge with " + that.toString());
		if(that.equals(this) ||
				(this.leftEdge < that.leftEdge && this.rightEdge > that.rightEdge) 
				)
		{
			rtn.duplicate = true;
			rtn.merged = true;
//			System.out.print(" -> " + this +"*\n");
			return rtn;
		}
		rtn.duplicate = this.isOverlappingWith(that);
		rtn.merged = false;
		// bigger at both side
		if(that.leftEdge < this.leftEdge && that.rightEdge > this.rightEdge)
		{
			this.leftEdge = that.leftEdge;
			this.rightEdge = that.rightEdge;
			rtn.merged = true;
		}
		// incoming is on the left, directly next or overlapped
		else if((this.leftEdge == that.rightEdge) || (that.rightEdge > this.leftEdge && that.rightEdge < this.rightEdge))
		{
			this.leftEdge = that.leftEdge;
			rtn.merged = true;
		}
		// incoming is on the right, directly next or overlapped
		else if((this.rightEdge == that.leftEdge) || (that.leftEdge > this.leftEdge && that.leftEdge < this.rightEdge))
		{
			this.rightEdge = that.rightEdge;
			rtn.merged = true;
		}
//		System.out.print(" -> " + this +"\n");
		return rtn;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Window)
		{
			Window w = (Window) obj;
			return w.leftEdge == this.leftEdge && w.rightEdge == this.leftEdge;
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ " + leftEdge + ", " + rightEdge + " ]";
	}
	
	

}