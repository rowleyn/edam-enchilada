/**
 * 
 */
package chartlib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author olsonja
 *
 */
public class ChartArea extends AbstractMetricChartArea {
	protected ArrayList<Dataset> datasets;
	protected int barWidth = 5;
	
	/**
	 * 
	 */
	public ChartArea() {
		super();
		datasets = new ArrayList<Dataset>();
		// TODO Auto-generated constructor stub
	}
	
	public ChartArea(Dataset dataset) {
		super();
		datasets = new ArrayList<Dataset>();
		datasets.add(dataset);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	protected void drawDataLines(Graphics2D g2d,Dataset dataset){
		Rectangle dataArea = getDataAreaBounds();
		// these booleans show whether we've drawn indicators that 
		// more data exist in each direction
		boolean drawnMoreLeft = false, drawnMoreRight = false;
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(foregroundColor);
		g2d.clip(dataArea);	//constrains drawing to the data value
		g2d.setStroke(new BasicStroke(1.5f));
		
		double[] coords = new double[dataArea.width];
		
		//	loops through all data points building array of points to draw
		Iterator<DataPoint> iterator = dataset.iterator();
		while(iterator.hasNext())
		{
			DataPoint curPoint = iterator.next();
			
			double pointPos = xAxis.relativePosition(curPoint.x);
			if (pointPos < 0 && !drawnMoreLeft) {
				drawnMoreLeft = true;
				drawMorePointsIndicator(0, g2d);
			}
			else if (pointPos > 1 && !drawnMoreRight) {
				drawnMoreRight = true;
				drawMorePointsIndicator(1, g2d);
			}
			else {
				int xCoord = (int) (dataArea.x+xAxis.relativePosition(curPoint.x) * dataArea.width);
				double yCoord = (dataArea.y + dataArea.height 
						- (yAxis.relativePosition(curPoint.y) * dataArea.height));
				
				if (yCoord > 0 && yCoord <= (dataArea.y + dataArea.height) && xCoord >= 0 && xCoord < dataArea.width) {
					if (coords[xCoord] == 0 || yCoord < coords[xCoord])
						coords[xCoord] = yCoord;
				} else if (curPoint.y == -999)
					coords[xCoord] = -999.0;
			}
		}
		
		// Then draws them:
		int lastX = 0;
		double lastY = -999.0;
		int numPoints = 0;
		//boolean firstPoint = true;
		for (int i = 0; i < coords.length; i++) {
			if (coords[i] == 0)
				continue;

			int xPos = i;
			if (coords[i] != -999.0 && lastY != -999.0){
				g2d.draw(new Line2D.Double((double) lastX, lastY, (double) xPos, coords[i]));
				numPoints++;
			}
			else if (coords[i] != -999.0) {
				// Point is valid, but last point wasn't... so just draw a large point:
				g2d.draw(new Line2D.Double((double) dataArea.x+0,(dataArea.y + dataArea.height 
						- (yAxis.relativePosition(0) * dataArea.height)), (double) xPos, coords[i]));
				numPoints++;
			}
			
			lastX = xPos;
			lastY = coords[i];
		}
		
		if(lastX<=dataArea.x+coords.length-1){
			g2d.draw(new Line2D.Double((double) lastX, lastY, (double) dataArea.x+coords.length-1,(dataArea.y + dataArea.height 
				- (yAxis.relativePosition(0) * dataArea.height))));
		}
		//cleanup
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
		
	}

	/**
	 * Draws the a (small) X for each data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	protected void drawDataPoints(Graphics2D g2d, Dataset dataset) {
		Rectangle dataArea = getDataAreaBounds();
		
		/*Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		*/
		boolean drawnMoreLeft = false, drawnMoreRight = false;
		
		int maxX = 0;
		
		//	loops through all data points, drawing each one as
		//  a scatter plot...
		Iterator<DataPoint> iterator = dataset.iterator();
		while(iterator.hasNext())
		{
			DataPoint curPoint = iterator.next();
			
			double x = curPoint.y, y = curPoint.y;
			//System.out.println("X: "+x+"\tY:: "+y);
			/*if (x >= 0 && x <= 1) {
*/
				int xCoord = (int) (dataArea.x + xAxis
						.relativePosition(curPoint.x)
						* dataArea.width);
				double yCoord = (dataArea.y + dataArea.height - (yAxis
						.relativePosition(curPoint.y) * dataArea.height));
				drawPoint(g2d, xCoord, yCoord);
			/*} else if (x < 0 && !drawnMoreLeft) {
				drawnMoreLeft = true;
				drawMorePointsIndicator(0, g2d);
				// puts a null bar in the array to hold its place
				// barsTemp[index] = null;
			} else if (!drawnMoreRight) {
				drawnMoreRight = true;
				drawMorePointsIndicator(1, g2d);
			}*/
		}
		
		
		//cleanup
		/*g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
		*/
	}
	
	protected void drawPoint(Graphics2D g2d,double xCoord, double yCoord){
		//drawPointBar(g2d,xCoord,yCoord);
		drawPointX(g2d,xCoord,yCoord);
	}
	
	protected void drawPointBar(Graphics2D g2d,double xCoord, double yCoord){
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		Rectangle dataArea = getDataAreaBounds();
		Rectangle bar;
		bar = new Rectangle(
				(int)( xCoord - barWidth / 2), //centers the bar on the value
				(int)( yCoord),
				(int)(barWidth),
				(int)(-1*yCoord)+ (dataArea.y + dataArea.height) );
		
		//draw the bar
		g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(foregroundColor);				
		g2d.fill(bar);
		
		//draws border around bar
		g2d.setColor(Color.BLACK);
		g2d.draw(bar);
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}
	
	protected void drawPointX(Graphics2D g2d,double xCoord, double yCoord){
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		int radius = 3;
		g2d.draw(new Line2D.Double((double)xCoord-radius, yCoord-radius, (double)xCoord+radius, yCoord+radius));
		g2d.draw(new Line2D.Double((double)xCoord+radius, yCoord-radius, (double)xCoord-radius, yCoord+radius));
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}
	
	/* (non-Javadoc)
	 * @see chartlib.GenericChartArea#drawData(java.awt.Graphics2D)
	 */
	@Override
	protected void drawData(Graphics2D g2d) {
		// TODO Auto-generated method stub
		drawDataPoints(g2d,datasets.get(0));
	}

	
	public Dataset getDataset(int i){
		return datasets.get(i);
	}
	
	public void setDataset(int i, Dataset newDataset){
		datasets.set(i,newDataset);
	}
	
	public void setDataset(Dataset newDataset){
		datasets.clear();
		datasets.add(newDataset);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return Returns the barWidth.
	 */
	public int getBarWidth() {
		return barWidth;
	}

	/**
	 * @param barWidth The barWidth to set.
	 */
	public void setBarWidth(int barWidth) {
		this.barWidth = barWidth;
	}
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	/**
	 * @param backgroundColor The backgroundColor to set.
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	/**
	 * @return Returns the foregroundColor.
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}
	/**
	 * @param foregroundColor The foregroundColor to set.
	 */
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
}
