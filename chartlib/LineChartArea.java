package chartlib;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.Iterator;

import javax.swing.JFrame;

public class LineChartArea extends MetricChartArea {
	private Dataset dataset;
	private Color color = Color.RED;
	private AxisTitle at;
	
	public LineChartArea(Dataset dataset) {
		this.dataset = dataset;
		setPreferredSize(new Dimension(400, 400));
		at = new AxisTitle("W00t", AxisTitle.AxisPosition.LEFT, new Point(200, 200));
	}
	
	public LineChartArea(Dataset dataset, Color color) {
		this(dataset);
		this.color = color;
	}
	
	@Override
	public void drawAxes(Graphics2D g2d) {
		super.drawAxes(g2d);
		at.draw(g2d);
	}
	
	/**
	 * Draws the data in a continuous line by drawing only one
	 * data point per horizontal pixel.
	 * 
	 * @param g2d
	 */
	@Override
	public void drawData(Graphics2D g2d) {
		
		GraphAxis actualYAxis = yAxis;
		Rectangle dataArea = getDataAreaBounds();
		// these booleans show whether we've drawn indicators that 
		// more data exist in each direction
		boolean drawnMoreLeft = false, drawnMoreRight = false;
		
		Shape oldClip = g2d.getClip();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(color);
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
				int xCoord = (int) (xAxis.relativePosition(curPoint.x) * dataArea.width);
				double yCoord = (dataArea.y + dataArea.height 
						- (actualYAxis.relativePosition(curPoint.y) * dataArea.height));
				
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
		for (int i = 0; i < coords.length; i++) {
			if (coords[i] == 0)
				continue;

			int xPos = dataArea.x + i;
			
			if (coords[i] != -999.0 && lastY != -999.0)
				g2d.draw(new Line2D.Double((double) lastX, lastY, (double) xPos, coords[i]));
			else if (coords[i] != -999.0) {
				// Point is valid, but last point wasn't... so just draw a large point:

				g2d.setStroke(new BasicStroke(2.5f));
				g2d.draw(new Line2D.Double((double) xPos, coords[i], (double) xPos, coords[i]));
				g2d.setStroke(new BasicStroke(1.5f));
			}
			
			lastX = xPos;
			lastY = coords[i];
		}
		
		
		//cleanup
		g2d.setClip(oldClip);
		g2d.setStroke(oldStroke);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dataset d = new Dataset();
		d.add(new DataPoint(1, 1));
		d.add(new DataPoint(2, 2));
		d.add(new DataPoint(3, 1));
		
		LineChartArea lca = new LineChartArea(d);
		
		lca.setAxisBounds(0, 4, 0, 4);
		lca.setTicksX(1, 1);
		lca.setTicksY(1, 1);
		
		
		
		JFrame f = new JFrame("woopdy doo");
		f.getContentPane().add(lca);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setPreferredSize(new Dimension(400, 400));
		f.pack();
		f.setVisible(true);
	}

}