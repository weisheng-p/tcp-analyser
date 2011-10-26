import java.util.ArrayList;


public class SlidingWindow {
	
	public long expected;
	
	// always sorted by leftEdge
	private ArrayList <Window> filled;
	
	public SlidingWindow()
	{
		filled = new ArrayList<Window>();
	}
	///todo: change expected
	public void addFilledWindow(long leftEdge, long rightEdge)
	{
		Window w = new Window(leftEdge, rightEdge);
		
		Window current; Window previous = null;
		
		for(int i = 0; i < filled.size(); i ++)
		{
			current = filled.get(i);
			if(previous == null)
			{
				// in the front, need merge
				if(current.leftEdge == w.rightEdge)
				{
					current.merge(w);
				}
				// in the front but cannot merge
				else if(current.leftEdge < w.rightEdge)
				{
					filled.add(0, w);
				}
			}
			else
			{
				// in the middle
				
				// merge with current
				if(current.leftEdge == w.rightEdge)
				{
					current.merge(w);
					// if can merge with pervious
					if(previous.rightEdge == current.leftEdge)
					{
						current.merge(previous);
						filled.remove(i - 1);
					}
				}
				// in the middle of nowhere
				else if(current.leftEdge < w.rightEdge && w.leftEdge > previous.rightEdge)
				{
					filled.add(i,w);
				}
				// last
				else if(i == filled.size() - 1)
				{
					filled.add(w);
				}
			}
			previous = current;
		}
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
	
	public void merge(Window m)
	{
		// incoming is on the left;
		if(this.leftEdge == m.rightEdge)
		{
			this.leftEdge = m.leftEdge;
		}
		// incoming is on the right
		else if(this.rightEdge == m.leftEdge)
		{
			this.rightEdge = m.rightEdge;
		}
	}
}