package org.vt.networking.interfaces;

public interface IProtocol {
	
	public Object compose(Object obj);
	
	public Object decompose(Object obj);

}
