package com.cck.iso;

import java.io.PrintStream;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Loggeable;

public class VISAHeader implements ISOHeader, Loggeable {
  private static final long serialVersionUID = 1L;
  
  private int headerLength;
  
  private byte[] headerBytes;
  
  private String flagFormat;
  
  private String textFormat;
  
  private String messageLength;
  
  private String destinationId;
  
  private String sourceId;
  
  private String roundTripControl;
  
  private String vipFlags;
  
  private String msgStatusFlags;
  
  private String batchNumber;
  
  private String reserved = "000";
  
  private String userInformation;
  
  private String bitmap;
  
  private String bitmapRjctGroup;
  
  public byte[] pack() {
    byte[] rslt;
    boolean isRjctBitmap = isRejectBitmap();
    if (isRjctBitmap) {
      rslt = new byte[28];
      rslt[0] = 28;
    } else {
      rslt = new byte[22];
      rslt[0] = 22;
    } 
    int idx = 1;
    idx = packDetail(rslt, this.flagFormat, idx, 1);
    idx = packDetail(rslt, this.textFormat, idx, 1);
    idx = packDetail(rslt, this.messageLength, idx, 2);
    idx = packDetail(rslt, this.destinationId, idx, 3);
    idx = packDetail(rslt, this.sourceId, idx, 3);
    idx = packDetail(rslt, this.roundTripControl, idx, 1);
    idx = packDetail(rslt, this.vipFlags, idx, 2);
    idx = packDetail(rslt, this.msgStatusFlags, idx, 3);
    idx = packDetail(rslt, this.batchNumber, idx, 1);
    idx = packDetail(rslt, this.reserved, idx, 3);
    idx = packDetail(rslt, this.userInformation, idx, 1);
    if (isRjctBitmap) {
      idx = packDetail(rslt, this.bitmap, idx, 3);
      idx = packDetail(rslt, this.bitmapRjctGroup, idx, 3);
    } 
    return rslt;
  }
  
  public void presetMessageLength(int length) {
    int formattedlength = length + 22;
    if (isRejectBitmap())
      formattedlength += 6; 
    String rawMessageLength = Integer.toHexString(formattedlength);
    try {
      this.messageLength = ISOUtil.zeropad(rawMessageLength, 4);
    } catch (Exception e) {
      this.messageLength = rawMessageLength;
    } 
  }
  
  public boolean isRejectBitmap() {
    return (this.bitmap != null);
  }
  
  private int packDetail(byte[] rslt, String value, int idx, int lgth) {
    boolean isNullValue = (value == null);
    if (value == null) {
      System.out.println("VALUE IS NULL");
      value = "";
    } 
    try {
      value = ISOUtil.zeropad(value, lgth);
      if (isNullValue)
        System.out.println("Using null value '" + value + "'"); 
    } catch (ISOException iSOException) {}
    byte[] fld = ISOUtil.hex2byte(value);
    System.arraycopy(fld, 0, rslt, idx, fld.length);
    return idx + lgth;
  }
  
  public int unpack(byte[] b) {
    int lgth = b[0];
    this.headerLength = lgth;
    lgth--;
    this.headerBytes = new byte[lgth];
    System.arraycopy(b, 1, this.headerBytes, 0, lgth);
    unpackHeaderDetails(this.headerBytes);
    return lgth + 1;
  }
  
  private byte[] extractBytes(byte[] headerbytes, int offset, int length) {
    byte[] b = new byte[length];
    System.arraycopy(headerbytes, offset, b, 0, length);
    return b;
  }
  
  private void unpackHeaderDetails(byte[] headerBytes) {
    int idx = 0;
    this.flagFormat = ISOUtil.hexString(extractBytes(headerBytes, idx, 1));
    idx++;
    this.textFormat = ISOUtil.hexString(extractBytes(headerBytes, idx, 1));
    idx++;
    this.messageLength = ISOUtil.hexString(extractBytes(headerBytes, idx, 2));
    idx += 2;
    this.destinationId = ISOUtil.hexString(extractBytes(headerBytes, idx, 3));
    idx += 3;
    this.sourceId = ISOUtil.hexString(extractBytes(headerBytes, idx, 3));
    idx += 3;
    this.roundTripControl = ISOUtil.hexString(extractBytes(headerBytes, idx, 1));
    idx++;
    this.vipFlags = ISOUtil.hexString(extractBytes(headerBytes, idx, 2));
    idx += 2;
    this.msgStatusFlags = ISOUtil.hexString(extractBytes(headerBytes, idx, 3));
    idx += 3;
    this.batchNumber = ISOUtil.hexString(extractBytes(headerBytes, idx, 1));
    idx++;
    this.reserved = ISOUtil.hexString(extractBytes(headerBytes, idx, 3));
    idx += 3;
    this.userInformation = ISOUtil.hexString(extractBytes(headerBytes, idx, 1));
    idx++;
    if (this.headerBytes.length > 21) {
      this.bitmap = ISOUtil.hexString(headerBytes, idx, 3);
      idx += 3;
      this.bitmapRjctGroup = ISOUtil.hexString(headerBytes, idx, 3);
    } 
  }
  
