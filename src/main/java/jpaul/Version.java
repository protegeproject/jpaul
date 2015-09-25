// Function.java, created Mon Feb 14 21:16:34 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the Modified BSD Licence; see COPYING for details.
package jpaul;

/**
 * <code>Version</code> contains fields naming the current
 * <code>jpaul</code> version.  We could have placed this information
 * in a manifest file, but putting it here allows Java code to access
 * it easily.
 *
 * <p>Run <code>java jpaul.Version</code> to find out the
 * <code>jpaul</code> release you're using.
 * 
 * @author  Alexandru Salcianu - salcianu@alum.mit.edu
 * @version $Id: Version.java,v 1.14 2006/03/24 22:14:10 salcianu Exp $ */
public abstract class Version {
    
    /** The long name of the <code>jpaul</code> project. */
    public static final String LONG_NAME = "JPAUL - Java Program Analysis Utilities Library";

    /** The release number of the <code>jpaul</code> library. */
    public static final String RELEASE = "2.5.1";

    /** The motivation behind the entire <code>jpaul</code> project. */
    public static final String CREDO = 
	"\"Papers have been written enough, let us see systems!\" - Reinhard Wilhelm";

    /** The person in charge of this <code>jpaul</code> release. */
    public static final String MAINTAINER = "Alex Salcianu <salcianu@users.sourceforge.net>";

    /** The project website (with Javadoc, info, etc.) */
    public static final String WEBSITE = "http://jpaul.sourceforge.net";

    
    /** Prints a small informative message about the current release. */
    public static void main(String[] args) {
	System.out.println(LONG_NAME + " - Release " + RELEASE);
	System.out.println();
	System.out.println("  " + CREDO);
	System.out.println();
	System.out.println("Project maintainer: " + MAINTAINER);
	System.out.println("Javadoc accessible from the official website on SourceForge:\n\t" + WEBSITE);
	System.out.println("Submit bug reports / feature requests on the website.");
    }

}
