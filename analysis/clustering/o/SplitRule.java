package analysis.clustering.o;

import java.util.*;

import analysis.BinnedPeak;
import analysis.BinnedPeakList;

public class SplitRule extends BinnedPeak {
	public double goodness;
	
	public SplitRule(int location, float area) {
		super(location, area);
	}
	
	public SplitRule(int location, float area, double goodness) {
		this(location, area);
		this.goodness = goodness;
	}
	
	public boolean isAtomLess(BinnedPeakList atom) {
		return area > atom.getAreaAt(location);
	}
	
	public List<DataWithSummary> splitAtoms(List<BinnedPeakList> atoms) {
		List<DataWithSummary> bucket;
		bucket = new ArrayList<DataWithSummary>(2);
		bucket.add(new DataWithSummary());
		bucket.add(new DataWithSummary());
		BinnedPeakList atom;
		
		Iterator<BinnedPeakList> i = atoms.iterator();
		
		while (i.hasNext()) {
			atom = i.next();
			if (isAtomLess(atom)) {
				bucket.get(0).add(atom);
			} else {
				bucket.get(1).add(atom);
			}
		}
		return bucket;
	}
	
	public String toString() {
		return "Split along dimension " + location + " at value " + area +
			" (goodness " + goodness + ")";
	}
}
