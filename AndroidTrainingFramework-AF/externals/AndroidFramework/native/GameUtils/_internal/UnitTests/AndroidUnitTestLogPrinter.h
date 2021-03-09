#include "../../package_utils.h"


#if ACP_UT


#include "gtest/gtest.h"

#include <android/log.h>

#include <iostream>
#include <fstream>
#include <string>

#define LOG_UT(...) __android_log_print(ANDROID_LOG_DEBUG, "UnitTest", __VA_ARGS__)

void ClearFile()
{
	std::string path = std::string(acp_utils::api::PackageUtils::GetSdCardPath().c_str()) + std::string("/UT/UT_result.txt");
	
    std::ofstream file(path.c_str(), std::ofstream::out | std::ofstream::trunc);

	file.close();

}

void WriteToFile(const std::string& text)
{
	std::string path = std::string(acp_utils::api::PackageUtils::GetSdCardPath().c_str()) + std::string("/UT/UT_result.txt");
	
    std::ofstream file(path.c_str(), std::ios::app);

	if(!file.fail())
	{
		file << text;
		file << "\r\n";
	}

	file.close();
}


// As we are the native side of an Android app, we don't have any 'console', so
// gtest's standard output goes nowhere.
// Instead, we inject an "EventListener" in gtest and then we print the results
// using LOG, which goes to adb logcat.
class AndroidUnitTestLogPrinter : public ::testing::EmptyTestEventListener 
{

public:
	void Init();

	// EmptyTestEventListener
	virtual void OnTestProgramStart(
	  const ::testing::UnitTest& unit_test) ;
	virtual void OnTestStart(const ::testing::TestInfo& test_info) ;
	virtual void OnTestPartResult(const ::testing::TestPartResult& test_part_result) ;
	virtual void OnTestEnd(const ::testing::TestInfo& test_info) ;
	virtual void OnTestProgramEnd(const ::testing::UnitTest& unit_test) ;
};

void AndroidUnitTestLogPrinter::Init() 
{
	
	::testing::TestEventListeners& listeners =
	  ::testing::UnitTest::GetInstance()->listeners();
	// Adds a listener to the end.  Google Test takes the ownership.
	listeners.Append(this);


	ClearFile();
}

void AndroidUnitTestLogPrinter::OnTestProgramStart(const ::testing::UnitTest& unit_test) 
{
	char buff[1023];

	sprintf(buff, "[ START      ] %d", unit_test.test_to_run_count());

	std::string text_to_print = buff;


	LOG_UT("%s", text_to_print.c_str());

	WriteToFile(text_to_print);
  
}

void AndroidUnitTestLogPrinter::OnTestStart(const ::testing::TestInfo& test_info) 
{
	char buff[1023];
	sprintf(buff, "[ RUN      ] %s.%s",
						 test_info.test_case_name(), test_info.name());
	std::string text_to_print = buff;

	LOG_UT("%s", text_to_print.c_str());

	WriteToFile(text_to_print);
	
}

void AndroidUnitTestLogPrinter::OnTestPartResult(const ::testing::TestPartResult& test_part_result) 
{
	char buff[1023];

	if(test_part_result.failed())
	{
		sprintf(buff, "*** Failure in %s:%d\n%s\n", 
							test_part_result.file_name(), test_part_result.line_number(), test_part_result.summary());
	}
	else
	{
		sprintf(buff, "%s", "Success");
	}

	std::string text_to_print = buff;


	LOG_UT("%s", text_to_print.c_str());

	WriteToFile(text_to_print);
}

void AndroidUnitTestLogPrinter::OnTestEnd(const ::testing::TestInfo& test_info) 
{
	char buff[1023];
	
	if(test_info.result()->Failed())
	{
		sprintf(buff, "[  FAILED  ] %s.%s Time: %d", test_info.test_case_name(), test_info.name());
	}
	else
	{
		sprintf(buff, "[		OK  ] %s.%s", test_info.test_case_name(), test_info.name());
	}
		
		

	std::string text_to_print = buff;
	  
	LOG_UT("%s", text_to_print.c_str());

	WriteToFile(text_to_print);
}

void AndroidUnitTestLogPrinter::OnTestProgramEnd(const ::testing::UnitTest& unit_test) 
{
	char buff[1023];

	int milliseconds = unit_test.elapsed_time();
	int seconds = (int) (milliseconds / 1000) % 60 ;
	milliseconds = milliseconds - seconds * 1000;
	//int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

	sprintf(buff, "[ END      ] %d Time: %d.%d", unit_test.successful_test_count(), seconds, milliseconds);

	std::string text_to_print = buff;
	LOG_UT("%s", text_to_print.c_str());

	WriteToFile(text_to_print);
}

#endif //ACP_UT
