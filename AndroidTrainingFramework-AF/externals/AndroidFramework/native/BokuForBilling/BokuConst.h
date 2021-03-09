#ifndef BOKUCONST
#define BOKUCONST

#define BOKU_API_KEY					"NvnTfQ4ZhF2ON6e3dOwbEbLdMZRsOjiQLqTWuza6PfWWITTkAgnZUm9cnfoJICg7513VZZGiQBPzYx5bB0azj6OLTUP63a96cfE0"		
#define GAMELOFT						"gameloft"
#define CLASS_NAME_BOKU_ACTIVITY		".BokuActivity"
#define BOKU							"BOKU"
#define NAME							"name"
#define OPERATION						"O"
#define RESULT							"R"
#define INDEX							"I"
#define STATUS							"S"
#define ITEM							"ITEM"
#define LIST							"LIST"
#define CHAR_ID							"CHAR_ID"
#define NOTIFY_ID						"NTFY_ID"
#define CHAR_REGION						"REGION_ID"
#define IMAGE							"image"
#define HTTP							"http"
#define PRICE							"price"
#define GOOGLE							"google"
#define BOKU_PROFILE_URL				"http://testshop.gameloft.com/smsunlock_profiles/profiles_test.php"

//Operation values
#define OP_GET_STRING			0
#define OP_START_BUY_ITEM		1
#define OP_FINISH_BUY_ITEM		2
#define OP_START_GET_LIST		3
#define OP_FINISH_GET_LIST		4
#define OP_START_GET_FQC_NAME	5
#define OP_GET_GGI				6
#define OP_GET_GGLIVE_UID		7

//Error response values of a transaction
#define	BUY_OK		0
#define BUY_CANCEL	1
#define BUY_FAIL	2
#define BUY_PENDING	3

#endif // !BOKUCONST
