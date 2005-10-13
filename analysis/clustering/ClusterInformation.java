package analysis.clustering;

import java.util.ArrayList;
import java.util.Set;

public class ClusterInformation {
	// names have 'ATOFMSAtomInfoDense.' , etc. before it. - AR
	public ArrayList<String> valueColumns;
	public String keyColumn;
	public String weightColumn;
	public boolean automatic;
	public boolean normalize;
	
	public ClusterInformation(ArrayList<String> v, String k, String w, boolean a, boolean n) {
		valueColumns = v;
		keyColumn = k;
		weightColumn = w;
		automatic = a;
		normalize = n;
	}
}