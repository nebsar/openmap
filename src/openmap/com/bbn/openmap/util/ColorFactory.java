// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/ColorFactory.java,v $
// $RCSfile: ColorFactory.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.util;


import java.awt.Color;
import java.awt.Paint;
import java.util.Properties;

import com.bbn.openmap.omGraphics.OMColor;

/** 
 * ColorFactory creates instances of colors.  This class has methods
 * which create <code>java.awt.Color</code> or
 * <code>com.bbn.openmap.omGraphics.OMColor</code> objects.  This
 * class handles creation of Color objects in a Java version-neutral
 * way by using reflection to create the java.awt.Color object.  This
 * way we support the extended Java 2 platform without sacrificing
 * support for JDK 1.1.X.
 * <p>
 * NOTE: For general RGB-colored graphics, you should directly use the
 * java.awt.Color 1.1.X constructors.  If you are interested in using
 * alpha-valued colors, or using colors in a (JDK) version-neutral
 * way, then read on...
 * <p>
 * You may want to use this class because:
 * <ul>
 * <li>You want to use alpha-valued colors in your images under both
 * JDK 1.1 and JDK 1.2.  (JDK1.1 has limited support for alpha-valued
 * images).  You can use the appropriate <code>createColor()</code>
 * methods in this class with the <code>forceAlpha</code> set to
 * <code>true</code> and you are guaranteed (almost!) to get back an
 * alpha-valued color.
 * <p>
 * <li>You want to use alpha-valued colors for generic graphics, but
 * only in a version-neutral way and if the support is available
 * (e.g., you're running under the Java 2 platform).  In this case you
 * can call the appropriate <code>createColor()</code> methods without
 * the <code>forceAlpha</code> argument, or with
 * <code>forceAlpha</code> set to false.
 * </ul>
 * @see com.bbn.openmap.omGraphics.OMColor
 */
public class ColorFactory {

    /** 
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see #parseColor(String, boolean)
     */
    public static Color parseColorFromProperties(Properties p, 
						 String propName, 
						 String dfault)
	throws NumberFormatException
    {
	String colorString = p.getProperty(propName, dfault);
	return parseColor(colorString, false);
    }

    /** 
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @param forceAlpha force using alpha value
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see #parseColor(String, boolean)
     */
    public static Color parseColorFromProperties(Properties p, 
						 String propName, 
						 String dfault,
						 boolean forceAlpha)
	throws NumberFormatException
    {
	String colorString = p.getProperty(propName, dfault);
	return parseColor(colorString, forceAlpha);
    }

    /** 
     * Take a string from a properties file, representing the 24bit
     * RGB or 32bit ARGB hex values for a color, and convert it to a
     * java.awt.Color.
     * @param p properties
     * @param propName the name of the property
     * @param dfault color to use if the property value doesn't work
     * @param forceAlpha force using alpha value
     * @return java.awt.Color
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see #parseColor(String, boolean)
     */
    public static Paint parseColorFromProperties(Properties p, 
						 String propName, 
						 Paint dfault)
	throws NumberFormatException
    {
	String colorString = p.getProperty(propName);
	if (colorString != null) {
	    return parseColor(colorString, false);
	}
	return dfault;
    }

    /**
     * Convert a string representing a 24/32bit hex color value into a
     * Color value.
     * @param colorString the 24/32bit hex string value (ARGB)
     * @return java.awt.Color (24bit RGB on JDK 1.1, 24/32bit ARGB on
     * JDK1.2)
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see #parseColor(String, boolean)
     */
    public static Color parseColor(String colorString)
	throws NumberFormatException
    {
	return parseColor(colorString, false);
    }

