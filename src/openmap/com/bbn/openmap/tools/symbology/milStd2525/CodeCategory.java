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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/symbology/milStd2525/CodeCategory.java,v $
// $RCSfile: CodeCategory.java,v $
// $Revision: 1.4 $
// $Date: 2003/12/18 19:11:11 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.tools.symbology.milStd2525;

import java.util.ArrayList;
import java.util.List;

/**
 * The CodeCategory CodePosition is used by Tactical Graphics to note
 * the types of activities that a symbol represents - tasks, general
 * maneuvers, support.  The CodeCategory is represented by the third
 * position of the symbol code of tactical graphic symbols.
 */
public class CodeCategory extends CodePosition {

    public CodeCategory() {
	super("Category", 3, 3);
    }

}
