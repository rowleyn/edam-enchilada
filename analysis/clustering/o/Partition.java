package analysis.clustering.o;

/**
 * Interface for different kinds of Partitions.  See OClusterThoughts for what
 * this means.
 * @author Thomas Smith
 */


import java.util.List;
import analysis.BinnedPeakList;
import analysis.CollectionDivider;

public interface Partition {
	public CollectionDivider getCollectionSource();
	public void setCollectionSource(CollectionDivider collectionSource);
	
	public Partition getParent();
	public Partition getLeftChild();
	public Partition getRightChild();
	
	public void setParent(Partition parent);
	
	public boolean transmogrifyChild(Partition oldChild, Partition newChild);
	
//	public int classify(BinnedPeakList bpl);
	// i don't know how that's going to work...
	public int split(DataWithSummary atoms);
	public String rulesUp();
	public String rulesDown();
}