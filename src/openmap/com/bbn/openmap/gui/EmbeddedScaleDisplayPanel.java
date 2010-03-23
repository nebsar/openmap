/* **********************************************************************
 *
 *  ROLANDS & ASSOCIATES Corporation
 *  500 Sloat Avenue
 *  Monterey, CA 93940
 *  (831) 373-2025
 *
 *  Copyright (C) 2002, 2003 ROLANDS & ASSOCIATES Corporation. All rights reserved.
 *  Openmap is a trademark of BBN Technologies, A Verizon Company
 *
 *
 * **********************************************************************
 *
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/ScaleDisplayLayer.java,v $
 * $Revision: 1.9 $
 * $Date: 2005/12/09 21:09:08 $
 * $Author: dietrick $
 *
 * **********************************************************************
 */

package com.bbn.openmap.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.beans.PropertyVetoException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * A panel that listens for projection changes and draws a little legend line on
 * itself with a distance.  Can be modified with Properties.
 * <pre>
 * 
 * unitOfMeasure=km (or any value from the Length class)
 * locationXoffset=-10 (value off the edge of the component to have legend)
 * locationYoffset=-10 
 * width=pixel width of component
 * height=pixel height of component
 * </pre>
 */
public class EmbeddedScaleDisplayPanel extends OMComponentPanel implements
		ProjectionListener {

	public EmbeddedScaleDisplayPanel() {
		super();
		setOpaque(false);
		setBackground(OMColor.clear);
		setPreferredSize(new Dimension(350, 100));

		dAttributes.setMattingPaint(new Color(0xffcccccc));
		dAttributes.setMatted(true);
	}

	public EmbeddedScaleDisplayPanel(DrawingAttributes dAtts, Length units) {
		this();
		setUnitOfMeasure(units.getAbbr());
		dAttributes = dAtts;
	}

	// Default colors to use, if not specified in the properties.
	protected String defaultLineColorString = "FFFFFF";
	protected String defaultTextColorString = "FFFFFF";
	protected String defaultUnitOfMeasureString = "km";
	protected int defaultLocationXoffset = -10;
	protected int defaultLocationYoffset = -10;
	protected int defaultWidth = 150;
	protected int defaultHeight = 10;

	// property text values
	public static final String UnitOfMeasureProperty = "unitOfMeasure";
	public static final String LocationXOffsetProperty = "locationXoffset";
	public static final String LocationYOffsetProperty = "locationYoffset";
	public static final String WidthProperty = "width";
	public static final String HeightProperty = "height";

	protected Length uom = Length.get(defaultUnitOfMeasureString);
	protected int locationXoffset = defaultLocationXoffset;
	protected int locationYoffset = defaultLocationYoffset;
	protected int width = defaultWidth;
	protected int height = defaultHeight;

	protected DrawingAttributes dAttributes = DrawingAttributes
			.getDefaultClone();
	private static final long serialVersionUID = 1L;
	static final float RADIANS_270 = Length.DECIMAL_DEGREE.toRadians(270);
	protected OMGraphicList legend;

	public static Logger logger = Logger
			.getLogger("com.bbn.openmap.gui.EmbedddedScaleDisplayPanel");

	/**
	 * Sets the properties for the <code>Layer</code>. This allows
	 * <code>Layer</code> s to get a richer set of parameters than the
	 * <code>setArgs</code> method.
	 * 
	 * @param prefix
	 *            the token to prefix the property names
	 * @param properties
	 *            the <code>Properties</code> object
	 */
	public void setProperties(String prefix, Properties properties) {
		super.setProperties(prefix, properties);
		prefix = com.bbn.openmap.util.PropUtils.getScopedPropertyPrefix(prefix);

		dAttributes.setProperties(prefix, properties);

		String unitOfMeasure = properties.getProperty(prefix
				+ UnitOfMeasureProperty);
		setUnitOfMeasure(unitOfMeasure);

		locationXoffset = PropUtils.intFromProperties(properties, prefix
				+ LocationXOffsetProperty, defaultLocationXoffset);

		locationYoffset = PropUtils.intFromProperties(properties, prefix
				+ LocationYOffsetProperty, defaultLocationYoffset);

		width = PropUtils.intFromProperties(properties, prefix + WidthProperty,
				defaultWidth);

		height = PropUtils.intFromProperties(properties, prefix
				+ HeightProperty, defaultHeight);
	}

	/**
	 * Getter for property unitOfMeasure.
	 * 
	 * @return Value of property unitOfMeasure.
	 */
	public String getUnitOfMeasure() {
		return this.uom.toString();
	}

	/**
	 * Setter for property unitOfMeasure.
	 * 
	 * @param unitOfMeasure
	 *            New value of property unitOfMeasure.
	 * 
	 * @throws PropertyVetoException
	 */
	public void setUnitOfMeasure(String unitOfMeasure) {
		if (unitOfMeasure == null)
			unitOfMeasure = Length.KM.toString();

		// There is a bug in the Length.get() method that will not
		// return
		// the correct (or any value) for a requested uom.
		// This does not work:
		// uom = com.bbn.openmap.proj.Length.get(unitOfMeasure);

		// Therefore, The following code correctly obtains the proper
		// Length object.

		Length[] choices = Length.getAvailable();
		uom = null;
		for (int i = 0; i < choices.length; i++) {
			if (unitOfMeasure.equalsIgnoreCase(choices[i].toString())
					|| unitOfMeasure.equalsIgnoreCase(choices[i].getAbbr())) {
				uom = choices[i];
				break;
			}
		}

		// of no uom is found assign Kilometers as the default.
		if (uom == null)
			uom = Length.KM;
	}

	javax.swing.Box palette;
	JRadioButton meterRadioButton;
	JRadioButton kmRadioButton;
	JRadioButton dmRadioButton;
	JRadioButton nmRadioButton;
	JRadioButton mileRadioButton;
	JRadioButton degRadioButton;
	javax.swing.ButtonGroup uomButtonGroup;

	private JPanel jPanel3;
	private JPanel jPanel2;
	private JPanel jPanel1;

	/** Creates the interface palette. */
	public java.awt.Component getGUI() {

		if (palette == null) {
			if (com.bbn.openmap.util.Debug.debugging("graticule"))
				com.bbn.openmap.util.Debug
						.output("GraticuleLayer: creating Graticule Palette.");

			palette = javax.swing.Box.createVerticalBox();
			uomButtonGroup = new javax.swing.ButtonGroup();
			jPanel1 = new JPanel();
			jPanel2 = new JPanel();
			jPanel3 = new JPanel();
			kmRadioButton = new JRadioButton();
			meterRadioButton = new JRadioButton();
			dmRadioButton = new JRadioButton();
			nmRadioButton = new JRadioButton();
			mileRadioButton = new JRadioButton();
			degRadioButton = new JRadioButton();

			jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1,
					javax.swing.BoxLayout.Y_AXIS));

			jPanel2.setBorder(new javax.swing.border.TitledBorder(
					"Unit Of Measure"));
			kmRadioButton.setText("KM");
			kmRadioButton.setToolTipText("Kilometers");
			uomButtonGroup.add(kmRadioButton);
			jPanel3.add(kmRadioButton);

			meterRadioButton.setText("M");
			meterRadioButton.setToolTipText("Meters");
			uomButtonGroup.add(meterRadioButton);
			jPanel3.add(meterRadioButton);

			dmRadioButton.setText("DM");
			dmRadioButton.setToolTipText("Data Miles");
			uomButtonGroup.add(dmRadioButton);
			jPanel3.add(dmRadioButton);

			nmRadioButton.setText("NM");
			nmRadioButton.setToolTipText("Nautical Miles");
			uomButtonGroup.add(nmRadioButton);
			jPanel3.add(nmRadioButton);

			mileRadioButton.setText("Mile");
			mileRadioButton.setToolTipText("Statute Miles");
			uomButtonGroup.add(mileRadioButton);
			jPanel3.add(mileRadioButton);

			degRadioButton.setText("Deg");
			degRadioButton.setToolTipText("Decimal Degrees");
			uomButtonGroup.add(degRadioButton);
			jPanel3.add(degRadioButton);

			jPanel2.add(jPanel3);

			jPanel1.add(jPanel2);

			palette.add(jPanel1);

			java.awt.event.ActionListener al = new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String ac = e.getActionCommand();

					if (ac.equalsIgnoreCase(UnitOfMeasureProperty)) {
						JRadioButton jrb = (JRadioButton) e.getSource();

						// Update the length of the line, too.
						setUnitOfMeasure(jrb.getText());
					} else {
						com.bbn.openmap.util.Debug
								.error("Unknown action command \""
										+ ac
										+ "\" in GraticuleLayer.actionPerformed().");
					}
				}
			};

			kmRadioButton.addActionListener(al);
			kmRadioButton.setActionCommand(UnitOfMeasureProperty);
			meterRadioButton.addActionListener(al);
			meterRadioButton.setActionCommand(UnitOfMeasureProperty);
			dmRadioButton.addActionListener(al);
			dmRadioButton.setActionCommand(UnitOfMeasureProperty);
			nmRadioButton.addActionListener(al);
			nmRadioButton.setActionCommand(UnitOfMeasureProperty);
			mileRadioButton.addActionListener(al);
			mileRadioButton.setActionCommand(UnitOfMeasureProperty);
			degRadioButton.addActionListener(al);
			degRadioButton.setActionCommand(UnitOfMeasureProperty);
		}
		if (uom.equals(Length.KM)) {
			kmRadioButton.setSelected(true);
		} else if (uom.equals(Length.METER)) {
			meterRadioButton.setSelected(true);
		} else if (uom.equals(Length.DM)) {
			dmRadioButton.setSelected(true);
		} else if (uom.equals(Length.NM)){
			nmRadioButton.setSelected(true);
		} else if (uom.equals(Length.MILE)){
			mileRadioButton.setSelected(true);
		} else if (uom.equals(Length.DECIMAL_DEGREE)) {
			degRadioButton.setSelected(true);
		}
		return palette;
	}

	public void projectionChanged(ProjectionEvent e) {
		int w, h, left_x = 0, right_x = 0, lower_y = 0, upper_y = 0;
		Projection projection = e.getProjection();
		OMGraphicList graphics = new OMGraphicList();

		w = projection.getWidth();
		h = projection.getHeight();
		// FIXME: Use the center since it's always real

		/**
		 * Since the pixel space for the component has nothing to do with the
		 * pixel space of the projection, we'll just use the projection pixel
		 * space to find out how long the line should be. Then, we'll move that
		 * length into component pixel space.
		 */

		lower_y = h / 2;
		right_x = w / 2;
		left_x = right_x - width;

		LatLonPoint loc1 = projection.inverse(left_x, lower_y,
				new LatLonPoint.Double());
		LatLonPoint loc2 = projection.inverse(right_x, lower_y,
				new LatLonPoint.Double());

		double dist = GreatCircle.sphericalDistance(loc1.getRadLat(), loc1
				.getRadLon(), loc2.getRadLat(), loc2.getRadLon());

		// Round the distance to one of the preferred values.
		dist = uom.fromRadians(dist);
		double new_dist = scopeDistance(dist);

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("modifying " + dist + " to new distance: " + new_dist);
		}

		left_x = getPtAtDistanceFromLatLon(loc2, new_dist, projection, uom);

		int lineLength = right_x - left_x;

		// If the length of the distance line is longer than the width of the
		// panel, divide it in half.
		if (lineLength > getWidth() - Math.abs(locationXoffset) * 2) {

			lineLength /= 3;
			new_dist /= 3.0;

			if (logger.isLoggable(Level.FINE)) {
				logger
						.fine("length of line too long, halving to "
								+ lineLength);
			}
			double testDist = scopeDistance(new_dist);
			if (testDist != new_dist) {
				lineLength = right_x
						- getPtAtDistanceFromLatLon(loc2, testDist, projection,
								uom);
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("needed to rescope distance to " + testDist
							+ " from " + new_dist);
				}
				new_dist = testDist;
			}

		}

		// Now, check the units and try to avoid fractions
		Length cur_uom = uom;

		if (new_dist < 1) {
			if (uom.equals(Length.KM)) {
				new_dist *= 1000;
				cur_uom = Length.METER;
			} else if (uom.equals(Length.MILE)) {
				new_dist = Length.FEET.fromRadians(Length.MILE
						.toRadians(new_dist));
				cur_uom = Length.FEET;
			}

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("modified UOM to " + cur_uom.getAbbr()
						+ ", value: " + new_dist);
			}

			double testDist = scopeDistance(new_dist);
			if (testDist != new_dist) {
				lineLength = right_x
						- getPtAtDistanceFromLatLon(loc2, testDist, projection,
								cur_uom);
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("needed to rescope distance to " + testDist
							+ " from " + new_dist);
				}
				new_dist = testDist;
			}
		}

		/**
		 * Now, figure out where OMGraphics go in the component space.
		 */
		if (locationXoffset < 0) {
			int cw = getWidth();
			left_x = cw + locationXoffset - lineLength;
			right_x = cw + locationXoffset;
		} else if (locationXoffset >= 0) {
			left_x = locationXoffset;
			right_x = locationXoffset + lineLength;
		}
		if (locationYoffset < 0) {
			int ch = getHeight();
			upper_y = ch + locationYoffset - height;
			lower_y = ch + locationYoffset;
		} else if (locationYoffset >= 0) {
			upper_y = locationYoffset;
			lower_y = locationYoffset + height;
		}

		// Draw the lines and the rounded distance string.
		OMLine line = new OMLine(left_x, lower_y, right_x, lower_y);
		dAttributes.setTo(line);
		graphics.add(line);

		line = new OMLine(left_x, lower_y, left_x, upper_y);
		dAttributes.setTo(line);
		graphics.add(line);

		line = new OMLine(right_x, lower_y, right_x, upper_y);
		dAttributes.setTo(line);
		graphics.add(line);

		// String outtext;
		// if (new_dist < 1.0f) {
		// outtext = String.format("%.3f %s", new_dist, cur_uom.getAbbr());
		// } else if (new_dist < 10.0f) {
		// outtext = String.format("%.2f %s", new_dist, cur_uom.getAbbr());
		// } else if (new_dist < 100.0f) {
		// outtext = String.format("%.1f %s", new_dist, cur_uom.getAbbr());
		// } else {
		String outtext = String.format("%.0f %s", new_dist, cur_uom.getAbbr());
		// }

		OMText text = new OMText(right_x, lower_y - 20, "" + outtext,
				OMText.JUSTIFY_RIGHT);

		Font font = text.getFont();
		text.setFont(font.deriveFont(font.getStyle(), font.getSize() + 4));

		dAttributes.setTo(text);
		text.setTextMatteColor((Color) dAttributes.getMattingPaint());
		text.setTextMatteStroke(new BasicStroke(5));
		text.setMattingPaint(OMColor.clear);
		graphics.add(text);
		graphics.generate(projection);

		setLegend(graphics);

	}

	protected int getPtAtDistanceFromLatLon(LatLonPoint loc2, double unitDist,
			Projection projection, Length uom) {
		double lineWidthInRadians = uom.toRadians(unitDist);
		LatLonPoint newX = GreatCircle.sphericalBetween(loc2.getRadLat(), loc2
				.getRadLon(), lineWidthInRadians, RADIANS_270);
		Point2D newLoc1 = projection.forward(newX);
		return (int) Math.round(newLoc1.getX());
	}

	/**
	 * Take a given distance and round it down to the nearest 1, 2, or 5 (or
	 * tens/hundreds version of those increments) multiple of that number.
	 * 
	 * @param dist
	 * @return
	 */
	protected double scopeDistance(double dist) {
		double new_dist;
		if (dist <= .01) {
			new_dist = .01;
		} else if (dist <= .02) {
			new_dist = .02;
		} else if (dist <= .05) {
			new_dist = .05;
		} else if (dist <= .1) {
			new_dist = .1;
		} else if (dist <= .2) {
			new_dist = .2;
		} else if (dist <= .5) {
			new_dist = .5;
		} else if (dist <= 1) {
			new_dist = 1;
		} else if (dist <= 2) {
			new_dist = 2;
		} else if (dist <= 5) {
			new_dist = 5;
		} else if (dist <= 10) {
			new_dist = 10;
		} else if (dist <= 20) {
			new_dist = 20;
		} else if (dist <= 50) {
			new_dist = 50;
		} else if (dist <= 100) {
			new_dist = 100;
		} else if (dist <= 200) {
			new_dist = 200;
		} else if (dist <= 500) {
			new_dist = 500;
		} else {
			new_dist = 1000;
		}
		return new_dist;
	}

	public OMGraphicList getLegend() {
		return legend;
	}

	public void setLegend(OMGraphicList legend) {
		this.legend = legend;
	}

	public void paint(Graphics g) {
		if (legend != null) {
			legend.render(g);
		}
	}

	protected MapBean mapBean;

	public void findAndInit(Object someObj) {
		super.findAndInit(someObj);

		if (someObj instanceof MapBean) {
			mapBean = (MapBean) someObj;
			mapBean.addProjectionListener(this);
		}
	}

	public void findAndUndo(Object someObj) {
		super.findAndUndo(someObj);

		if (someObj.equals(mapBean)) {
			mapBean.removeProjectionListener(this);
			mapBean = null;
		}
	}
}