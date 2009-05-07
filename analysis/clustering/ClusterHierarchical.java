/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is EDAM Enchilada's Clusters class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Ben J Anderson andersbe@gmail.com
 * David R Musicant dmusican@carleton.edu
 * Anna Ritz ritza@carleton.edu
 * Tom Bigwood tom.bigwood@nevelex.com
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package analysis.clustering;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ATOFMS.ParticleInfo;
import analysis.CollectionDivider;
import database.InfoWarehouse;
import database.NonZeroCursor;
import externalswing.SwingWorker;

/**
 * @author jtbigwoo
 * @version 1.0 April 23, 2009 Yesterday was the 15th anniversary of Nixon's death.
 */

public class ClusterHierarchical extends Cluster {

	protected int numClusters; // number of centroids desired.
	private int numParticles; // number of particles in the collection.
	private int returnThis;

	private JDialog errorUpdate;
	private JLabel errorLabel;
	private JFrame container;

	/**
	 * Constructor.  Calls the constructor for ClusterK.
	 * @param cID - collection ID
	 * @param database - database interface
	 * @param k - number of centroids desired
	 * @param name - collection name
	 * @param comment -comment to enter
	 */
	public ClusterHierarchical(int cID, InfoWarehouse database, int k,
			String name, String comment, ClusterInformation c) 
	{
		super(cID, database, name.concat("Hierarchical"), comment, true);
		collectionID = cID;
		clusterInfo = c;//set inherited variable
		numClusters = k;
	}
	
	/** 
	 * method necessary to extend from ClusterK.  Begins the clustering
	 * process.
	 * @param - interactive or testing mode - christej
	 * @return - new collection int.
	 */
	public int cluster(boolean interactive) {
		if(interactive)
			return divide();
		else
			return innerDivide(interactive);
	}

