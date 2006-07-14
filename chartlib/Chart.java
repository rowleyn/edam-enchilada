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
 * The Original Code is EDAM Enchilada's Chart class.
 *
 * The Initial Developer of the Original Code is
 * The EDAM Project at Carleton College.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Jonathan Sulman sulmanj@carleton.edu
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

/*
 * Created on Feb 1, 2005
 *
 * A class for handling and displaying charts.
 */
package chartlib;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author sulmanj
 * A container with one or more chart areas (containing labeled axes and data),
 * a key and a title, if desired.
 * 
 * The primary role of this class is to handle the layout of chart areas
 * and related components, and to relay messages to them.
 * The actual handling of graphing and data happens
 * in the ChartArea class.
 */
public class Chart extends JPanel
{
	//collection of datasets
	protected Dataset[] datasets; 
	protected String title;
	protected int numCharts;
	
	//graphical elements
	protected ChartTitle titleLabel;
	protected JPanel bottomHalf;
	protected ChartKey key;
	protected ArrayList<ChartArea> chartAreas;
	protected JPanel chartPanel;
	protected JPanel ckPanel;
	public static final Color[] DATA_COLORS = {Color.ORANGE, Color.BLUE, Color.RED, Color.GREEN};
	
	//graphics settings
	protected boolean hasKey; //does the chart have a key to the data colors
	
	//flag to indicate that the chart's current value should be kept for
	//a parameter.
	public static final int CURRENT_VALUE = Integer.MAX_VALUE;
	
	/**
	 * Default contructor.  Initializes a chart with no datasets
	 * and one chartArea with default axis limits 0 - 10.
	 *
	 */
	public Chart()
	{
		datasets = new Dataset[1];
		title = "New Chart";
		numCharts = 1;
		hasKey = false;
		
		setupLayout();
		packData();
	}
	
	public Chart(Dataset ds, String titleString)
	{
		
		//at this point, we limit to one dataset
		datasets = new Dataset[1];
		
		datasets[0] = ds;
		
		title = titleString;
		numCharts = 1;
		hasKey = true;
		
		setupLayout();

	}
	
	/*
	 * Creates a dataset with multiple chart areas, stacked vertically.
	 * @param numAreas The number of chart areas.
	 */ 
	/*public Chart( int numAreas, boolean combineArea )
	{
		numCharts = numAreas;
		combineCharts = combineArea;
		title = "New Chart";
		hasKey = true;
		datasets = new Dataset[numAreas];
		setupLayout();
	}*/
	
	/**
	 * Returns the index of the chart value at point p in the Chart.
	 * @param p The point to check.
	 * @return The index of the chart found, or -1 if no chart is found.
	 */
	public int getChartIndexAt(Point p)
	{
		return getChartIndexAt(p, false);
	}
	
	/**
	 * Returns the index of the chart value at point p in the Chart.
	 * @param p The point to check.
	 * @param p If true, only checks for charts' actual data value;
	 * if the point is on an axis, the method will return -1.
	 * @return The index of the chart found, or -1 if no chart's 
	 * data value is found at the point.
	 */ 
	public int getChartIndexAt(Point p, boolean dataAreaOnly)
	{
		Component cp = findComponentAt(p);
		int result = -1;
		
		for(int count = 0; count < chartAreas.size(); count++)
			if(cp == chartAreas.get(count)) result = count;
		
		if(result != -1 && dataAreaOnly)
		{
			//translate point to chartArea coordinates
			Point q = getChartLocation(result);
			q.x = p.x - q.x;
			q.y = p.y - q.y;
			//System.out.println(q.x + ", "+ q.y);
			if(!((ChartArea)(cp)).isInDataArea(q))
				return -1;
		}
		
		return result;
	}
	
