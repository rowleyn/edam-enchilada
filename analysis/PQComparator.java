package analysis;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ArrayList;

public class PQComparator 
implements Comparator<ArrayList<Integer>> 
{
	public int compare(
			ArrayList<Integer> arg0, 
			ArrayList<Integer> arg1) 
	{
		Iterator<Integer> iter0 = arg0.iterator(), 
		iter1 = arg1.iterator();
		
		while (iter0.hasNext() && iter1.hasNext())
		{
			int result = iter0.next()-iter1.next();
			if (result != 0)
				return result;
		}
		if (iter0.hasNext())
			return 1;
		else if (iter1.hasNext())
			return -1;
		else
			return 0;
	}

}