    /**
     * Convert a string representing a 24/32bit hex color value into a
     * Color value.
     * NOTE:
     * <ul>
     * <li>Only 24bit (RGB) java.awt.Color is supported on the JDK 1.1
     * platform.
     * <li>Both 24/32bit (ARGB) java.awt.Color is supported on the Java 2
     * platform.
     * </ul>
     * @param colorString the 24/32bit hex string value (ARGB)
     * @param forceAlpha force using alpha value
     * @return java.awt.Color (24bit RGB on JDK 1.1, 24/32bit ARGB on
     * JDK1.2)
     * @exception NumberFormatException if the specified string
     * cannot be interpreted as a hexidecimal integer
     * @see #createColor(int, boolean)
     */
    public static Color parseColor(String colorString, boolean forceAlpha)
	throws NumberFormatException
    {

	int value;

	try {
	    value = (int)Long.parseLong(colorString, 16);
	} catch (NumberFormatException nfe) {
	    value = Long.decode(colorString).intValue();
	    // If decode can't catch it, throw an Exception...
	}

	// We want to test for this - if the length of the colorString
	// is less than 7, then the caller probably doesn't care about
	// transparency and wants the color to be opaque.  However,
	// "0" is a common number for clear, and should be
	// transparent.
	if (colorString.length() < 7 && !colorString.equals("0")) {
	    // Just a RGB value, use regular JDK1.1 constructor
	    return new Color(value);
	}
	return createColor(value, forceAlpha);
    }


    /**
     * Create a Color.
     * @param red red component (0.0-1.0)
     * @param green green component (0.0-1.0)
     * @param blue blue component (0.0-1.0)
     * @param alpha alpha component (0.0-1.0)
     * @return Color or OMColor that has an ARGB value
     * @see #createColor(int, boolean)
     */
    public static Color createColor(float red, float green, 
				    float blue, float alpha) {
	return createColor(red, green, blue, alpha, false);
    }


    /**
     * Create a Color.
     * @param red red component (0.0-1.0)
     * @param green green component (0.0-1.0)
     * @param blue blue component (0.0-1.0)
     * @param alpha alpha component (0.0-1.0)
     * @param forceAlpha force using alpha value
     * @return Color or OMColor that has an ARGB value
     * @see #createColor(int, boolean)
     */
    public static Color createColor(
	    float red, float green, float blue, float alpha,
	    boolean forceAlpha)
    {
	int value =
	    (((int)(alpha*255)&0xff) << 24) |
	    (((int)(red*255)&0xff) << 16) |
	    (((int)(green*255)&0xff) << 8) |
	      (int)(blue*255)&0xff;
	return createColor(value, forceAlpha);
    }


    /**
     * Create a Color.
     * @param red red component (0-255)
     * @param green green component (0-255)
     * @param blue blue component (0-255)
     * @param alpha alpha component (0-255)
     * @return Color or OMColor that has an ARGB value
     * @see #createColor(int, boolean)
     */
    public static Color createColor(
	    int red, int green, int blue, int alpha)
    {
	return createColor(red, green, blue, alpha, false);
    }


    /**
     * Create a Color.
     * @param red red component (0-255)
     * @param green green component (0-255)
     * @param blue blue component (0-255)
     * @param alpha alpha component (0-255)
     * @param forceAlpha force using alpha value
     * @return Color or OMColor that has an ARGB value
     * @see #createColor(int, boolean)
     */
    public static Color createColor(
	    int red, int green, int blue, int alpha,
	    boolean forceAlpha)
    {
	int value =
	    ((alpha&0xff) << 24) | ((red&0xff) << 16) | ((green&0xff) << 8) | blue&0xff;
	return createColor(value, forceAlpha);
    }


    /**
     * Create a Color.
     * @param value 32bit ARGB color value
     * @return Color or OMColor that has an ARGB value
     * @see #createColor(int, boolean)
     */
    public static Color createColor(int value) {
	return createColor(value, false);
    }


    /**
     * Create a Color.
     * @param value 32bit ARGB color value
     * @param forceAlpha force using alpha value
     * the underlying java.awt.Color supports it
     * @return Color or OMColor that has an ARGB value
     */
    public static Color createColor(int value, boolean forceAlpha) {
	if (!OMColor.nativeAlpha) {
	    return (forceAlpha) ? new OMColor(value) : new Color(value);
	}
	// alpha is supported by the Java VM.
	try {
	    // we prefer the generic Java 2 solution (supports alpha)
	    return (Color)OMColor.alphaValueConstructor.newInstance(new Object[] {
			new Integer(value), new Boolean(true)
		    });
	} catch (Exception e) {
	    System.err.println("ColorFactory.parseColor(): internal error...");
	    return (forceAlpha) ? new OMColor(value) : new Color(value);
	}
    }

    public static String getHexColorString(Color color) {
	return Integer.toHexString((color.getRGB() & 0x00FFFFFF) | 
				   (color.getAlpha() << 24));
    }
}