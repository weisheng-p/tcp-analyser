import java.util.ArrayList;


public class SlidingWindow {
	
	public long expected;
	
	// always sorted by leftEdge
	private ArrayList <Window> filled;
	
	public SlidingWindow()
	{
		filled = new ArrayList<Window>();
	}
	public void clear()
	{
		filled.clear();
	}
	
	public long getNextExpectedSeqNum()
	{
		if(filled.size() == 0)
			return 0;
		return filled.get(0).leftEdge;
	}
	///todo: change expected
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
						filled.remove(i +1);
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
				}
				// add at the front
				else if(previous == null)
				{
					if(current.leftEdge > w.rightEdge)
					{
						filled.add(0,w);
					}
				}
				// add at the middle
				else
				{
					if(previous.rightEdge > w.leftEdge && w.rightEdge < current.leftEdge)
					{
						filled.add(i,w);
					}
				}
			}
			
			previous = current;
		}
		return duplicate;
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
	
	public MergeResult merge(Window m)
	{
		MergeResult rtn = new MergeResult();
		rtn.duplicate = this.isOverlappingWith(m);
		rtn.merged = false;
		// incoming is on the left, directly next or overlapped
		if((this.leftEdge == m.rightEdge) || (m.rightEdge > this.leftEdge && m.rightEdge < this.rightEdge))
		{
			this.leftEdge = m.leftEdge;
			rtn.merged = true;
		}
		// incoming is on the right, directly next or overlapped
		else if((this.rightEdge == m.leftEdge) || (m.leftEdge > this.leftEdge && m.leftEdge < this.rightEdge))
		{
			this.rightEdge = m.rightEdge;
			rtn.merged = true;
		}
		return rtn;
	}
	
	public boolean isOverlappingWith(Window m)
	{
		return ((m.rightEdge > this.leftEdge && m.rightEdge < this.rightEdge) || (m.leftEdge > this.leftEdge && m.leftEdge < this.rightEdge));
	}
}

class MergeResult
{
	public boolean merged;
	public boolean duplicate;
}