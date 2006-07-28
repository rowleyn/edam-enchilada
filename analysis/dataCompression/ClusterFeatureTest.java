package analysis.dataCompression;

import java.util.ArrayList;
import analysis.BinnedPeakList;
import analysis.DistanceMetric;
import analysis.Normalizer;
import junit.framework.TestCase;

/**
 * 
 * @author christej
 *
 */

public class ClusterFeatureTest extends TestCase {
	private BinnedPeakList bp1;
	private ClusterFeature testCF1;
	private DistanceMetric dMetric;
	
	protected void setUp() throws Exception {
		super.setUp();
		dMetric = DistanceMetric.EUCLIDEAN_SQUARED;
		bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-210, (float) 1);
		bp1.add(-160, (float) 1);
		bp1.add(-100, (float) 1);
		bp1.add(-30, (float) 1);
		bp1.add(20, (float) 2);
		bp1.add(90, (float) 2);
		bp1.add(120, (float) 2);
		testCF1 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		testCF1.updateCF(bp1, 1);
	}
	public void testUpdateCF() {
		DistanceMetric dMetric = DistanceMetric.EUCLIDEAN_SQUARED;
		BinnedPeakList bp1 = new BinnedPeakList(new Normalizer());
		bp1.add(-250, (float) 1);
		bp1.add(-200, (float) 1);
		bp1.add(-95, (float) 1);
		ClusterFeature test = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		test.updateCF(bp1, 1);
		
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-25, (float) 1);
		bp2.add(30, (float) 2);
		bp2.add(100, (float) 2);
		bp2.add(125, (float) 2);
		test.updateCF(bp2, 2);
		
		ArrayList<Integer> expected = new ArrayList<Integer>();
		expected.add(1);
		expected.add(2);
		assertEquals(test.getAtomIDs(), expected);
		assertEquals(test.getCount(), 2);
		bp1.normalize(dMetric);
		bp1.addAnotherParticle(bp2);
		bp1.normalize(dMetric);
		assert test.getSums().getDistance(bp1, dMetric)==0.0 : "not equal";
	}
	
	public void testAbsorbCF() {
		
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-200, (float) 1);
		bp2.add(-150, (float) 1);
		bp2.add(-90, (float) 1);
		bp2.add(-20, (float) 1);
		bp2.add(30, (float) 2);
		bp2.add(100, (float) 2);
		bp2.add(130, (float) 2);
		testCF1.updateCF(bp2, 2);
		
		BinnedPeakList bp3 = new BinnedPeakList(new Normalizer());
		bp3.add(-210, (float) 0.1);
		bp3.add(-200, (float) 0.1);
		bp3.add(-160, (float) 0.1);
		bp3.add(-150, (float) 0.1);
		ClusterFeature testCF2 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		testCF2.updateCF(bp3, 3);

		testCF1.absorbCF(testCF2);

		ArrayList<Integer> expected = new ArrayList<Integer>();
		expected.add(1);
		expected.add(2);
		expected.add(3);
		assertEquals(testCF1.getAtomIDs(), expected);
		assertEquals(testCF1.getCount(), 3);

		bp1.addAnotherParticle(bp2);
		bp1.normalize(dMetric);
		bp1.multiply(2);
		bp1.addAnotherParticle(bp3);
		bp1.normalize(dMetric);
		assert testCF1.getSums().getDistance(bp1, dMetric)==0.0 : "not equal";
	}
	
	public void testIsEqual() {
		
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-210, (float) 0.1);
		ClusterFeature testCF2 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		testCF2.updateCF(bp2, 2);
		assert !testCF2.isEqual(testCF1) : "they are equal";

		testCF2.setCount(1);
		assert !testCF2.isEqual(testCF1) : "they are equal";
		
		BinnedPeakList bp3 = new BinnedPeakList(new Normalizer());
		bp3.copyBinnedPeakList(bp1);
		testCF2.setSums(bp3);

		assert testCF2.isEqual(testCF1) : "they are not equal";
		
	}

	public void testMakesSumsSparse() {
		ClusterFeature testCF2 = new ClusterFeature(new CFNode(null, dMetric), dMetric);
		testCF2.updateCF(bp1, 1);
		assert testCF1.isEqual(testCF2) : "not equal";
		testCF1.getSums().add(0,0);
		assert !testCF1.isEqual(testCF2) : "are equal";
		testCF1.makeSumsSparse();
		assert testCF1.isEqual(testCF2): "not equal";
	}
	
	public void testUpdateCFBoolean() {
		
		ClusterFeature test = new ClusterFeature(null, dMetric);
		test.updateCF(bp1,1);
		CFNode node = new CFNode(testCF1, dMetric);
		testCF1.updatePointers(node, null);
		
		BinnedPeakList bp2 = new BinnedPeakList(new Normalizer());
		bp2.add(-210, (float) 1);
		bp2.add(-160, (float) 1);
		bp2.add(-100, (float) 1);
		bp2.add(-30, (float) 1);
		ClusterFeature testCF2 = new ClusterFeature(node, dMetric);
		testCF2.updateCF(bp2, 2);
		node.addCF(testCF2);
		
		BinnedPeakList bp3 = new BinnedPeakList(new Normalizer());
		bp3.add(-210, (float) 1);
		bp3.add(-200, (float) 1);
		bp3.add(-160, (float) 1);
		bp3.add(-150, (float) 1);
		ClusterFeature testCF3 = new ClusterFeature(node, dMetric);
		testCF3.updateCF(bp3, 3);
		node.addCF(testCF3);
		
		testCF1.updateCF();

		assert !testCF1.isEqual(test) : "are equal";
		ArrayList<Integer> array = new ArrayList<Integer>();
		array.add(2);
		array.add(3);
		assertEquals(array, testCF1.getAtomIDs());
	}
	
}