	/**
	 * Given a point in screen coordinates that is on a chart,
	 * finds what key in chart
	 * coordinates the screen point is at.
	 * @param index The chart to apply the point to.
	 * @param p The point to get the value for.
	 * @return A Point2D.Double object containing the key of p
	 * in the chart, converted to chart coordinates.  Returns null if
	 * the point is not within the data value of the specified chart.
	 */
	public java.awt.geom.Point2D.Double getDataValueForPoint(int index, Point p)
	{
//		int chart = getChartAt(p,true);
//		if(chart == -1) return null;
//		else 
//		{
		//if(index < 0 || index > chartAreas.length) return null;
		Point q = getChartLocation(index);
		q.x = p.x - q.x;
		q.y = p.y - q.y;
		return chartAreas.get(index).getDataValueForPoint(q);
//		}
	}
	
	/**
	 * Given a point in screen coordinates that is on a chart,
	 * finds what key in chart
	 * coordinates the screen point is at.
	 * @param p The point in screen coordinates.
	 * @return A Point2D.Double object containing the key of p
	 * in the chart, converted to chart coordinates.  Returns null if
	 * the point is not within the data value of a chart.
	 */
	public java.awt.geom.Point2D.Double getDataValueForPoint(Point p)
	{
		int chart = getChartIndexAt(p,true);
		if(chart == -1) return null;
		else 
		{
			return getDataValueForPoint(chart, p);
		}
	}
	
	/**
	 * Gets the point in the dataset that is under point p in the chart.
	 * For a bar chart, detects the data point if p is within 3 pixels of
	 * any point in the bar.
	 * @param index The chart to check.
	 * @param p The point in screen coordinates.
	 * @return The x coordinate of the value found.
	 */
	public Double getBarForPoint(int index, Point p) throws ClassCastException
	{
		Point q = getChartLocation(index);
		q.x = p.x - q.x;
		q.y = p.y - q.y;
		return ((BarChartArea)chartAreas.get(index)).getBarAt(q, 3);
	}
	
	public Double getBarForPoint(Point p)
	{
		int chart = getChartIndexAt(p, true);
		if(chart == -1) return null;
		else
			return getBarForPoint(chart, p);
	}
	
//	/**
//     * Finds the coordinate in the chart's display space
//     * corresponding to the given data value.
//     * @param x The data value to transform to screen coordinates.
//     * @return The X coordinate in screen space of x, relative to the chart's
//     * data value.  Returns -1 if x is not within the chart's bounds.
//     */
//	public int getXCoordForDataValue(int index, double x)
//	{
//		return chartAreas.get(index].getXCoordForDataValue(x);
//	}
	
	/**
	 * Returns a chart's dataset.
	 * @param index The chart.
	 * @return The specified chart's dataset.
	 */
	public Dataset getDataset(int index)
	{
		return chartAreas.get(index).getDataset(0);
	}
	
	/**
	 * Returns the number of chart areas in this chart.
	 * @return the number of chart areas in this chart.
	 */
	public int getNumCharts()
	{
		return numCharts;
	}
	
	/**
	 * Returns the lower limit on the x axis of the chart.
	 * @param index Which chart to check.
	 * @return the lower limit on the x axis of the chart.
	 */
	public double getXmin(int index)
	{
		return chartAreas.get(index).xAxis.getMin();
	}
	
	/**
	 * Returns the upper limit on the x axis of the chart.
	 * @param index Which chart to check.
	 * @return the upper limit on the x axis of the chart.
	 */
	public double getXmax(int index)
	{
		return chartAreas.get(index).xAxis.getMax();
	}
	
	/**
	 * Returns the lower limit on the y axis of the chart.
	 * @param index Which chart to check.
	 * @return the lower limit on the y axis of the chart.
	 */
	public double getYmin(int index)
	{
		return chartAreas.get(index).yAxis.getMin();
	}
	
	/**
	 * Returns the upper limit on the y axis of the chart.
	 * @param index Which chart to check.
	 * @return the upper limit on the y axis of the chart.
	 */
	public double getYmax(int index)
	{
		return chartAreas.get(index).yAxis.getMax();
	}
	
