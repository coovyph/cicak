package com.cck.component.field;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("fieldD")
public class DateFieldGenerator extends AFieldValueGenerator{

	public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("MMdd");


	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName, Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {
		Object obj = msgBody.get(fd.getKey());
		if(!(obj instanceof String)){
			throw new FieldGeneratorException("Invalid null value for key " + fd.getKey());
		}

		String value = (String)obj;

		DateValue dv = new DateValue(value);
		return dv.getDate();
	}


	private static final class DateValue{
		private String zone="GMT+7";
		private int offset=0;
		private String format="MMdd";

		public static final DateTimeFormatter DEFAULT = DateTimeFormatter.ofPattern("MMdd");

		public DateValue(String raw) {
			if(!StringUtils.hasLength(raw))return;
			String[] raws = raw.split(";");
			if(raws.length<3)return;
			this.zone = raws[0];
			try {
				this.offset = Integer.parseInt(raws[1]);
			}catch(Exception e) {
				this.offset = 0;
			}
			this.format = raws[2];
		}

		public String getDate() {
			LocalDateTime localDateTime = null;
			try {
				localDateTime = LocalDateTime.now(ZoneId.of(this.zone));
			}catch(Exception e) {
				localDateTime = localDateTime.now();
			}

			DateTimeFormatter formatter = null;
			try {
				formatter = DateTimeFormatter.ofPattern(this.format);
			}catch(Exception e) {
				formatter = DEFAULT;
			}

			localDateTime = localDateTime.plusDays(this.offset);

			return localDateTime.format(formatter);
		}

	}

}