  public int getHeaderLength() {
    return this.headerLength;
  }
  
  public void setHeaderLength(int headerLength) {
    this.headerLength = headerLength;
  }
  
  public byte[] getHeaderBytes() {
    return this.headerBytes;
  }
  
  public void setHeaderBytes(byte[] headerBytes) {
    this.headerBytes = headerBytes;
  }
  
  public String getFlagFormat() {
    return this.flagFormat;
  }
  
  public void setFlagFormat(String flagFormat) {
    this.flagFormat = flagFormat;
  }
  
  public String getTextFormat() {
    return this.textFormat;
  }
  
  public void setTextFormat(String textFormat) {
    this.textFormat = textFormat;
  }
  
  public String getMessageLength() {
    return this.messageLength;
  }
  
  public String getDestinationId() {
    return this.destinationId;
  }
  
  public void setDestinationId(String destinationId) {
    this.destinationId = destinationId;
  }
  
  public String getSourceId() {
    return this.sourceId;
  }
  
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }
  
  public String getRoundTripControl() {
    return this.roundTripControl;
  }
  
  public void setRoundTripControl(String roundTripControl) {
    this.roundTripControl = roundTripControl;
  }
  
  public String getVipFlags() {
    return this.vipFlags;
  }
  
  public void setVipFlags(String vipFlags) {
    this.vipFlags = vipFlags;
  }
  
  public String getMsgStatusFlags() {
    return this.msgStatusFlags;
  }
  
  public void setMsgStatusFlags(String msgStatusFlags) {
    this.msgStatusFlags = msgStatusFlags;
  }
  
  public String getBatchNumber() {
    return this.batchNumber;
  }
  
  public void setBatchNumber(String batchNumber) {
    this.batchNumber = batchNumber;
  }
  
  public String getReserved() {
    return this.reserved;
  }
  
  public void setReserved(String reserved) {
    this.reserved = reserved;
  }
  
  public String getUserInformation() {
    return this.userInformation;
  }
  
  public void setUserInformation(String userInformation) {
    this.userInformation = userInformation;
  }
  
  public String getBitmap() {
    return this.bitmap;
  }
  
  public void setBitmap(String bitmap) {
    this.bitmap = bitmap;
  }
  
  public String getBitmapRjctGroup() {
    return this.bitmapRjctGroup;
  }
  
  public void setBitmapRjctGroup(String bitmapRjctGroup) {
    this.bitmapRjctGroup = bitmapRjctGroup;
  }
  
  public void setDestination(String dst) {}
  
  public String getDestination() {
    return null;
  }
  
  public void setSource(String src) {}
  
  public String getSource() {
    return null;
  }
  
  public int getLength() {
    return this.headerLength;
  }
  
  public void swapDirection() {}
  
  public Object clone() {
    return null;
  }
  
  private String formatDump(String ident, String label, String value) {
    StringBuilder sb = new StringBuilder();
    sb.append(ident);
    sb.append(String.format("%1$-25s", new Object[] { label }));
    sb.append(": ");
    sb.append(value);
    sb.append("\n");
    return sb.toString();
  }
  
  public void dump(PrintStream p, String indent) {
    p.print(formatDump(indent, "flag format", this.flagFormat));
    p.print(formatDump(indent, "text format", this.textFormat));
    if (this.messageLength == null) {
      p.print(formatDump(indent, "message length", "????"));
    } else {
      p.print(formatDump(indent, "message length", this.messageLength));
    } 
    p.print(formatDump(indent, "destination id", this.destinationId));
    p.print(formatDump(indent, "source id", this.sourceId));
    p.print(formatDump(indent, "round trip control", this.roundTripControl));
    p.print(formatDump(indent, "vip flags", this.vipFlags));
    p.print(formatDump(indent, "msg status", this.msgStatusFlags));
    p.print(formatDump(indent, "batch number", this.batchNumber));
    p.print(formatDump(indent, "reserved", this.reserved));
    p.print(formatDump(indent, "user information", this.userInformation));
    if (this.bitmap != null)
      p.print(formatDump(indent, "bitmap", this.bitmap)); 
    if (this.bitmapRjctGroup != null)
      p.print(formatDump(indent, "bitmap reject group", this.bitmapRjctGroup)); 
  }
}