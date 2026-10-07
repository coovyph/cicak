package com.cck.iso;

import java.io.PrintStream;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.util.Loggeable;

public class VISAMsg extends ISOMsg {
  public byte[] pack() throws ISOException {
    byte[] dataBytes = super.pack();
    int length = dataBytes.length;
    ISOHeader header = getISOHeader();
    if (!(header instanceof VISAHeader))
      throw new ISOException("header is not instance of VISA Header"); 
    VISAHeader visaHeader = (VISAHeader)header;
    visaHeader.presetMessageLength(length);
    byte[] headerBytes = visaHeader.pack();
    byte[] rslt = new byte[length + headerBytes.length];
    System.arraycopy(headerBytes, 0, rslt, 0, headerBytes.length);
    System.arraycopy(dataBytes, 0, rslt, headerBytes.length, length);
    return rslt;
  }
  
  public int unpack(byte[] b) throws ISOException {
    VISAHeader visaHeader = new VISAHeader();
    int idx = visaHeader.unpack(b);
    byte[] srcBytes = new byte[b.length - idx];
    System.arraycopy(b, idx, srcBytes, 0, srcBytes.length);
    setHeader(visaHeader);
    return super.unpack(srcBytes);
  }
  
  public void dump(PrintStream p, String indent) {
    p.print(String.valueOf(indent) + "<" + "isomsg");
    switch (this.direction) {
      case 1:
        p.print(" direction=\"incoming\"");
        break;
      case 2:
        p.print(" direction=\"outgoing\"");
        break;
    } 
    if (this.fieldNumber != -1)
      p.print(" id=\"" + this.fieldNumber + "\""); 
    p.println(">");
    String newIndent = String.valueOf(indent) + "  ";
    if (getPackager() != null)
      p.println(
          String.valueOf(newIndent) + 
          "<!-- " + getPackager().getDescription() + " -->"); 
    if (this.header instanceof Loggeable)
      ((Loggeable)this.header).dump(p, newIndent); 
    for (int i = 0; i <= this.maxField; i++) {
      ISOComponent c;
      if ((c = (ISOComponent)this.fields.get(Integer.valueOf(i))) != null)
        c.dump(p, newIndent); 
    } 
    p.println(String.valueOf(indent) + "</" + "isomsg" + ">");
  }
  
  public static void main(String[] args) throws Exception {
    VISAMsg msg = new VISAMsg();
    msg.setPackager((ISOPackager)new ISO87APackager());
    VISAHeader header = new VISAHeader();
    header.setFlagFormat("01");
    header.setTextFormat("02");
    header.setDestinationId("000000");
    header.setSourceId("105921");
    header.setRoundTripControl("00");
    header.setVipFlags("0000");
    header.setMsgStatusFlags("000000");
    header.setBatchNumber("00");
    header.setUserInformation("00");
    msg.setHeader(header);
    msg.dump(System.out, "");
    msg.setMTI("0200");
    System.out.println(ISOUtil.hexdump(msg.pack()));
    VISAMsg msgx = (VISAMsg)msg.clone();
    msgx.setResponseMTI();
    msgx.dump(System.out, "");
  }
}
