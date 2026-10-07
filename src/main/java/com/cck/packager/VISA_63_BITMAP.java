package com.cck.packager;

import java.util.BitSet;

import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOBitMapPackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

public class VISA_63_BITMAP extends ISOBitMapPackager{

	@Override
	public int getMaxPackedLength() {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public byte[] pack(ISOComponent c) throws ISOException {
		boolean isValid = (c != null);
		BitSet bitSet = null;
		if(isValid) {
			Object objValue = c.getValue();
			if(objValue instanceof BitSet) {
				bitSet = (BitSet)objValue;
				isValid = true;
			}else {
				isValid = false;
			}
		}

		if(!isValid) {
			throw new ISOException("Cannot pack invalid BitSet information");
		}

		byte[] result = {0x00,0x00,0x00};

		for(int i=1;i<4;i++) {
			int rbyte=0;
			for(int j=7;j>=0;j--) {
				if(bitSet.get((i * 8) - j)) {
					rbyte+=powBit(j);
				}
			}
			result[i-1]=(byte)rbyte;
		}


		return result;
	}

	private int powBit(int r) {
		int result = 1;
		for(int i=0;i<r;i++) {
			result*=2;
		}
		return result;
	}

	@Override
	public int unpack(ISOComponent c, byte[] b, int offset) throws ISOException {

		if(!(c instanceof ISOBitMap)) {
			throw new ISOException("Cannot unpack ISOBitMap information");
		}

		if(b.length < (offset + 3)) {
			throw new ISOException("The data length is not enough");
		}

		ISOBitMap bitmap = (ISOBitMap)c;

		BitSet bitSet = new BitSet();

		for(int i=0;i<3;i++) {
			int val = 0;
			for(int j=7;j>=0;j--) {
				val=powBit(j);
				if( (b[offset+i] & val) == val) {
					bitSet.set(((i + 1) * 8) - j);
				}
			}
		}

		System.out.println(bitSet);

		bitmap.setValue(bitSet);

		return offset + 3;
	}

	public static void main(String[] args) throws Exception{
		ISOBitMap bitmap = new ISOBitMap(1);
		BitSet bitSet = new BitSet();
		bitSet.set(1);

		bitSet.set(8);
		bitSet.set(9);
		bitmap.setValue(bitSet);

		VISA_63_BITMAP bitmapPackager = new VISA_63_BITMAP();
		byte[] result = bitmapPackager.pack(bitmap);

		System.out.println(ISOUtil.hexString(result));

		ISOBitMap bm = new ISOBitMap(1);
		bitmapPackager.unpack(bm, result,0);
	}

}