	/**
	 * Sets a dataset for all chart areas.
	 * @param ds The dataset to add.
	 *
	 */
	public void setDataset(Dataset ds )
	{
		for(int count=0; count < chartAreas.size(); count++)
			setDataset(count, ds);
		this.packData();
	}
	
	/**
	 * Sets a dataset for a chart value.
	 * @param ds The dataset to add.
	 * @param index The chart value to which to add the dataset, indexed at 0,
	 * starting from the top.
	 */
	public void setDataset(int index, Dataset ds )
	{
		datasets[index] = ds;
		
		chartAreas.get(index).setDataset(ds);
		this.packData();
		repaint();
	}
	
	
	
	/**
	 * Sets new boundaries for the axes and displayed data of a chart.
	 * Does not change the tick parameters.  To keep a bound at its current
	 * value, use the flag CURRENT_VALUE.
	 * @param xmin Minimum of X axis.
	 * @param xmax Maximum of X axis.
	 * @param ymin Minimum of Y axis.
	 * @param ymax Maximum of Y axis.
	 * @param index The index of the chart to change.
	 */
	public void setAxisBounds(int index, double xmin, double xmax, double ymin, double ymax )
	throws IllegalArgumentException
	{
			//translates the flag CURRENT_VALUE into the actual value.
		if (xmin == CURRENT_VALUE) xmin = getXmin(index);
		if (xmax == CURRENT_VALUE) xmax = getXmax(index);
		if (ymin == CURRENT_VALUE) ymin = getYmin(index);
		if (ymax == CURRENT_VALUE) ymax = getYmax(index);
		
		chartAreas.get(index).setAxisBounds(xmin, xmax, ymin, ymax);
	}
	
	/**
	 * Sets new boundaries for the axes and displayed data of all charts.
	 * Does not change the tick parameters. To keep a bound at its current
	 * value, use the flag CURRENT_VALUE.
	 * @param xmin Minimum of X axis.
	 * @param xmax Maximum of X axis.
	 * @param ymin Minimum of Y axis.
	 * @param ymax Maximum of Y axis.
	 */
	public void setAxisBounds(double xmin, double xmax, double ymin, double ymax )
	throws IllegalArgumentException
	{
		for(int count=0; count < chartAreas.size(); count++)
			setAxisBounds(count,xmin,xmax,ymin,ymax);
	}

//	/**
//	 * Sets new values for the axis ticks by setting the number of ticks.
//	 * To retain the current value for
//	 * a tick parameter, use the flag CURRENT_VALUE.
//	 * @param bigX This many big ticks will be evenly spaced on the X axis.
//	 * @param bigY This many big ticks will be evenly spaced on the Y axis.
//	 * @param smallX Number of small ticks on the X axis between each big tick.
//	 * @param smallY Number of small ticks on the Y axis between each big tick.
//	 * @param index The index of the chart to change.
//	 */
//	public void setNumTicks(int index, int bigX, int bigY, int smallX, int smallY )
//	{	
//		//X ticks
//		if(bigX == CURRENT_VALUE && smallX != CURRENT_VALUE)
//				chartAreas.get(index).setTicksX(chartAreas.get(index).getBigTicksX(), smallX);
//		else
//		{
//			if(smallX == CURRENT_VALUE)
//				chartAreas.get(index).setNumTicksX(bigX, chartAreas.get(index).getSmallTicksX());
//			else
//				chartAreas.get(index).setNumTicksX(bigX, smallX);
//		}
//		//Y ticks
//		if(bigY == CURRENT_VALUE && smallY != CURRENT_VALUE)
//				chartAreas.get(index).setTicksY(chartAreas.get(index).getBigTicksY(), smallY);
//		else
//		{
//			if(smallY == CURRENT_VALUE)
//				chartAreas.get(index).setNumTicksY(bigY, chartAreas.get(index).getSmallTicksY());
//			else
//				chartAreas.get(index).setNumTicksY(bigY, smallY);
//		}
//	}
	
	
	
//	/**
//	 * Sets new values for the axis ticks of all charts.
//	 * To retain the current value for
//	 * a tick parameter, use the flag CURRENT_VALUE.
//	 * @param bigX Big ticks on the X axis are multiples of this.
//	 * @param bigY Big ticks on the Y axis are multiples of this.
//	 * @param smallX Number of small ticks on the X axis between each big tick.
//	 * @param smallY Number of small ticks on the Y axis between each big tick.
//	 */
//	public void setNumTicks( int bigX, int bigY, int smallX, int smallY )
//	{
//		for(int count = 0; count < chartAreas.size(); count++)
//			setNumTicks(count, bigX, bigY, smallX, smallY);
//	}
	
	
	/**
	 * Sets a new title for the graph.
	 * @param title The new title.
	 */
	public void setTitle(String title)
	{
		this.title = title;
		titleLabel.setText(title);
	}
	
