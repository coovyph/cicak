package com.cck.packager;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldPackager;
import org.jpos.iso.packager.GenericSubFieldPackager;

public class VISA63SubFieldPackager extends GenericSubFieldPackager{

	public VISA63SubFieldPackager() throws ISOException {
		super();
	}

	@Override
	protected ISOFieldPackager getBitMapfieldPackager() {
		return new VISA_63_BITMAP();
	}
}