	/**
	 * Divide refines the centroids if needed and calls the clustering method.
	 * In the end, it finalizes the clusters by calling a method to report 
	 * the centroids.
	 * TODO:  The max number of subsamples clustered when we refine centroids is 
	 * 50.  We need a way to either validate this or a way to change it from the
	 * application.  
	 * 
	 * (non-Javadoc)
	 * @see analysis.CollectionDivider#divide()
	 */
	public int divide() {
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				int returnThis = innerDivide(true);	
				return returnThis;
			}
		};
		
		errorUpdate = new JDialog((JFrame)container,"Clustering",true);
		errorLabel = new JLabel("Clusters stabilize when change in error = 0");
		errorLabel.setSize(100,250);
		errorUpdate.add(errorLabel);
		errorUpdate.pack();
		errorUpdate.validate();
		// XXX: still a race condition!  Not a really bad one, though.  		System.out.println("HERE");
		worker.start();
		errorUpdate.setVisible(true);
		
		return returnThis;
	}

	public int innerDivide(boolean interactive) {
		numParticles = db.getCollectionSize(collectionID);

		processPart();
		returnThis = newHostID;
		// TODO Auto-generated method stub
		return returnThis;
	}

	private void processPart()
	{
		ArrayList<ClusterPair> clusterPairs = new ArrayList<ClusterPair>((numParticles * numParticles) / 2);
		HashMap<Integer, ClusterContents> clusterContents = new HashMap<Integer, ClusterContents>((numParticles * 4) / 2);
		ArrayList<ParticleInfo> particles = new ArrayList<ParticleInfo>(numParticles);
		
		// set up distance matrix
		while (curs.next()) {
			ParticleInfo info = curs.getCurrent();
			info.getBinnedList().posNegNormalize(distanceMetric);
			ClusterContents currentCluster = new ClusterContents(info.getID());
			clusterContents.put(info.getID(), currentCluster);
			for (ParticleInfo infoFromList : particles) {
				float distance = info.getBinnedList().getDistance(infoFromList.getBinnedList(), distanceMetric);
				clusterPairs.add(new ClusterPair(info.getID(), infoFromList.getID(), distance));
			}
			particles.add(info);
		}
		particles = null;
		
		while (clusterContents.size() > numClusters && clusterContents.size() > 1)
		{
			// the first pair element in the sorted list has the smallest distance, merge them
			// name the clusters A and B.  Cluster B will be merged into Cluster A and removed.
			// Sorry, cluster B.
			Collections.sort(clusterPairs);
			int clusterAID = clusterPairs.get(0).getFirstClusterID();
			int clusterBID = clusterPairs.get(0).getSecondClusterID();
			float aToBDistance = clusterPairs.get(0).getDistance();
			int clusterASize = clusterContents.get(clusterAID).getAtomIDList().size();
			int clusterBSize = clusterContents.get(clusterBID).getAtomIDList().size();
			clusterPairs.remove(0);
			clusterContents.get(clusterAID).merge(clusterContents.get(clusterBID));
			clusterContents.remove(clusterBID);
			
			HashMap<Integer, ClusterPair> removedPairs = new HashMap<Integer, ClusterPair>((clusterContents.size() * 4) / 3);
			// because we're going to be removing elements, 
			// it's easier to go backwards through the list
			for (int i = clusterPairs.size() - 1; i >= 0; i--) {
				// We're trying to get the distance from the new cluster to each 
				// existing cluster.  We'll call the existing clusters Cluster Q.
				int clusterQID = -1;
				ClusterPair currentPair = clusterPairs.get(i);
				if (clusterAID == currentPair.getFirstClusterID() || clusterBID == currentPair.getFirstClusterID()) {
					clusterQID = currentPair.getSecondClusterID();
				}
				else if (clusterAID == currentPair.getSecondClusterID() || clusterBID == currentPair.getSecondClusterID()) {
					clusterQID = currentPair.getFirstClusterID();
				}
				if (clusterQID != -1) {
					if (removedPairs.get(clusterQID) == null) {
						// if this is the first time we've encountered Cluster Q, delete the pair
						// from the list, but save it for calculations below.
						removedPairs.put(clusterQID, currentPair);
						clusterPairs.remove(i);
					}
					else {
						// if this is the second time we've encountered Cluster Q, recalculate
						// distance and put a new pair in the list.
						ClusterPair removedPair = removedPairs.get(clusterQID);
						int clusterQSize = clusterContents.get(clusterQID).getAtomIDList().size();
						float aToQDistance, bToQDistance, newDistance;
						if (clusterAID == removedPair.getFirstClusterID() || clusterAID == removedPair.getSecondClusterID()) {
							aToQDistance = removedPair.getDistance();
							bToQDistance = currentPair.getDistance();
						}
						else
						{
							bToQDistance = removedPair.getDistance();
							aToQDistance = currentPair.getDistance();
						}
						float distance = ((clusterASize + clusterQSize) * aToQDistance) / (clusterASize + clusterBSize + clusterQSize) + 
							((clusterBSize + clusterQSize) * bToQDistance) / (clusterASize + clusterBSize + clusterQSize) - 
							((clusterQSize) * aToBDistance) / (clusterASize + clusterBSize + clusterQSize);
						clusterPairs.set(i, new ClusterPair(clusterAID, clusterQID, distance));
					}
				}
			}
		}
		return;
	}
	
	class ClusterPair implements Comparable<ClusterPair> {
		
		int firstClusterID;
		int secondClusterID;
		float distance;
		
		public ClusterPair(int firstID, int secondID, float dist) {
			firstClusterID = firstID;
			secondClusterID = secondID;
			distance = dist;
		}

		public int getFirstClusterID() {
			return firstClusterID;
		}
		
		public int getSecondClusterID() {
			return secondClusterID;
		}
		
		public float getDistance() {
			return distance;
		}

		public int compareTo(ClusterPair otherOne) {
			if (otherOne.distance > this.distance) {
				return -1;
			}
			else if (otherOne.distance < this.distance) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	class ClusterContents {
		
		int clusterID;
		ArrayList<Integer> atomIDList;
		
		public ClusterContents(int atomID) {
			clusterID = atomID;
			atomIDList = new ArrayList<Integer>();
			atomIDList.add(atomID);
		}
		
		public void merge(ClusterContents otherOne) {
			atomIDList.addAll(otherOne.atomIDList);
		}
		
		public ArrayList<Integer> getAtomIDList() {
			return atomIDList;
		}
	}
	/**
	 * Sets the cursor type; clustering can be done using either by 
	 * disk or by memory.
	 * 
	 * (non-Javadoc)
	 * @see analysis.CollectionDivider#setCursorType(int)
	 */
	public boolean setCursorType(int type) 
	{

		switch (type) {
		case CollectionDivider.DISK_BASED :
			System.out.println("DISK_BASED");
			try {
					curs = new NonZeroCursor(db.getBPLOnlyCursor(db.getCollection(collectionID)));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		return true;
		case CollectionDivider.STORE_ON_FIRST_PASS : 
		    System.out.println("STORE_ON_FIRST_PASS");
			curs = new NonZeroCursor(db.getMemoryClusteringCursor(db.getCollection(collectionID), clusterInfo));
		return true;
		default :
			return false;
		}
	}

}