// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

public class EMVTag {
	public static final int _4F_APPLICATION_IDENTIFIER = 0x4F;

	public static final int _50_APPLICATION_LABEL = 0x50;

	public static final int _57_TRACK2_EQUIVALENT_DATA = 0x57;

	public static final int _5A_APPLICATION_PAN = 0x5A;

	public static final int _5F20_CARDHOLDER_NAME = 0x5F20;

	public static final int _5F24_APPLICATION_EXPIRY_DATE = 0x5F24;

	public static final int _5F25_APPLICATION_EFFECTIVE_DATE = 0x5F25;

	public static final int _5F2A_TRANSACTION_CURRENCY_CODE = 0x5F2A;

	public static final int _5F28_ISSUER_COUNTRY_CODE = 0x5F28;

	public static final int _5F30_SERVICE_CODE = 0x5F30;

	public static final int _5F34_APPLICATION_PAN_SEQ_NR = 0x5F34;

	public static final int _6F_FCI_TEMPLATE = 0x6F;

	public static final int _70_AEF_DATA_TEMPLATE = 0x70;

	public static final int _71_ISSUER_SCRIPT_TEMPLATE_1 = 0x71;

	public static final int _72_ISSUER_SCRIPT_TEMPLATE_2 = 0x72;

	public static final int _77_RSP_MSG_TEMPLATE_FORMAT_2 = 0x77;

	public static final int _80_RSP_MSG_TEMPLATE_FORMAT_1 = 0x80;

	public static final int _82_APPLICATION_INTERCHANGE_PROFILE = 0x82;

	public static final int _83_COMMAND_TEMPLATE = 0x83;

	public static final int _84_DF_NAME = 0x84;

	public static final int _87_APPLICATION_PRIORITY_INDICATOR = 0x87;

	public static final int _89_AUTHORISATION_CODE = 0x89;

	public static final int _8A_AUTHORIZATION_RESPONSE_CODE = 0x8A;

	public static final int _8C_CDOL1 = 0x8C;

	public static final int _8D_CDOL2 = 0x8D;

	public static final int _8E_CVM_LIST = 0x8E;

	public static final int _8F_CA_PUBLIC_KEY_INDEX = 0x8F;

	public static final int _90_ISSUER_PUBLIC_KEY_CERTIFICATE = 0x90;

	public static final int _91_ISSUER_AUTHENTICATION_DATA = 0x91;

	public static final int _92_ISSUER_PUBLIC_KEY_REMAINDER = 0x92;

	public static final int _93_SIGNED_STATIC_APPLICATION_DATA = 0x93;

	public static final int _94_APPLICATION_FILE_LOCATOR = 0x94;

	public static final int _95_TERMINAL_VERIFICATION_RESULTS = 0x95;

	public static final int _9A_TRANSACTION_DATE = 0x9A;

	public static final int _9B_TRANSACTION_STATUS_INFORMATION = 0x9B;

	public static final int _9C_TRANSACTION_TYPE = 0x9C;

	public static final int _9F02_AMOUNT_AUTHORIZED_NUMERIC = 0x9F02;

	public static final int _9F03_AMOUNT_OTHER_NUMERIC = 0x9F03;

	public static final int _9F06_APPLICATION_IDENTIFIER = 0x9F06;

	public static final int _9F07_APPLICATION_USAGE_CONTROL = 0x9F07;

	public static final int _9F08_APPLICATION_VERSION_NR_ICC = 0x9F08;

	public static final int _9F09_TERM_APPLICATION_VERSION_NR = 0x9F09;

	public static final int _9F0D_ISSUER_ACTION_CODE_DEFAULT = 0x9F0D;

	public static final int _9F0E_ISSUER_ACTION_CODE_DENIAL = 0x9F0E;

	public static final int _9F0F_ISSUER_ACTION_CODE_ONLINE = 0x9F0F;

	public static final int _9F10_ISSUER_APPLICATION_DATA = 0x9F10;

	public static final int _9F13_LAST_ONLINE_ATC_REGISTER = 0x9F13;

	public static final int _9F14_LOWER_CONSEC_OFFLINE_LIMIT = 0x9F14;

	public static final int _9F15_MERCHANT_CATEGORY_CODE = 0x9F15;

	public static final int _9F17_PIN_TRY_COUNTER = 0x9F17;

	public static final int _9F1A_TERMINAL_COUNTRY_CODE = 0x9F1A;

	public static final int _9F1B_TERMINAL_FLOOR_LIMIT = 0x9F1B;

	public static final int _9F1C_TERMINAL_IDENTIFICATION = 0x9F1C;

	public static final int _9F1E_IFD_SERIAL_NUMBER = 0x9F1E;

	public static final int _9F20_TRACK_2_DISCRETIONARY_DATA = 0x9F20;

	public static final int _9F21_TRANSACTION_TIME = 0x9F21;

	public static final int _9F23_UPPER_CONSEC_OFFLINE_LIMIT = 0x9F23;

	public static final int _9F26_APPLICATION_CRYPTOGRAM = 0x9F26;

	public static final int _9F27_CRYPTOGRAM_INFORMATION_DATA = 0x9F27;

	public static final int _9F2D_ICC_PIN_ENCR_PUB_KEY_CERTIFICATE = 0x9F2D;

	public static final int _9F2E_ICC_PIN_ENCR_PUB_KEY_EXPONENT = 0x9F2E;

	public static final int _9F2F_ICC_PIN_ENCR_PUB_KEY_REMAINDER = 0x9F2F;

	public static final int _9F32_ISSUER_PUBLIC_KEY_EXPONENT = 0x9F32;

	public static final int _9F33_TERMINAL_CAPABILITIES = 0x9F33;

	public static final int _9F34_CVM_RESULTS = 0x9F34;

	public static final int _9F35_TERMINAL_TYPE = 0x9F35;

	public static final int _9F36_APPLICATION_TRANSACTION_COUNTER = 0x9F36;

	public static final int _9F37_UNPREDICTABLE_NUMBER = 0x9F37;

	public static final int _9F38_PDOL = 0x9F38;

	public static final int _9F39_POS_ENTRY_MODE = 0x9F39;

	public static final int _9F40_ADD_TERMINAL_CAPABILITIES = 0x9F40;

	public static final int _9F41_TRANSACTION_SEQUENCE_COUNTER = 0x9F41;

	public static final int _9F42_APPLICATION_CURRENCY_CODE = 0x9F42;

	public static final int _9F44_APPLICATION_CURRENCY_EXPONENT = 0x9F44;

	public static final int _9F45_DATA_AUTHENTICATION_CODE = 0x9F45;

	public static final int _9F46_ICC_PUBLIC_KEY_CERTIFICATE = 0x9F46;

	public static final int _9F47_ICC_PUBLIC_KEY_EXPONENT = 0x9F47;

	public static final int _9F48_ICC_PUBLIC_KEY_REMAINDER = 0x9F48;

	public static final int _9F53_TRANSACTION_CATEGORY_CODE = 0x9F53;

	public static final int _A5_FCI_PROPRIETARY_TEMPLATE = 0xA5;
}
