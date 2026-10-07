package com.cck.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jpos.iso.ISOMsg;

public class ISOFieldExpression {
  public static final String OPERATOR = "+";
  
  private List<FieldExpression> fieldExpressions = new ArrayList<>();
  
  public ISOFieldExpression(String expression) throws Exception {
    StringTokenizer tokenz = new StringTokenizer(expression, "+");
    while (tokenz.hasMoreElements()) {
      String operand = tokenz.nextToken();
      parseOperand(operand);
    } 
  }
  
  private void parseOperand(String operand) throws Exception {
    FieldExpression fieldExpression = new FieldExpression();
    fieldExpression.parse(operand);
    this.fieldExpressions.add(fieldExpression);
  }
  
  public String construct(ISOMsg msg) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < this.fieldExpressions.size(); i++) {
      FieldExpression fieldExpression = this.fieldExpressions.get(i);
      String field = fieldExpression.process(msg);
      if (!StringUtils.isEmpty(field)) {
        if (first) {
          first = false;
        } else {
          sb.append(".");
        } 
        sb.append(field);
      } 
    } 
    return sb.toString();
  }
  
  private static class FieldExpression {
    private int id;
    
    private int start = -1;
    
    private int stop = -1;
    
    public static final String OPERAND_SEPARATOR = ".";
    
    public String process(ISOMsg msg) {
      if (msg == null)
        return ""; 
      if (msg.hasField(this.id)) {
        String data = msg.getString(this.id);
        if (this.start != -1) {
          if (this.stop != -1)
            return data.substring(this.start, this.stop); 
          return data.substring(this.start);
        } 
        return data;
      } 
      return "";
    }
    
    public void parse(String operand) throws Exception {
      StringTokenizer tokenz = new StringTokenizer(operand.trim(), ".");
      if (tokenz.hasMoreElements()) {
        parseId(tokenz.nextToken());
        if (tokenz.hasMoreElements()) {
          parseStart(tokenz.nextToken());
          if (tokenz.hasMoreElements())
            parseStop(tokenz.nextToken()); 
        } 
      } else {
        parseId(operand);
      } 
    }
    
    private void parseStart(String rawId) {
      try {
        this.start = Integer.parseInt(rawId);
      } catch (Exception e) {
        this.start = -1;
      } 
    }
    
    private void parseStop(String rawId) {
      try {
        this.stop = Integer.parseInt(rawId);
      } catch (Exception e) {
        this.stop = -1;
      } 
    }
    
    private void parseId(String rawId) throws Exception {
      try {
        this.id = Integer.parseInt(rawId);
      } catch (Exception e) {
        e.printStackTrace();
        throw new Exception("Cannot parse " + rawId + " as field id");
      } 
    }
    
    public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + this.id;
      result = 31 * result + this.start;
      result = 31 * result + this.stop;
      return result;
    }
    
    public boolean equals(Object obj) {
      if (this == obj)
        return true; 
      if (obj == null)
        return false; 
      if (getClass() != obj.getClass())
        return false; 
      FieldExpression other = (FieldExpression)obj;
      if (this.id != other.id)
        return false; 
      if (this.start != other.start)
        return false; 
      if (this.stop != other.stop)
        return false; 
      return true;
    }
    
    public String toString() {
      return "FieldExpression [id=" + this.id + ", start=" + this.start + ", stop=" + this.stop + "]";
    }
    
    private FieldExpression() {}
  }
  

}
