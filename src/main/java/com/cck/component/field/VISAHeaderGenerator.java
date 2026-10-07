package com.cck.component.field;

import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import com.cck.iso.VISAHeader;

@Component("fieldVH")
public class VISAHeaderGenerator extends AFieldValueGenerator {
	public static final String H_FLAG_FORMAT = "flag_format";

	public static final String H_TEXT_FORMAT = "text_format";

	public static final String H_DESTINATION_ID = "dest_id";

	public static final String H_SOURCE_ID = "source_id";

	public static final String H_ROUND_TRIP_CONTROL = "round_trip";

	public static final String H_VIP_FLAGS = "vip_flags";

	public static final String H_MSG_STATUS = "msg_status";

	public static final String H_BATCH_NUMBER = "batch_number";

	public static final String H_USER_INFORMATION = "user_info";

	public static final String H_BITMAP = "bitmap";

	public static final String H_RESERVED = "reserved";

	public static final String H_REJECT_GROUP = "rjct_group";


	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName,Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {

		Object val = msgBody.get(fd.getKey());
		if (!(val instanceof Map))
			throw new FieldGeneratorException("Invalid header parameter value for " + fd.getKey()); 
		Map<String, String> valMap = (Map<String, String>)val;
		VISAHeader header = new VISAHeader();
		header.setFlagFormat(valMap.get("flag_format"));
		header.setTextFormat(valMap.get("text_format"));
		header.setDestinationId(valMap.get("dest_id"));
		header.setSourceId(valMap.get("source_id"));
		header.setRoundTripControl(valMap.get("round_trip"));
		header.setVipFlags(valMap.get("vip_flags"));
		header.setMsgStatusFlags(valMap.get("msg_status"));
		header.setMsgStatusFlags(valMap.get("batch_number"));
		header.setUserInformation(valMap.get("user_info"));
		header.setBitmap(valMap.get("bitmap"));
		header.setBitmapRjctGroup(valMap.get("rjct_group"));
		header.setReserved(valMap.get("reserved"));

		return header;
	}

}