	/**
	 * Sets a new title for the X axis on all charts.
	 * @param titleX New X axis title.
	 */
	public void setTitleX(String titleX)
	{
		for(int count = 0; count < chartAreas.size(); count++)
			//setTitleX(count, titleX);
			;
	}
	
	/**
	 * Sets a new title for the X axis on the given chart.
	 * @param titleX New X axis title.
	 * @param index The index of the chart to change.
	 */
	public void setTitleX(int index, String titleX)
	{
		//chartAreas.get(index).setTitleX(titleX);
	}
	
	
	
	/**
	 * Sets a new title for the Y axes of all charts.
	 * @param titleY New Y axis title.
	 */
	public void setTitleY(String titleY)
	{
		for(int count = 0; count < chartAreas.size(); count++)
			//setTitleY(count, titleY);
			;
	}
	
	
	/**
	 * Sets a new title for the Y axis on the given chart.
	 * @param index The index of the chart, starting at 0 on the top.
	 * @param titleY The new title.
	 */
	public void setTitleY(int index, String titleY)
	{
		//chartAreas.get(index).setTitleY(0, titleY);
	}
	
	
	/**
	 * Sets a new width for the bars of all charts.
	 * @param width The new width for the bars, in pixels
	 */
	public void setBarWidth(int width )
	{
		for(int count = 0; count < chartAreas.size(); count++)
			setBarWidth(count, width);
	}
	
	/**
	 * Sets a new width for the bars in the given chart.
	 * @param index The chart to change, starting at 0 at the top.
	 * @param width The new width for the bars, in pixels.
	 */
	public void setBarWidth(int index, int width)
	{
		chartAreas.get(index).setBarWidth(width);
	}
	
	/**
	 * Sets a color for the data of all charts and updates the key accordingly.
	 * In Charts with a key and multiple chart areas, this will result in
	 * a pretty useless key.
	 * @param c The new color.
	 */
	public void setColor(Color c)
	{
		for(int count = 0; count < chartAreas.size(); count++)
			setColor(count, c);
	}
	
	/**
	 * Sets a color for the data and key of the given chart.
	 * @param index Which chart to change.
	 * @param index The chart to change, starting at 0 at the top.
	 * @param c The new color.
	 */
	public void setColor(int index, Color c)
	{
		chartAreas.get(index).setForegroundColor(c);

		key.setColor(index, c);
	}
	
	/**
	 * Set the chart to draw the x-axis as a date instead
	 * of as a number...
	 * @param index The chart to act on
	 */
	public void drawXAxisAsDateTime(int index) {
		chartAreas.get(index).drawXAxisAsDateTime();
	}
	
	/**
	 * Sets all the charts' axis limits to new values that fit the dataset.
	 */
	public void packData()
	{
		packData(true,true);
	}
	
	/**
	 * Sets the given chart's axis limits to new values that fit the dataset.
	 * @param index The chart to alter.
	 */
	/*public void packData(int index)
	{
		chartAreas.get(index).pack();
	}*/
	
