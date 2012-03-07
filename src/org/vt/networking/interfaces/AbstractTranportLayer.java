package org.vt.networking.interfaces;

/**
 * This is the abstract class of Transport Layer which define 
 * the functionality of transport Layer 
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */
public abstract class AbstractTranportLayer {
	
	abstract protected void compose();
	
	abstract protected void decompose();
		
	abstract protected void multiplex();
		
	abstract protected void demultiplex();

}
