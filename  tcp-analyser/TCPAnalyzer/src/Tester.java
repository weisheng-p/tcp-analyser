import util.SlidingWindow;


public class Tester {
	public static void main(String arg[])
	{
		SlidingWindow w = new SlidingWindow();
		
		w.addFilledWindow(2,3);
		w.addFilledWindow(6,15);
		w.addFilledWindow(0,6);
		
		System.out.println(w);
	}
}