	/**
	 * Sets all the charts' axis limits to new values that fit the dataset.
	 * If only the Y axis is specified, packs the Y axis to fit the data that is
	 * visible with the current x values.
	 * @param packX Whether to pack the x axis.
	 * @param packY Whether to pack the y axis.
	 */
	public void packData(boolean packX, boolean packY)
	{
		for(int count = 0; count < chartAreas.size(); count++)
			packData(count, packX, packY);
	}
	
	public void packData(int index, boolean packX, boolean packY)
	{
		chartAreas.get(index).packData(packX, packY);
	}
	
	/**
	 * Tells whether the chart should display a key.
	 * @return True if the chart displays a key, false if not.
	 */
	public boolean hasKey() {
		return hasKey;
	}
	/**
	 * Sets whether the chart should display a key and updates the layout.
	 * @param hasKey True if the chart displays a key, false if not.
	 */
	public void setHasKey(boolean hasKey) {
		boolean setup = (hasKey != this.hasKey);
		this.hasKey = hasKey;
		if(setup) setupLayout();
	}
	
	
	
	/**
	 * Creates new objects for all the GUI elements and lays them out.
	 * Called when first creating Chart, or maybe afterwards if a 
	 * new type of layout is desired (e.g. more chart areas). 
	 * 
	 * Currently implemented with only one chart value.
	 * 
	 * @param titleString The title of the chart.
	 */
	protected void setupLayout()
	{
		//Border layout is good for having spacing on the sides
		//and a dynamically resizing center value (the ChartArea)
		setLayout(new BorderLayout());
		
		titleLabel = new ChartTitle(title);
		
		
		
		//	ChartArea and key layout
		ckPanel = new JPanel(); //panel for chart and key
		ckPanel.setLayout(new BoxLayout(ckPanel,BoxLayout.X_AXIS));
		
		
		chartPanel = createChartPanel();
		
		ckPanel.add(chartPanel);
		
		
		//sets up key
		if(hasKey)
		{
			key = new ChartKey(numCharts);
			ckPanel.add(key);
		}
		
		//	title box is on top
		add(titleLabel,BorderLayout.NORTH);
		
		//spacers for outside edges make everything look nicer
		add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.WEST);
		add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.SOUTH);
		add(Box.createRigidArea(new Dimension(10,10)),BorderLayout.EAST);
		
		
		add(ckPanel,BorderLayout.CENTER);
	}
	
	protected JPanel createChartPanel(){
		JPanel chartPanel = new JPanel();
		chartPanel.setLayout(new GridLayout(0, 1)); //one column of chart areas
		
		chartAreas = new ArrayList<ChartArea>();
		for (int count = 0; count < numCharts; count++) {

			if (datasets[count] != null) {
				chartAreas.add(new ChartArea(datasets[count]));
			} else {
				chartAreas.add(new ChartArea());
			}

			// chartAreas.get(count].setPreferredSize(new Dimension(500,500));
			chartPanel.add(chartAreas.get(count));
		}
		return chartPanel;
	}
	
	/**
	 * Returns the key of the upper left corner of a chart value in
	 * the Chart object's coordinate system.
	 * @param index Which chart to locate.
	 * @return A Point containing the key.
	 */
	protected Point getChartLocation(int index)
	{
		Point p = new Point();
		p.x = 10;
		p.y = titleLabel.getHeight();
		for(int count=0; count < index; count++)
			p.y += chartAreas.get(count).getHeight();
		return p;
	}
	
	public double[] getXRange() {
		double[][] bounds;
		double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE;
		for (ChartArea c : chartAreas) {
			bounds = c.findAllMinsMaxes(c.getDataset(0));
			if (bounds[0][0] < xmin) xmin = bounds[0][0];
			if (bounds[0][1] > xmax) xmax = bounds[0][1];
		}
		return new double[] {xmin, xmax};
	}
}
