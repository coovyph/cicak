// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CicakDateUtil {
	
	public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMdd");
	public static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
	public static final DateTimeFormatter LOCAL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMddHHmmss");
	
	
	private CicakDateUtil() {}
	
	public static String formatGmtDateTime(LocalDateTime date) {
		return LOCAL_DATETIME_FORMATTER.format(date.atOffset(ZoneOffset.UTC));
	}
	
	
	public static String formatLocalDate(LocalDate date) {
		return LOCAL_DATE_FORMATTER.format(date);
	}
	
	public static String formatLocalTime(LocalDateTime dateTime) {
		return LOCAL_TIME_FORMATTER.format(dateTime);
	}
	
	
}
