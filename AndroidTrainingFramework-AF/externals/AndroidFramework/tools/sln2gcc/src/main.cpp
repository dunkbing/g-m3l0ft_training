
#include "main.h"
#include <iostream>
#include <fstream>
#include <signal.h>

#include <wx/textfile.h>
#include <wx/textctrl.h>

#include <wx/arrstr.h>
#include <wx/filename.h>
#include <wx/log.h>
#include <wx/regex.h>
#include <wx/tokenzr.h>
#include <wx/sysopt.h>
#include <wx/dir.h>


#include <tchar.h>
#include <psapi.h>

#include "CompileThread.h"

#include "VcSln.h"
#include "VcProject.h"

#include "MemMgr.h"

#include <TlHelp32.h>


#include <list>
std::list<wxString> allErrors;


void Handler(const wxString& file, int line, const wxString& func, const wxString& cond, const wxString& msg)
{
}

void NormalizePath(wxString& file)
{
	TCHAR path[2048] = {};
	file = file.Trim(true);
	file = file.Trim(false);
	GetFullPathName(file.c_str(), _countof(path), path, NULL);
	file = path;
}



#if USE_ELEPHANT_MEMORY_MANAGER

IMPLEMENT_APP_NO_MAIN(sln2gccApp)
int main(int argc, char **argv) 
{
	// Basic Memory manager initialization
	cMemoryManager::InitializeCallbacks(MemoryManagerTTYPrint, MemoryManagerErrorHandle, NULL);

	// Initialize live view
	cMemoryManager::InitializeLiveView();

	// Initialize Elephant with 96MB (or nearest if not possible) with a 32MB heap.
	cMemoryManager::Get().Initialize(96 * 1024 * 1024, 32 * 1024 * 1024);

	cHeap::sHeapDetails details;
	details.bAllowDestructionWithAllocations = true;
	details.bAllowZeroSizeAllocations = true;



	int returnCode = wxEntry(argc, argv); 



	// Write out all the details before we end
	cMemoryManager::Get().ReportAll();

	// Note you do not need to call this.  The destructor will call this also but if you want to decide when all the 
	// memory should be freed this will perform that task.
	cMemoryManager::Get().Destroy();

	return returnCode;
}
#else
///this replace int main()
IMPLEMENT_APP_CONSOLE(sln2gccApp)   ///used for console app
//IMPLEMENT_APP(sln2androidApp)         ///used for gui app
#endif

wxMutex* sln2gccApp::s_mutexMain = new wxMutex(wxMUTEX_RECURSIVE);

bool sln2gccApp::s_bAbortProgram = false;

wxTimeSpan sln2gccApp::s_elapsedTime;
wxDateTime sln2gccApp::s_startTime;

int         sln2gccApp::s_argc;
wxChar**    sln2gccApp::s_argv;
int         sln2gccApp::s_errorcode = 0;
wxString sln2gccApp::s_inputFile = wxEmptyString;

wxString sln2gccApp::s_outputPath = wxEmptyString;
wxString sln2gccApp::s_precompiledPath = wxEmptyString;

wxString sln2gccApp::s_typeOfBuild = STR_DEBUG; //can be debug, release
wxString sln2gccApp::s_gccConfig = wxT("");
long sln2gccApp::s_jobs = 1;

wxArrayString sln2gccApp::s_distccHosts;
long sln2gccApp::s_distccJobs = 0;
bool sln2gccApp::s_verbose = false;
long sln2gccApp::s_errorsNo = 0;
wxString sln2gccApp::s_projectToBuild = wxT("all");
bool sln2gccApp::s_gcc2msvc = false;
bool sln2gccApp::s_distccEnable = false;
bool sln2gccApp::s_distccUseFullPath = false;

//bool sln2gccApp::s_clean = false;
//long sln2gccApp::s_errorsNoMax = 10;


wxString sln2gccApp::s_toolChain = wxEmptyString;
wxString sln2gccApp::s_toolChainBackup = wxEmptyString;

wxFileName* sln2gccApp::s_executableFilePath = NULL;
wxCmdLineParser* sln2gccApp::m_parser = NULL;
MapStringToInt sln2gccApp::s_fileChangedMap;

Declarations sln2gccApp::s_declarations;

using namespace std;


#ifdef __WXMSW__
BOOL WINAPI ConsoleHandler(DWORD dwCtrlType);  //  control signal type
BOOL WINAPI ConsoleHandler(DWORD CEvent);
#endif

// needed to convert paths
const wxString sln2gccApp::msc_backslash = wxT("\\");
const wxString sln2gccApp::msc_slash = wxT("/");
const wxString sln2gccApp::msc_doubleSlash = wxT("//");

// already compiled libs directory
//const wxString sln2gccApp::msc_debugLibsDir   = wxT("debugLibs");
//const wxString sln2gccApp::msc_releaseLibsDir = wxT("releaseLibs");


sln2gccApp::~sln2gccApp()
{
	delete s_executableFilePath;
	s_executableFilePath = NULL;
	delete m_parser;
	m_parser = NULL;
}


int sln2gccApp::OnRun()
{
	sln2gccApp::ChangeColour(COLOR_WHITE);
	wxPrintf(wxString(wxT("sln2gcc (c) Gameloft 2013 Bucharest, v")) + VERSION + wxT(" created by cristian.vasile@gameloft.com .\n"));

	signal(SIGABRT, AbortProgram);//If program aborts go to assigned function "myFunction".
	signal(SIGTERM, AbortProgram);//If program terminates go to assigned function "myFunction".

#ifdef __WXMSW__
	if (SetConsoleCtrlHandler((PHANDLER_ROUTINE)ConsoleHandler, TRUE) == FALSE)
	{
		// unable to install handler... 
		// display message to the user
		sln2gccApp::ChangeColour(COLOR_YELLOW);
		wxPrintf(wxT("Unable to install abort handler (this is a harmless warning)\n"));
		sln2gccApp::ChangeColour(COLOR_WHITE);
	}
#endif

	//if(KillProcessByName((const char*)(s_executableFilePath->GetFullName().ToAscii())))///kill the other processes -> only one process will work at a time!
	//{
	//    sln2gccApp::ChangeColour(COLOR_YELLOW);
	//    wxPrintf(_T("Some ") + s_executableFilePath->GetFullName() + _T(" previous started was killed!\n"));   
	//}

	sln2gccApp::ChangeColour(COLOR_WHITE);


	switch (m_parser->Parse(false))
	{
	case -1:
		wxPrintf(m_parser->GetUsageString());
		wxPrintf(_T("Help was given, terminating.\n"));

		break;

	case 0:///no error
	{

		m_parser->Found(_T("i"), &s_inputFile);
		m_parser->Found(_T("t"), &s_typeOfBuild);
		m_parser->Found(_T("p"), &s_projectToBuild);
		m_parser->Found(_T("g"), &s_gccConfig);
		s_gccConfig.Trim().Trim(false);

		m_parser->Found(_T("j"), &s_jobs);
		if (s_jobs <= 0) s_jobs = 1;

		s_verbose = m_parser->Found(_T("v"));



		if (wxFileName(s_inputFile).IsRelative(wxPATH_NATIVE))
		{
			s_inputFile = wxFileName::GetCwd() + SEP + s_inputFile;
		}

		NormalizePath(s_inputFile);

		s_outputPath = wxFileName(s_inputFile).GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + ".." + SEP + ".." + SEP + "build" + SEP + "generated";
		s_precompiledPath = wxFileName(s_inputFile).GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + ".." + SEP + ".." + SEP + "build" + SEP + "precompiled";

		NormalizePath(s_outputPath);
		NormalizePath(s_precompiledPath);

		wxPrintf(wxT("\n"));
		wxPrintf(wxT("Input sln2gcc.xml: ") + s_inputFile + wxT("\n"));
		wxPrintf(wxT("Intermediate path: ") + s_outputPath + wxT("\n"));
		wxPrintf(wxT("Precompiled path:  ") + s_precompiledPath + wxT("\n"));
		wxPrintf(wxT("\n"));
		wxPrintf(wxT("Type of Build: ") + s_typeOfBuild + wxT("\n"));
		wxPrintf(wxT("Gcc config:    ") + s_gccConfig + wxT("\n"));
		wxPrintf(wxT("\n"));
		wxPrintf(wxT("No. jobs: %d\n"), s_jobs);
		wxPrintf(wxT("Verbose:  %d\n"), s_verbose);
		wxPrintf(wxT("\n"));
		
		wxFileName::SetCwd(wxFileName(s_inputFile).GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR));


		if (!wxFile::Exists(s_inputFile))
		{
			ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("input file does not exist: ") + s_inputFile + wxT("\n"));
			sln2gccApp::SetErrorCode(1);
			break;
		}

		wxXmlDocument*  gccConfigsDoc = NULL;
		wxXmlDocument*  makeXmlDoc = NEW wxXmlDocument();

		if (!makeXmlDoc->Load(s_inputFile))
		{
			ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("input file cannot be opened: ") + s_inputFile + wxT("\n"));
			sln2gccApp::SetErrorCode(1);
			break;
		}

		//Declarations s_declarations;
		//CompilerSettings androidCompilerSettings;
		VcSln *sln = NULL;

		wxXmlNode* root = makeXmlDoc->GetRoot();

		if ((root) && (root->GetName() == wxT("Makefile")))
		{
			///---Solution-------------------------------------------------------------
			wxXmlNode* solutionNode = root->GetChildren(wxT("Solution"));
			if (solutionNode)
			{
				wxString solutionPath = solutionNode->GetPropVal(wxT("Path"), wxEmptyString);

				NormalizePath(solutionPath);

				if (solutionPath != wxEmptyString)
				{
					if (wxFileName(solutionPath).IsRelative(wxPATH_NATIVE))
					{
						solutionPath = wxFileName::GetCwd() + wxFileName::GetPathSeparator() + solutionPath;
					}

					if (!wxFile::Exists(solutionPath))
					{
						ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("solution file does not exist: ") + solutionPath + wxT("\n"));
						sln2gccApp::SetErrorCode(1);
						break;
					}

					sln = NEW VcSln(solutionPath);

					if (!sln->Parse())
					{
						ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("solution cannot be parsed: ") + solutionPath + wxT("\n"));
						sln2gccApp::SetErrorCode(1);
						break;
					}

					s_declarations[wxT("SolutionDir")] = wxFileName(solutionPath).GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR);

				}
				else
				{
					ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("<Solution Path=\"\"> is empty.\n"));
					sln2gccApp::SetErrorCode(1);
					break;
				}
			}
			else
			{
				ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("cannot find <Solution> node.\n"));
				sln2gccApp::SetErrorCode(1);
				break;
			}
			///---Solution-------------------------------------------------------------


			///---MainProject-------------------------------------------------------------
			wxXmlNode* mainProjectNode = root->GetChildren(wxT("MainProject"));
			if (mainProjectNode)
			{
				wxString mainProjectName = mainProjectNode->GetPropVal(wxT("Name"), wxEmptyString);

				if (mainProjectName != wxEmptyString)
				{
					s_declarations[wxT("MAIN_PROJECT")] = mainProjectName;
				}
			}
			///---MainProject-------------------------------------------------------------


			///---CommonGccConfig--------------------------------------------------
			wxXmlNode* gccNode = NULL;

			wxXmlNode* IncludeGccNode = root->GetChildren(wxT("IncludeCommonGccConfig"));
			if (IncludeGccNode)
			{
				wxString Path = IncludeGccNode->GetPropVal(wxT("Path"), wxT("."));

				if (wxFileName(Path).IsRelative(wxPATH_NATIVE))
				{
					// First try to find the file starting from the executable path
					Path = EXE_PATH + Path;
					if (!wxFile::Exists(Path))
					{
						// Try to search the relative path starting from the CWD
						Path = wxFileName::GetCwd() + SEP + Path;
					}
				}

				if (!wxFile::Exists(Path))
				{
					ERROR_PRINTF(Path, wxT("File does not exists: ") + Path + wxT("\n"));
					sln2gccApp::SetErrorCode(1);
					break;
				}

				gccConfigsDoc = NEW wxXmlDocument();

				if (!gccConfigsDoc->Load(Path))
				{
					ERROR_PRINTF(Path, wxT("file cannot be opened: ") + Path + wxT("\n"));
					sln2gccApp::SetErrorCode(1);
					break;
				}

				if (!gccConfigsDoc->GetRoot())
				{
					ERROR_PRINTF(Path, wxT("XML root node cannot be found in: ") + Path + wxT("\n"));
					sln2gccApp::SetErrorCode(1);
					break;
				}
				gccNode = gccConfigsDoc->GetRoot()->GetChildren(wxT("CommonGccConfig"));
			}
			else
			{
				gccNode = root->GetChildren(wxT("CommonGccConfig"));
			}


			if (s_gccConfig != wxEmptyString)
			{
				bool foundGccConfig = false;
				wxArrayString availableGccConfigs;
				if (gccNode)
				{
					wxXmlNode* child = gccNode->GetChildren();
					while (child)
					{
						if (child->GetName() == wxT("GccConfig"))
						{
							wxString Name = child->GetPropVal(wxT("Name"), wxT("not_set"));
							availableGccConfigs.Add(Name);
							//wxPrintf(Name);
							if (Name == s_gccConfig)
							{
								foundGccConfig = true;
								gccNode = child;
								break;
							}

						}

						child = child->GetNext();
					}
				}

				if (!foundGccConfig)
				{
					ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("cannot find ") + s_gccConfig + wxT(" !\n"));
					PRINTF(COLOR_RED, wxT("To fix this error you have the following solutions:\n"));
					//PRINTF(COLOR_RED, wxT("\t1. Remove the parameter -g (or --gccconfig) from command line.\n"));
					PRINTF(COLOR_RED, wxT("\t1. Add in config file node <CommonGccConfig><GccConfig Name=\"") + s_gccConfig + wxT("\"> gcc configuration </GccConfig></CommonGccConfig>\n"));
					if (availableGccConfigs.Count())
					{
						PRINTF(COLOR_RED, wxT("\t2. OR insert in to the command line one of the following available configs:\n"));
						for (unsigned int aaa = 0; aaa < availableGccConfigs.Count(); aaa++)
							PRINTF(COLOR_RED, wxT("\t\t\t -g ") + availableGccConfigs[aaa] + wxT("\n"));
					}
					//PRINTF(COLOR_RED, wxString(wxT("ATTENTION!\n\tFrom version 1.5.0 this warning will become ERROR!\n"))
					//                   + wxT("\tNow i will use all the configuration from <CommonGccConfig>.\n"));

					sln2gccApp::SetErrorCode(1);
					break;
				}
			}


			if (gccNode)
			{
				wxXmlNode* child = gccNode->GetChildren();
				while (child)
				{
					if (child->GetName() == wxT("Macro"))
					{
						// process text enclosed by <tag1></tag1>
						wxString content = child->GetNodeContent();

						// process properties of <tag1>
						wxString Name = child->GetPropVal(wxT("Name"), wxT("not_set"));

						wxString Value = child->GetPropVal(wxT("Value"), wxT("not_set"));

						if (Value == wxEmptyString || Value == wxT("not_set"))
						{
							Value = wxEmptyString;

							wxString CommonValue = child->GetPropVal(wxT("CommonValue"), wxT("not_set"));
							wxString DebugValue = child->GetPropVal(wxT("DebugValue"), wxT("not_set"));
							wxString ReleaseValue = child->GetPropVal(wxT("ReleaseValue"), wxT("not_set"));

							if (CommonValue != wxEmptyString && CommonValue != wxT("not_set")) Value = CommonValue;

							if (sln2gccApp::s_typeOfBuild == STR_RELEASE)
							{
								if ((ReleaseValue != wxEmptyString) && (ReleaseValue != wxT("not_set"))) Value += wxT(";") + ReleaseValue;
							}
							else
							{
								if ((DebugValue != wxEmptyString) && (DebugValue != wxT("not_set"))) Value += wxT(";") + DebugValue;
							}
						}

						if (sln2gccApp::s_verbose)
							PRINTF(COLOR_BLUE, wxT("<Macro Name=\"") + Name + wxT("\" Value=\"") + Value + wxT("\"/>\n"));

						REPLACE_SEPARATORS_WITH_SPACE(Value);

						if ((Name != wxT("MAIN_PROJECT")) ||
							((Name == wxT("MAIN_PROJECT")) && (s_declarations[Name] == wxEmptyString)))
							s_declarations[Name] = Value;
					}

					child = child->GetNext();
				}


				CHECK_MACRO(wxT("MAIN_PROJECT"), s_declarations, wxT("_Android"));
				CHECK_MACRO(wxT("GENERATE_BREAKPAD_SYM"), s_declarations, wxT("0"));
				CHECK_MACRO(wxT("GENERATE_DSYM"), s_declarations, wxT("1"));
				CHECK_MACRO(wxT("STRIP_DEBUG_SYMBOLS_FOR_RELEASE"), s_declarations, wxT("1"));
				CHECK_MACRO(wxT("STRIP_DEBUG_SYMBOLS_FOR_DEBUG"), s_declarations, wxT("0"));


				//CHECK_MACRO(wxT("TOOLCHAIN_VERSION"),             s_declarations, wxT("4.6"));
				//CHECK_MACRO(wxT("TOOLCHAIN_FOLDERNAME"),          s_declarations, wxT("arm-linux-androideabi-4.6"));
				CHECK_MACRO(wxT("TOOLCHAIN"), s_declarations, wxT("not_set"));
				CHECK_MACRO(wxT("TOOLCHAIN_BACKUP"), s_declarations, wxT("not_set"));


				CHECK_MACRO(wxT("DISTCC_ENABLE"), s_declarations, wxT("0"));
				CHECK_MACRO(wxT("DISTCC_HOSTS"), s_declarations, wxT("localhost"));
				CHECK_MACRO(wxT("DITSCC_JOBS"), s_declarations, wxT("4"));
				CHECK_MACRO(wxT("DISTCC"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("DISTCC_USE_FULL_PATH"), s_declarations, wxT("0"));


				//CHECK_MACRO(wxT("CCACHE_ENABLE"),                 s_declarations, wxT("0"));	
				//CHECK_MACRO(wxT("CCACHE_VERBOSE"),                s_declarations, wxT("0"));	
				//CHECK_MACRO(wxT("CCACHE"),                        s_declarations, wxT(""));

				CHECK_MACRO(wxT("CONVERT_ERRORS_TO_MSVC"), s_declarations, wxT("1"));

				CHECK_MACRO(wxT("CPP"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("CC"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("LD"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("LS"), s_declarations, wxT(""));

				CHECK_MACRO(wxT("COMPILE_CPP_COMMAND_LINE"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("COMPILE_CC_COMMAND_LINE"), s_declarations, wxT(""));

				CHECK_MACRO(wxT("DYNAMIC_LINK_COMMAND_LINE"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("STATIC_LINK_COMMAND_LINE"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("EXE_LINK_COMMAND_LINE"), s_declarations, wxT(""));

				CHECK_MACRO(wxT("BREAKPAD_SYM_COMMAND_LINE"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("DSYM_COMMAND_LINE"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("STRIP_COMMAND_LINE"), s_declarations, wxT(""));

				CHECK_MACRO(wxT("TYPES_OF_FILES_TO_BE_COMPILED"), s_declarations, wxT("cpp;c"));

				CHECK_MACRO(wxT("DEFINES"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("CFLAGS"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("INCLUDE_PATHS"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("LDLIBS"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("LDFLAGS"), s_declarations, wxT(""));
				CHECK_MACRO(wxT("LINK_PATHS"), s_declarations, wxT(""));



				///add the flag that will generate the dependency files
				s_declarations[wxT("CFLAGS")] = s_declarations[wxT("CFLAGS")] + wxT(" -MD ");

				wxString errorMsg = wxEmptyString;


				//DISTCC -----------------------------------------
				s_declarations[wxT("DISTCC_ENABLE")] = sln2gccApp::ExpandExpression(s_declarations[wxT("DISTCC_ENABLE")], s_declarations, errorMsg);
				s_distccEnable = (CHECK_MACRO_IF_IS_ENABLED(wxT("DISTCC_ENABLE"), s_declarations));

				s_declarations[wxT("DITSCC_JOBS")] = sln2gccApp::ExpandExpression(s_declarations[wxT("DITSCC_JOBS")], s_declarations, errorMsg);
				if (s_declarations[wxT("DITSCC_JOBS")] == wxEmptyString) s_distccJobs = 0;
				else
				{
					long getValue = 0;
					s_declarations[wxT("DITSCC_JOBS")].ToLong(&getValue);
					s_distccJobs = getValue;
				}

				if ((s_distccJobs > 0) && s_distccEnable)
				{
					if (s_declarations[wxT("DISTCC")] == wxEmptyString)
					{
						WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("The path to distcc.exe it is not set, so, distributed compiling will not work. You will use your machine only.\n"));
						s_distccEnable = false;
					}
					else
					{
						s_declarations[wxT("DISTCC")] = sln2gccApp::ExpandExpression(s_declarations[wxT("DISTCC")], s_declarations, errorMsg);

						if (!wxFileName::FileExists(s_declarations[wxT("DISTCC")]))
						{
							WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("DISTCC it is set to an unknown file: ") + s_declarations[wxT("DISTCC")] + wxT("\n"));
							s_distccEnable = false;
						}
						//else
						//{
						//    wxFileName distccFName(s_declarations[wxT("DISTCC")]);
						//    KillProcessByName((const char*)distccFName.GetFullName().ToAscii());
						//}
					}

					if (s_distccEnable)
					{
						s_distccUseFullPath = (CHECK_MACRO_IF_IS_ENABLED(wxT("DISTCC_USE_FULL_PATH"), s_declarations));

						s_declarations[wxT("DISTCC_HOSTS")] = sln2gccApp::ExpandExpression(s_declarations[wxT("DISTCC_HOSTS")], s_declarations, errorMsg);
						wxSetEnv(wxT("DISTCC_HOSTS"), s_declarations[wxT("DISTCC_HOSTS")]);

						//////////////////////////////////////////////////////////////////////////
						PRINTF(COLOR_BLUE_LIGHT, wxString(wxT("DISTCC Jobs=%d\n")), s_distccJobs);
						PRINTF(COLOR_BLUE_LIGHT, wxString(wxT("DISTCC Hosts=")));
						wxString newValue = wxEmptyString;
						wxString Value = s_declarations[wxT("DISTCC_HOSTS")];

						wxStringTokenizer* tkn = NEW wxStringTokenizer(Value, wxT(" "));

						while (tkn->HasMoreTokens())
						{
							wxString token = tkn->GetNextToken();
							token = token.Trim().Trim(false);

							s_distccHosts.Add(token);
							PRINTF(COLOR_BLUE_LIGHT, wxString(wxT(" ")) + token + wxT(","));
						}

						PRINTF(COLOR_BLUE_LIGHT, wxString(wxT("\n")));
						//////////////////////////////////////////////////////////////////////////

					}

				}
				//DISTCC -----------------------------------------



				//TOOLCHAIN -----------------------------------------
				s_toolChainBackup = s_declarations[wxT("TOOLCHAIN_BACKUP")];
				s_toolChainBackup = sln2gccApp::ExpandExpression(s_toolChainBackup, s_declarations, errorMsg);

				if (wxDir::Exists(s_toolChainBackup))
				{
					///insert the toolchain in the system env PATH
					if (s_toolChainBackup != wxEmptyString)
					{
						wxString envPATH = wxEmptyString;
						///try to take this value from ENVIRONMENT VARIABLES!
						if (wxGetEnv(wxT("PATH"), &envPATH))
						{
							envPATH = s_toolChainBackup + wxT(";") + envPATH;


							if (sln2gccApp::s_verbose) PRINTF(COLOR_YELLOW, wxT("envPATH=") + envPATH + wxT("\n"));


							//insert the NEW path in environment variable
							if (!wxSetEnv(wxT("PATH"), envPATH))
							{
								WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("cannot insert the TOOLCHAIN_BACKUP in the env PATH!\n"));
							}
						}
					}
				}
				else
				{
					s_toolChainBackup = wxEmptyString;
					WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("TOOLCHAIN_BACKUP does not have a valid path!\n"));
				}

				s_toolChain = s_declarations[wxT("TOOLCHAIN")];
				s_toolChain = sln2gccApp::ExpandExpression(s_toolChain, s_declarations, errorMsg);

				if (!wxDir::Exists(s_toolChain))
				{
					if (s_toolChainBackup == wxEmptyString)
					{
						ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("the path for TOOLCHAIN=") + s_toolChain + wxT(" does not exist!\n"));
						ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("I can't use TOOLCHAIN_BACKUP because does not exist!\n"));
						sln2gccApp::SetErrorCode(1);
						break;
					}
					else
					{
						WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("The path for TOOLCHAIN=") + s_toolChain + wxT(" does not exist!\n"));
						WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("I will use the TOOLCHAIN_BACKUP instead!\n"));
						s_declarations[wxT("TOOLCHAIN")] = s_toolChainBackup;
						s_toolChain = s_toolChainBackup;
					}
				}
				else
				{
					///insert the toolchain in the system env PATH
					if (s_toolChain != wxEmptyString)
					{
						wxString envPATH = wxEmptyString;
						///try to take this value from ENVIRONMENT VARIABLES!
						if (wxGetEnv(wxT("PATH"), &envPATH))
						{
							envPATH = s_toolChain + wxT(";") + envPATH;

							if (sln2gccApp::s_verbose) PRINTF(COLOR_YELLOW, wxT("envPATH=") + envPATH + wxT("\n"));

							//insert the NEW path in environment variable
							if (!wxSetEnv(wxT("PATH"), envPATH))
							{
								WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("cannot insert the TOOLCHAIN in the env PATH!\n"));
							}
						}
					}
				}
				//TOOLCHAIN -----------------------------------------






				//CONVERT_ERRORS_TO_MSVC -----------------------------------------
				s_gcc2msvc = (CHECK_MACRO_IF_IS_ENABLED(wxT("CONVERT_ERRORS_TO_MSVC"), s_declarations));
				//CONVERT_ERRORS_TO_MSVC -----------------------------------------



				if ((!ToolchainCheck(wxT("CPP"), s_declarations))
					|| (!ToolchainCheck(wxT("CC"), s_declarations))
					|| (!ToolchainCheck(wxT("LS"), s_declarations))
					|| (!ToolchainCheck(wxT("LD"), s_declarations)))
				{
					sln2gccApp::SetErrorCode(1);
					break;
				}

				//KillProcessByName("as.exe");
				//KillProcessByName("cc1.exe");
				//KillProcessByName("cc1plus.exe");
				//KillProcessByName("ld.exe");

			}
			else
			{
				ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("cannot find <CommonGccConfig> node.\n"));
				sln2gccApp::SetErrorCode(1);
				break;
			}
			///---CommonGccConfig-------------------------------------------------


			///---Project-------------------------------------------------------------
			wxXmlNode *projectNode = root->GetChildren();

			while (projectNode)
			{
				if (projectNode->GetName() == wxT("Project"))
				{
					wxString name = projectNode->GetPropVal(wxT("Name"), wxEmptyString);

					VcProject* prjTemp = sln->GetProject(name);
					if (prjTemp)
					{
						bool isTheMainProject = (s_declarations[wxT("MAIN_PROJECT")] == prjTemp->GetName());

						//wxPrintf(wxT("Setting Up project: '") + prjTemp->GetName() + wxT("' ") + (isTheMainProject?wxT(" -> main project!\n"):wxT("!\n")));


						prjTemp->m_targetOutType = projectNode->GetPropVal(wxT("TargetOutType"), wxEmptyString);
						if (prjTemp->m_targetOutType == wxEmptyString)
						{
							if (isTheMainProject)
								prjTemp->m_targetOutType = wxT("so");
							else
								prjTemp->m_targetOutType = wxT("a");
						}


						/**
						 * If the UseS2GFile is not specified, It will try to use the the s2g file from the folder with vcproj.
						 * If the UseS2GFile is specified, the path to that file will be relative to the s_inputFile (aka sln2gcc.xml) location
						 */
						bool loadSettingsFromS2GFile = false;
						prjTemp->m_s2gFile = projectNode->GetPropVal(wxT("UseS2GFile"), wxT("false"));

						bool defaultUseS2GFile = false;
						if (IS_TRUE(prjTemp->m_s2gFile))
						{
							wxFileName prjFN(prjTemp->GetFileName());

							// check s2g file
							wxString pathToCheck = prjFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + prjFN.GetName() + wxT(".s2g");
							wxFileName checkS2G(pathToCheck);
							if (checkS2G.FileExists() == false)
							{
								ERROR_PRINTF(pathToCheck, wxT("File not Found!\n"));
								sln2gccApp::SetErrorCode(1);
								projectNode = projectNode->GetNext();
								continue;
							}

							prjTemp->m_s2gFile = prjFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + prjFN.GetName() + wxT(".s2g");
							defaultUseS2GFile = true;
							loadSettingsFromS2GFile = true;
						}
						else
							if (IS_FALSE(prjTemp->m_s2gFile))
							{
								//this means - do not use s2g files and take                                 
								loadSettingsFromS2GFile = false;
							}
							else
							{
								loadSettingsFromS2GFile = true;

								wxFileName sln2gccFN(sln2gccApp::s_inputFile);

								// check s2g file
								wxString pathToCheck = sln2gccFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + prjTemp->m_s2gFile;
								wxFileName checkS2G(pathToCheck);
								if (checkS2G.FileExists() == false)
								{
									ERROR_PRINTF(pathToCheck, wxT("File not Found!\n"));
									sln2gccApp::SetErrorCode(1);
									projectNode = projectNode->GetNext();
									continue;
								}

								prjTemp->m_s2gFile = sln2gccFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + prjTemp->m_s2gFile;
							}

						if (loadSettingsFromS2GFile && wxFileExists(prjTemp->m_s2gFile))
						{
							wxPrintf(wxT("Loading settings for project '") + prjTemp->GetName() + wxT("' from '") + wxFileName(prjTemp->m_s2gFile).GetFullName() + wxT("' file") + (isTheMainProject ? wxT(" -> main project!\n") : wxT("!\n")));

							wxXmlDocument*  s2gProject = NEW wxXmlDocument();

							if (s2gProject->Load(prjTemp->m_s2gFile))
							{
								wxXmlNode* s2gProjectRoot = s2gProject->GetRoot();

								if ((s2gProjectRoot) && (s2gProjectRoot->GetName() == wxT("Project")))
								{
									LoadProjectSettings(s2gProjectRoot, prjTemp);
								}
								else
								{
									loadSettingsFromS2GFile = false;
									WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("the UseS2GFile file '") + prjTemp->m_s2gFile + wxT("' do not have <Project>!\n"));
								}

								delete s2gProjectRoot;
								s2gProject->DetachRoot();
							}
							else
							{
								loadSettingsFromS2GFile = false;
								WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("the UseS2GFile file '") + prjTemp->m_s2gFile + wxT("' cannot be opened!\n"));
							}


							delete s2gProject;
						}
						else
						{
							/**
							 * Show an warning message only if is not used the default value.
							 */
							if (!defaultUseS2GFile && loadSettingsFromS2GFile)
							{
								ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT(" the UseS2GFile file '") + prjTemp->m_s2gFile + wxT("' does not exist!\n"));
								sln2gccApp::SetErrorCode(1);
								projectNode = projectNode->GetNext();
								continue;
							}

							loadSettingsFromS2GFile = false;
						}

						if (!loadSettingsFromS2GFile)
						{
							wxPrintf(wxT("Loading settings for project '") + prjTemp->GetName() + wxT("' from '") + wxFileName(sln2gccApp::s_inputFile).GetFullName() + wxT("' file") + (isTheMainProject ? wxT(" -> main project!\n") : wxT("!\n")));
							LoadProjectSettings(projectNode, prjTemp);
						}

					}
					else
					{

						ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxString(wxT("cannot find '")) + name + wxT("' in '") + sln->GetFileName() + wxT("' \n"));
						PRINTF(COLOR_RED, wxString(wxT("\t Tips: In file '") + sln2gccApp::s_inputFile + wxT("' you need to specify the correct name for projects.\n")));
						PRINTF(COLOR_RED, wxString(wxT("\t Tips: The project name is case sensitive!\n")));
						wxString availableProjects = wxEmptyString;
						for (int p = 0; p < sln->GetProjectsCount(); p++)
						{
							availableProjects += sln->GetProject(p)->GetName();
							if (p < sln->GetProjectsCount() - 1) availableProjects += wxT(", ");
						}
						PRINTF(COLOR_RED, wxString(wxT("\t Tips: Available projects are: ")) + availableProjects + wxT("\n"));

						AbortProgram(1);
					}

				}

				projectNode = projectNode->GetNext();
			};
			///---Project-------------------------------------------------------------
		}
		else
		{
			ERROR_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("cannot find <Makefile> node.\n"));
			sln2gccApp::SetErrorCode(1);
			break;
		}


		if (sln == NULL)
		{
			ERROR_PRINTF(wxString(wxT("file")), wxT("sln is NULL due to unknown error.\n"));
			sln2gccApp::SetErrorCode(1);
			break;
		}

		///---COMPILE----------------------------------------------------------------


		///compile the projects
		bool rebuildMainProject = false;
		sln2gccApp::s_startTime = wxDateTime::UNow();

		int prjCount = sln->GetProjectsCount();


		bool someProjectsWasIgnored = false;
		for (int i = 0; i < prjCount; i++)
		{
			VcProject* prj = sln->GetProject(i);
			if (prj->IsIncludedInProject())
			{
				PRINTF(COLOR_GREEN, wxT("project '") + prj->GetName() + wxT("'\t\tready!\n"));
			}
			else
			{
				someProjectsWasIgnored = true;
				PRINTF(COLOR_YELLOW, wxString(wxT("project '") + prj->GetName() + wxT("'\t\tignored!\n")));
			}
		}
		if (someProjectsWasIgnored)
		{
			WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxString(wxT("Some projects were ignored, because they were not found in config file '%s'!\n")), sln2gccApp::s_inputFile);
		}

		//detect the mainProject
		VcProject* mainProject = NULL;
		for (int i = 0; i < prjCount; i++)
		{
			VcProject* prj = sln->GetProject(i);

			if (!prj) continue;
			if (!prj->IsIncludedInProject()) continue;


			if (s_declarations[wxT("MAIN_PROJECT")] == prj->GetName())
			{
				//last compiled project must be the mainProject
				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxString(wxT("MAIN_PROJECT='")) + prj->GetName() + wxT("' !\n"));
				mainProject = prj;
				break;
			}
		}

		if (mainProject == NULL)
		{
			WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxString(wxT("MAIN_PROJECT='")) + s_declarations[wxT("MAIN_PROJECT")] + wxT("' is not valid!\n"));
			if (prjCount > 0)
			{
				mainProject = sln->GetProject(0);
				s_declarations[wxT("MAIN_PROJECT")] = mainProject->GetName();
				WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxString(wxT("I will take the first project '")) + s_declarations[wxT("MAIN_PROJECT")] + wxT("' as a MAIN_PROJECT!\n"));
			}
		}



		VcProjectList::iterator it = sln->m_prj.begin();
		while (it != sln->m_prj.end())
		{

			VcProject* prj = (*it);//sln->GetProject(i);
			if ((!prj) || (mainProject == prj))
			{
				it++;
				continue;
			}


			if ((s_projectToBuild != wxT("all")) && (s_projectToBuild != prj->GetName()))
			{
				it = sln->RemoveProjectFromList(it);
				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("0Delete '") + prj->GetName() + wxT("' !\n"));
				delete prj;

				continue;
			}



			if (!prj->IsIncludedInProject())
			{
				it = sln->RemoveProjectFromList(it);
				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("1Delete '") + prj->GetName() + wxT("' !\n"));
				delete prj;

				continue;
			}



			if (!prj->Parse())
			{
				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("2Delete '") + prj->GetName() + wxT("' !\n"));
				delete prj;
				it = sln->RemoveProjectFromList(it);

				continue;
			}

			//if(s_projectToBuild == wxT("all") || (s_projectToBuild == prj->GetName()))
			//{
			if (!prj->PrepareCompileCommands(s_declarations, sln))
			{
				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("3Delete '") + prj->GetName() + wxT("' !\n"));
				delete prj;
				it = sln->RemoveProjectFromList(it);

				continue;
			}
			//}


			if (prj->HasTargetChanged()) rebuildMainProject = true;


			//the mainProject is the project that will link all the other projects, so it needs to know the paths
			//to all other libraryes!         
			if (mainProject)
			{
				wxString relPathFromMainProjectToPrj = sln2gccApp::GetRelativePath(mainProject->GetWrokingPath(), prj->GetWrokingPath());
				mainProject->m_projectDecl[wxT("LINK_PATHS")] = mainProject->m_projectDecl[wxT("LINK_PATHS")] + wxT(" ") + relPathFromMainProjectToPrj;
			}



			//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("Delete '") + prj->GetName() + wxT("' !\n"));
			delete prj;
			it = sln->RemoveProjectFromList(it);
		}
		///last compiled project must be the mainProject
		if (mainProject && (sln2gccApp::GetErrorCode() == 0)) //compile it only if NO errors or link errors was reported.
		{
			if ((s_projectToBuild == wxT("all")) || (s_projectToBuild == mainProject->GetName()))
			{
				mainProject->Parse();

				mainProject->PrepareCompileCommands(s_declarations, sln, rebuildMainProject, false, true);

			}
			sln->RemoveProjectFromList(mainProject);
			delete mainProject;
		}

		if (VcProject::Compile() == false)
		{
			PRINTF(COLOR_RED, wxT("\nCompile process failed!\n"));
			break;//AbortProgram(1);
		}

		if (s_errorsNo > 0)
		{
			PRINTF(COLOR_RED, wxT("Linking process aborted because of previous compilation errors (%d)!\n"), s_errorsNo);
			break;//AbortProgram(1);
		}

		if (sln2gccApp::s_bAbortProgram)
		{
			PRINTF(COLOR_RED, wxT("\nProcess aborted (by user)!\n"));
			if (sln2gccApp::GetErrorCode() == 0) sln2gccApp::s_errorcode = 1;
			break;
		}

		if (VcProject::Link() == false)
		{
			PRINTF(COLOR_RED, wxT("\nLink process failed!\n"));
			break;//AbortProgram(1);
		}


		VcProject::CopyLibs();

		s_elapsedTime = wxDateTime::UNow().Subtract(sln2gccApp::s_startTime);
		int elapsedMinutes = s_elapsedTime.GetMinutes();
		int elapsedSeconds = s_elapsedTime.GetSeconds().ToLong() % 60;
		PRINTF(COLOR_GREEN, wxString(wxT("\nCompile time %dm%ds\n")), elapsedMinutes, elapsedSeconds);

		//wxString test = GetRelativePath(wxT("c:\\test\\..\\haha\\"), wxT("c:\\test\\.."));
		//wxPrintf(test + wxT("\n"));


		if (sln != NULL)
		{
			delete sln;
		}
		makeXmlDoc->DetachRoot();
		delete root;
		delete makeXmlDoc;

		delete gccConfigsDoc;

		break;
	}

	default:
	{
		sln2gccApp::ChangeColour(COLOR_YELLOW);
		wxPrintf(_T("Command line errors detected, aborting.\n"));
		sln2gccApp::ChangeColour(COLOR_WHITE);

		wxPrintf(m_parser->GetUsageString());
		sln2gccApp::SetErrorCode(1);
		break;
	}
	}
	

	Clean();


	wxPrintf(_T("\n_______________________________________________________________________________\n\n"));

#ifdef _DEBUG    
	wxPrintf(_T("Press any key\n"));
	getchar();
#endif


	if (GetErrorCode())
	{
		//if (allErrors.size() == 0)
		{
			sln2gccApp::ChangeColour(COLOR_RED);
			wxPrintf("\nSome errors were reported by the build process...\n");
			sln2gccApp::ChangeColour(COLOR_WHITE);
		}

		//if (allErrors.size() > 0)
		//{
		//	sln2gccApp::ChangeColour(COLOR_WHITE);
		//	wxPrintf("\nAll errors reported by the build process:\n\n\n");
		//	sln2gccApp::ChangeColour(COLOR_RED);

		//	for (int i = 0; i < allErrors.size(); i++)
		//	{
		//		wxPrintf("%s", allErrors.front());
		//		allErrors.pop_front();
		//	}

		//	sln2gccApp::ChangeColour(COLOR_WHITE);
		//}
	}



#if USE_MEMORY_LEAKS_TRACKER
	MemMgr::InternalMemTracker::Instance()->PrintStatus();
	MemMgr::InternalMemTracker::Instance()->Destroy();
#endif
	return s_errorcode;
}

bool sln2gccApp::LoadProjectSettings(wxXmlNode* projectNode, VcProject* prjTemp)
{
	wxXmlNode* child = projectNode->GetChildren();
	while (child)
	{
		if (child->GetName() == wxT("Macro"))
		{
			// process text enclosed by <tag1></tag1>
			wxString content = child->GetNodeContent();

			// process properties of <tag1>
			wxString Name = child->GetPropVal(wxT("Name"), wxT("not_set"));

			wxString Value = child->GetPropVal(wxT("Value"), wxT("not_set"));


			// no value
			if (Value == wxEmptyString || Value == wxT("not_set"))
			{
				Value = wxEmptyString;

				wxString CommonValue = child->GetPropVal(wxT("CommonValue"), wxT("not_set"));
				wxString DebugValue = child->GetPropVal(wxT("DebugValue"), wxT("not_set"));
				wxString ReleaseValue = child->GetPropVal(wxT("ReleaseValue"), wxT("not_set"));

				if (CommonValue != wxEmptyString && CommonValue != wxT("not_set")) Value = CommonValue;

				if (sln2gccApp::s_typeOfBuild == STR_RELEASE)
				{
					if ((ReleaseValue != wxEmptyString) && (ReleaseValue != wxT("not_set"))) Value += wxT(";") + ReleaseValue;
				}
				else
				{
					if ((DebugValue != wxEmptyString) && (DebugValue != wxT("not_set"))) Value += wxT(";") + DebugValue;
				}
			}

			// make link paths relative to GLLegacy/_Android
			if (Name == wxT("LINK_PATHS"))
			{
				if ((Value != wxEmptyString) && (Value != wxT("not_set")))
				{
					REPLACE_SEPARATORS_WITH_SPACE(Value);

					wxString newValue = wxEmptyString;

					wxStringTokenizer* tkn = NEW wxStringTokenizer(Value, wxT(" "));

					while (tkn->HasMoreTokens())
					{
						wxString token = tkn->GetNextToken();

						newValue += wxString(wxT(" "));

						wxFileName fileToken(token);

						if (fileToken.IsRelative())
						{
							newValue += wxString(wxT("../../../"));
						}

						newValue += token;
					}

					Value = newValue;

					delete tkn;
				}
			}


			if (sln2gccApp::s_verbose)
				PRINTF(COLOR_BLUE, wxT("<Macro Name=\"") + Name + wxT("\" Value=\"") + Value + wxT("\"/>\n"));

			REPLACE_SEPARATORS_WITH_SPACE(Value);

			wxString errorMsg = wxEmptyString;
			Value = sln2gccApp::ExpandExpression(Value, prjTemp->m_projectDecl, errorMsg, false);
			Value = sln2gccApp::ExpandExpression(Value, s_declarations, errorMsg);

			prjTemp->m_projectDecl[Name] = Value;
		}
		else if (child->GetName() == wxT("Flag"))
		{

		}

		child = child->GetNext();
	}


	child = projectNode->GetChildren(wxT("UnityBuilds"));
	if (child)
	{
		//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("UnityBuilds.\n"));

		wxXmlNode* ubChilds = child->GetChildren();
		while (ubChilds)
		{
			if (ubChilds->GetName() == wxT("AutoGenerated"))
			{
				prjTemp->m_autoGeneratedUBNumber = 0;

				ubChilds->GetPropVal(wxT("UnityBuildsNumber"), wxT("-1")).ToLong(&prjTemp->m_autoGeneratedUBNumber);

				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT("AutoGenerated  UnityBuildsNumber %ld\n"), prjTemp->m_autoGeneratedUBNumber);

				if (prjTemp->m_autoGeneratedUBNumber < 0)
				{
					WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT(" <AutoGenerated> missing proprety! Use UnityBuildsNumber=how_many_generated_files !\n"));
					prjTemp->m_autoGeneratedUBNumber = 0;
				}



				wxXmlNode* fileNode = ubChilds->GetChildren();

				while (fileNode)
				{
					if (fileNode->GetName() == wxT("ExcludeFileFromUnityBuild"))
					{
						wxString fileName = fileNode->GetPropVal(wxT("Name"), wxEmptyString);
						if (fileName != wxEmptyString)
						{
							prjTemp->m_excludeFileFromAutoGenUB.Add(fileName);
							//wxPrintf(wxString(wxT("ignore file=")) + fileName + wxT("\n"));
						}
					}

					fileNode = fileNode->GetNext();
				};
			}

			if (ubChilds->GetName() == wxT("Custom"))
			{
				wxString customUBName = ubChilds->GetPropVal(wxT("Name"), wxEmptyString);

				//WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), customUBName + wxT(" <Custom> node not available yet! Use <AutoGenerated> instead.\n"));

				CustomUnityBuild* cub = NEW CustomUnityBuild();
				cub->m_name = wxT("CUB_") + customUBName;

				wxFileName fn(cub->m_name);
				if ((fn.GetExt() != wxT("cpp"))
					&& (fn.GetExt() != wxT("c")))
				{
					WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT(" <Custom> UnitiBuild (CUB) '") + customUBName + wxT("' does not have a valid(c or cpp) extension. Extension 'cpp' will be appended!\n"));
					cub->m_name += wxT(".cpp");
				}

				prjTemp->m_customUBFiles.Add(cub);

				wxXmlNode* fNode = ubChilds->GetChildren();

				while (fNode)
				{
					if (fNode->GetName() == wxT("Filter"))
					{
						wxString filterName = fNode->GetPropVal(wxT("Name"), wxEmptyString);

						if (filterName != wxEmptyString)
						{

							cub->m_rule[filterName] = fNode->GetPropVal(wxT("AddOnlyFilesThatMatch"), wxT("*.cpp"));
							wxString recursive = fNode->GetPropVal(wxT("recursive"), wxT("true"));

							if (recursive.Upper() == wxT("TRUE") || recursive.Upper() == wxT("YES") || recursive.Upper() == wxT("1"))
								cub->m_recursive[filterName] = 1;
							else
								cub->m_recursive[filterName] = 0;

							//wxPrintf(wxString(wxT("filterName='")) + filterName  + wxT("' recursive=%d cub->m_rule.size()=%d, m_customUBFiles.size()=%d\n"), cub->m_recursive[filterName], cub->m_rule.size(), prjTemp->m_customUBFiles.size());

						}
					}

					fNode = fNode->GetNext();
				};


			}

			ubChilds = ubChilds->GetNext();
		}
	}
	else
	{
		///-----------------------------------------------------------------------------------------
		///- this code will be removed.
		///- all the unity builds settings must be in <UnityBuild> node. See the aboove code
		child = projectNode->GetChildren(wxT("AutoGeneratedUnityBuild"));
		if (child)
		{
			WARNING_PRINTF(sln2gccApp::s_inputFile + wxT("(/s2g)"), wxT(" <AutoGeneratedUnityBuild> is deprecated! Use <UnityBuilds> <AutoGenerated UnityBuildsNumber=0 /> </UnityBuilds> instead\n"));

			prjTemp->m_autoGeneratedUBNumber = 0;
			child->GetPropVal(wxT("UnityBuildsNO"), wxT("0")).ToLong(&prjTemp->m_autoGeneratedUBNumber);
			//wxString ubNO     = child->GetPropVal(wxT("UnityBuildsNO"), wxT("0"));
			//wxString ub   = child->GetPropVal(wxT("Release"), wxT("Release"));
			wxXmlNode* fileNode = child->GetChildren();

			while (fileNode)
			{
				if (fileNode->GetName() == wxT("ExcludeFileFromUnityBuild"))
				{
					wxString fileName = fileNode->GetPropVal(wxT("Name"), wxEmptyString);
					if (fileName != wxEmptyString)
					{
						prjTemp->m_excludeFileFromAutoGenUB.Add(fileName);
						//wxPrintf(wxString(wxT("ignore file=")) + fileName + wxT("\n"));
					}
				}

				fileNode = fileNode->GetNext();
			};
		}
		///-----------------------------------------------------------------------------------------
	}


	child = projectNode->GetChildren(wxT("MSVCConfiguration"));
	if (child)
	{
		prjTemp->m_projectDecl[wxT("CONFIG_DEBUG")] = child->GetPropVal(wxT("Debug"), wxT("Debug"));
		prjTemp->m_projectDecl[wxT("CONFIG_RELEASE")] = child->GetPropVal(wxT("Release"), wxT("Release"));

	}

	prjTemp->SetIncludedInSolution(true);


	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("USE_ADDITIONAL_INCLUDE_DIRECTORIES_FROM_VS"), prjTemp->m_projectDecl, wxT("1"));

	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("USE_PROPERTY_GROUP_FROM_VS"), prjTemp->m_projectDecl, wxT("0"));

	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("USE_EXCLUDEFROMBUILD_VS_FLAG"), prjTemp->m_projectDecl, wxT("0"));

	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("USE_PCH_FILE"), prjTemp->m_projectDecl, wxT(""));

	if (prjTemp->m_projectDecl[wxT("USE_PCH_FILE")] != wxT(""))
		CHECK_MACRO_ON_PROJECT(prjTemp, wxT("USE_PCH_FILE_AS"), prjTemp->m_projectDecl, wxT("c++-header"));

	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("DEFINES"), prjTemp->m_projectDecl, wxT(""));
	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("CFLAGS"), prjTemp->m_projectDecl, wxT(""));
	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("INCLUDE_PATHS"), prjTemp->m_projectDecl, wxT(""));
	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("LDLIBS"), prjTemp->m_projectDecl, wxT(""));
	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("LDFLAGS"), prjTemp->m_projectDecl, wxT(""));
	CHECK_MACRO_ON_PROJECT(prjTemp, wxT("LINK_PATHS"), prjTemp->m_projectDecl, wxT(""));




	child = projectNode->GetChildren(wxT("AddSourceFileToProject"));
	if (child)
	{
		wxString addFileName = child->GetPropVal(wxT("FileName"), wxT(""));
		if ((addFileName != wxEmptyString))
		{
			wxString errorMsg = wxEmptyString;
			addFileName = sln2gccApp::ExpandExpression(addFileName, s_declarations, errorMsg);
			prjTemp->m_sourceFiles.Add(addFileName);
		}

		wxXmlNode* fileNode = child->GetChildren();
		while (fileNode)
		{
			if (fileNode->GetName() == wxT("File"))
			{
				addFileName = fileNode->GetPropVal(wxT("Name"), wxEmptyString);
				if (addFileName != wxEmptyString)
				{
					wxString errorMsg = wxEmptyString;
					addFileName = sln2gccApp::ExpandExpression(addFileName, s_declarations, errorMsg);
					prjTemp->m_sourceFiles.Add(addFileName);
				}
			}

			fileNode = fileNode->GetNext();
		};

	}


	child = projectNode->GetChildren(wxT("FileSpecific"));
	if (child)
	{
		wxString specificCFLAGS = child->GetPropVal(wxT("CFLAGS"), wxT(""));
		wxString specificFileName = child->GetPropVal(wxT("FileName"), wxT(""));
		if ((specificCFLAGS != wxEmptyString) && (specificFileName != wxEmptyString))
		{
			prjTemp->m_fileSpecificCFLAGS[specificFileName] = specificCFLAGS;
			//wxPrintf(wxString(wxT("FileSpecific file=")) + specificFileName + wxT(" specificCFLAGS=") + specificCFLAGS + wxT("\n"));
		}

		wxXmlNode* fileNode = child->GetChildren();
		while (fileNode)
		{
			if (fileNode->GetName() == wxT("File"))
			{
				specificCFLAGS = fileNode->GetPropVal(wxT("CFLAGS"), wxT(""));
				specificFileName = fileNode->GetPropVal(wxT("Name"), wxEmptyString);
				if ((specificCFLAGS != wxEmptyString) && (specificFileName != wxEmptyString))
				{
					prjTemp->m_fileSpecificCFLAGS[specificFileName] = specificCFLAGS;
					//wxPrintf(wxString(wxT("child FileSpecific file=")) + specificFileName + wxT(" specificCFLAGS=") + specificCFLAGS + wxT("\n"));
				}
			}

			fileNode = fileNode->GetNext();
		};

	}




	wxXmlNode* ignoreNode = projectNode->GetChildren(wxT("Ignore"));
	if (ignoreNode)
	{
		wxXmlNode* fNode = ignoreNode->GetChildren();
		while (fNode)
		{
			if (fNode->GetName() == wxT("File"))
			{
				wxString fileName = fNode->GetPropVal(wxT("Name"), wxEmptyString);
				if (fileName != wxEmptyString)
				{
					prjTemp->m_filesIgnored.Add(fileName);
					//wxPrintf(wxString(wxT("ignore file=")) + fileName + wxT("\n"));
				}
			}

			if (fNode->GetName() == wxT("Filter"))
			{
				wxString filterNode = fNode->GetPropVal(wxT("Name"), wxEmptyString);

				if (filterNode != wxEmptyString)
				{
					prjTemp->m_filtersIgnored.Add(filterNode);
					//wxPrintf(wxString(wxT("ignore filter=")) + fileName + wxT("\n"));
				}
			}


			fNode = fNode->GetNext();
		};
	}

	///set the default file types that must be compiled 
	if (s_declarations[wxT("TYPES_OF_FILES_TO_BE_COMPILED")] == wxEmptyString)
	{
		s_declarations[wxT("TYPES_OF_FILES_TO_BE_COMPILED")] = wxT("cpp;c");
	}

	wxStringTokenizer tkn(s_declarations[wxT("TYPES_OF_FILES_TO_BE_COMPILED")], wxT(" ;"));
	while (tkn.HasMoreTokens())
	{
		wxString newTok = tkn.GetNextToken();
		newTok.Trim().Trim(false);
		if (newTok == wxEmptyString) continue;

		prjTemp->m_typesOfFilesToBeCompiled.Add(newTok);
		//wxPrintf(wxT("extension=") + newTok + wxT("\n"));
	}
	///

	return true;
}

bool sln2gccApp::OnInit()
{
	wxLogNull logNo;
	//wxLog::Suspend();
	//wxSystemOptions::SetOption(wxT("msw.window.no-clip-children "), 0);

	//this->SetTopWindow(NULL);
	s_argc = sln2gccApp::argc;
	s_argv = sln2gccApp::argv;
	s_errorcode = 0;
	///-----

	s_executableFilePath = NEW wxFileName(wxString(s_argv[0]));
	/// exista posibilitatea ca executabilul sa fie chemat din consola cu , comanda (spre exemplu)
	/// bin\TextTool.exe  data\test.xml   (unde <data\test.xml> este parametru)
	/// caz in care argv0=bin\TextTool.exe  => pentru a reconstitui calea in care se afla executabilul se
	/// face verificarea de mai jos:
	if (!wxFileName::FileExists(wxString(s_argv[0])) || s_executableFilePath->GetVolume() == wxEmptyString)
	{
		delete s_executableFilePath;

		/// reconstruiesc calea executabilului din CurentWorkingDirectory (GetCwd) si argv0
		s_executableFilePath = NEW wxFileName(wxFileName::GetCwd() + wxFileName::GetPathSeparator() + (wxString(s_argv[0])));
	}
	///-----


	///--------- command line settings --------------
	wxCmdLineEntryDesc cmdLineDesc[] =
	{
		{ wxCMD_LINE_SWITCH, "h", "help", "display help info", wxCMD_LINE_VAL_NONE, wxCMD_LINE_PARAM_OPTIONAL | wxCMD_LINE_OPTION_HELP },
		{ wxCMD_LINE_OPTION, "i", "input", "input path\\filename (Ex: sln2gcc.xml)", wxCMD_LINE_VAL_STRING, wxCMD_LINE_OPTION_MANDATORY },
		{ wxCMD_LINE_OPTION, "t", "type", "type of build <release|debug>", wxCMD_LINE_VAL_STRING, wxCMD_LINE_OPTION_MANDATORY },
		{ wxCMD_LINE_OPTION, "p", "project", "<project name> to build or <all>", wxCMD_LINE_VAL_STRING, wxCMD_LINE_OPTION_MANDATORY },
		{ wxCMD_LINE_OPTION, "g", "gccconfig", "the gcc config <GccConfig> </GccConfig> choosed from sln2gcc.xml", wxCMD_LINE_VAL_STRING, wxCMD_LINE_OPTION_MANDATORY },
		{ wxCMD_LINE_SWITCH, "v", "verbose", "verbose. Print a lot of info.", wxCMD_LINE_VAL_NONE, wxCMD_LINE_PARAM_OPTIONAL },
		{ wxCMD_LINE_OPTION, "j", "jobs", "jobs or how many simultaneous processes.", wxCMD_LINE_VAL_NUMBER, wxCMD_LINE_PARAM_OPTIONAL },
		{ wxCMD_LINE_OPTION, "e", "errors", "stop compile after x errors. default is 10.", wxCMD_LINE_VAL_NUMBER, wxCMD_LINE_PARAM_OPTIONAL },
		{ wxCMD_LINE_NONE }
	};

	m_parser = NEW wxCmdLineParser(cmdLineDesc, s_argc, s_argv);
	m_parser->SetSwitchChars(wxT("-"));

	//wxString logo = wxString(wxT("sln2gcc v")) + VERSION + wxT(" support=cristian.vasile@gameloft.com");
	//m_parser->SetLogo(logo);
	///--------- command line settings --------------

	return true;
}

void sln2gccApp::Clean()
{
	static bool s_bClean = false;

	if (s_bClean)
	{
		return;
	}

	s_bClean = true;

	delete m_parser;
	m_parser = NULL;
	delete s_executableFilePath;
	s_executableFilePath = NULL;


	VcProject::Clear();


	//VcProject::s_compileCommandList.clear();
	//VcProject::s_linkCommandList.clear();

}

void sln2gccApp::Abort()
{
	wxMutexLocker scope(*s_mutexMain);

	wxPrintf(_T("\nAborting...\n"));

	// terminate all other sln2gcc.exe threads except this one
	// because those threads create other processes that create others and so on...

	// get current thread information
	int currentProcessID = GetCurrentProcessId();
	int currentThreadID = GetCurrentThreadId();


	sln2gccApp::s_bAbortProgram = true;
	if (sln2gccApp::GetErrorCode() == 0)
		sln2gccApp::SetErrorCode(1);

	wxThread::Sleep(5000);


	s_elapsedTime = wxDateTime::UNow().Subtract(sln2gccApp::s_startTime);
	int elapsedMinutes = s_elapsedTime.GetMinutes();
	int elapsedSeconds = s_elapsedTime.GetSeconds().ToLong() % 60;
	PRINTF(COLOR_GREEN, wxString(wxT("\nCompile time %dm%ds\n")), elapsedMinutes, elapsedSeconds);

	CommandDescriptor::KillAllProcesses();

	//kill the main process (recursivelly all childs will be killed)
	sln2gccApp::KillProcessByPID(currentProcessID);


	/*
	// take system snapshot
	HANDLE hSnapShot = CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, currentProcessID);
	if (hSnapShot != INVALID_HANDLE_VALUE)
	{
	THREADENTRY32 otherThread;
	otherThread.dwSize = sizeof(otherThread);

	// iterate over threads
	if (Thread32First(hSnapShot, &otherThread))
	{
	do
	{
	// check to see if otherThread belongs to this process
	// careful not to suicide
	if (otherThread.th32OwnerProcessID == currentProcessID &&
	otherThread.th32ThreadID != currentThreadID)
	{
	// get thread handle...
	HANDLE threadHandle = OpenThread(THREAD_ALL_ACCESS, false , otherThread.th32ThreadID);

	if (threadHandle)
	{
	// ...and kill it
	if (WaitForSingleObject(threadHandle, 100) == WAIT_TIMEOUT)
	if (!TerminateThread(threadHandle, 0)) // dangerous function used !
	wxPrintf(_T("\nTerminateThread failed!"));

	CloseHandle(threadHandle);
	}
	}

	otherThread.dwSize = sizeof(otherThread);

	} while (Thread32Next(hSnapShot, &otherThread));
	}
	CloseHandle(hSnapShot);
	}
	*/

#if USE_MEMORY_LEAKS_TRACKER
	MemMgr::InternalMemTracker::Instance()->PrintStatus();
	MemMgr::InternalMemTracker::Instance()->Destroy();
#endif

	//wxFileName *FN;

	//FN = NEW wxFileName(sln2gccApp::s_declarations[wxT("DISTCC")]);
	//sln2gccApp::KillProcessByName((const char*)FN->GetFullName().ToAscii());
	//delete FN;

	//FN = NEW wxFileName(sln2gccApp::s_declarations[wxT("CPP")]);
	//sln2gccApp::KillProcessByName((const char*)FN->GetFullName().ToAscii());
	//delete FN;

	//FN = NEW wxFileName(sln2gccApp::s_declarations[wxT("CC")]);
	//sln2gccApp::KillProcessByName((const char*)FN->GetFullName().ToAscii());
	//delete FN;

	//FN = NEW wxFileName(sln2gccApp::s_declarations[wxT("LD")]);
	//sln2gccApp::KillProcessByName((const char*)FN->GetFullName().ToAscii());
	//delete FN;

	//FN = NEW wxFileName(sln2gccApp::s_declarations[wxT("LS")]);
	//sln2gccApp::KillProcessByName((const char*)FN->GetFullName().ToAscii());
	//delete FN;

	/*
	FN = NEW wxFileName(sln2gccApp::s_declarations[wxT("CCACHE")]);
	sln2gccApp::KillProcessByName((const char*)FN->GetFullName().ToAscii());
	delete FN;
	*/

	//sln2gccApp::KillProcessByName("cc1plus.exe");
	//sln2gccApp::KillProcessByName("cc1.exe");
	//sln2gccApp::KillProcessByName("as.exe");
	//sln2gccApp::KillProcessByName("ld.exe");


	wxThread::Sleep(1000);

	//Clean();


}


void AbortProgram(int return_code)
{
	sln2gccApp::Abort();

	exit(return_code);
}

#ifdef __WXMSW__
BOOL WINAPI ConsoleHandler(DWORD CEvent)
{
	//char mesg[128];

	switch (CEvent)
	{
	case CTRL_C_EVENT:
		wxPrintf(_T("CTRL+C received!\n"));
		AbortProgram(1);
		break;
	case CTRL_BREAK_EVENT:
		wxPrintf(_T("CTRL+BREAK received!\n"));
		AbortProgram(1);
		break;
	case CTRL_CLOSE_EVENT:
		wxPrintf(_T("Program being force closed!\n"));
		AbortProgram(1);
		break;
	case CTRL_LOGOFF_EVENT:
		wxPrintf(_T("User is logging off!\n"));
		break;
	case CTRL_SHUTDOWN_EVENT:
		wxPrintf(_T("User is logging off!\n"));
		AbortProgram(1);
		break;

	}
	return TRUE;
}
#endif

wxString sln2gccApp::ExpandExpression(wxString expression, Declarations& s_declarations, wxString& warningMsg, bool removeUntranslatedSymbols)
{
	if (!HAVE_MACROS(expression)) return expression;

	warningMsg = wxEmptyString;
	int numberOfDeclFound = 0;
	wxString wrongStr = wxEmptyString;
	wxRegEx regularExp(wxT("\\$\\([ _0-9a-zA-Z]*\\)"));
	if (!regularExp.Matches(expression)) return expression;

	size_t start = 0;
	size_t len = 0;
	while (regularExp.GetMatch(&start, &len))
	{
		//wxPrintf(expression.Mid(start, len) + wxT(" match found\n"));

		wxString keyOriginal = expression.Mid(start, len);
		wxString keyBackup = keyOriginal;
		keyBackup.Replace(wxT("$"), wxT("^"));
		wxString key = keyOriginal;
		key.Replace(wxT("$"), wxT(" "));
		key.Replace(wxT("("), wxT(" "));
		key.Replace(wxT(")"), wxT(" "));
		key.Trim(true).Trim(false);

		wxString valueForKey = s_declarations[key];
		//PRINTF(COLOR_WHITE, key + wxT("=") + valueForKey + wxT("\n"));
		if (valueForKey == wxEmptyString)
		{
			///try to take this value from ENVIRONMENT VARIABLES!
			if (!wxGetEnv(key, &valueForKey))
			{
				valueForKey = wxEmptyString;
			}
			//PRINTF(COLOR_YELLOW, key + wxT("=") + valueForKey + wxT("\n"));
		}

		valueForKey.Trim(true).Trim(false);

		if ((valueForKey != wxEmptyString) || ((valueForKey == wxEmptyString) && removeUntranslatedSymbols))
			expression.Replace(keyOriginal, valueForKey);
		else
			expression.Replace(keyOriginal, keyBackup);

		if (!regularExp.Matches(expression)) break;
	}

	expression.Replace(wxT("\\\\"), wxT("\\"));
	expression.Replace(wxT("//"), wxT("/"));
	expression.Replace(wxT("^"), wxT("$"));

	return expression;
}


wxString sln2gccApp::GetRelativePath(wxString startPath, wxString targetPath)
{
	wxFileName startPathFN(startPath);
	wxFileName targetPathFN(targetPath);
	startPathFN.Normalize();
	targetPathFN.Normalize();

	startPath = startPathFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR);
	targetPath = targetPathFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR);

	//wxPrintf(wxString(wxT("startPath=")) + startPath + wxT("\n"));
	//wxPrintf(wxString(wxT("targetPath=")) + targetPath + wxT("\n"));



	if (startPathFN.GetVolume().Upper() != targetPathFN.GetVolume().Upper())
	{
		return targetPath;
	}



	int length = startPath.size() < targetPath.size() ? startPath.size() : targetPath.size();
	int lastGoodIndex = 0;
	wxString str1 = startPath.Upper();
	wxString str2 = targetPath.Upper();
	int t = 0;
	for (t = 0; t < length; t++)
	{
		//String str1 = startPath.Substring(0, t).ToUpper();
		//Program.Print("str1" + str1);

		//String str2 = targetPath.Substring(0, t).ToUpper();
		//Program.Print("str2" + str2);

		if ((str1[t] == '\\' || str1[t] == '/') && (str1[t] == str2[t]))
		{
			lastGoodIndex = t;
		}
		else
			if (str1[t] != str2[t])
			{
				break;
			}
	}



	wxString basePath = startPath.Mid(0, lastGoodIndex);
	//wxPrintf(wxString(wxT("getRelativePath basePath:")) + basePath);

	bool basePathIsRoot = false;
	// This is a special fix for windows paths
	if ((basePath.size() > 1) && (basePath.LastIndexOf(':') == (basePath.size() - 1)))
	{
		// In case of windows paths like D:\folder1\folder2\, If the base path is root 
		// aka D:\ , the  basePath=D: .
		// In this case the "\" will/must stay with the diffPathTarget to avoid a gcc bug.
		// That gcc bug is in case of include paths that use relative paths , but relative to the root
		// Example: if the command line:  gcc -I../../folder1/folder2/  , the gcc will replace the 
		// path ../../tools/include with D:../../folder1/folder2/ ehich is wrong and the path is not a valid path.
		// So, for relative paths that have the base the root,  the relative paths must start with /
		// Correct example: gcc -I/../../folder1/folder2/
		basePathIsRoot = true;
	}


	wxString diffPathTarget = targetPath.Mid(lastGoodIndex);

	if (diffPathTarget != wxEmptyString)
	{
		//diffPathTarget = diffPathTarget.Replace('\\', '/');

		if (diffPathTarget.size() > 1)
			if (diffPathTarget.LastIndexOf('\\') == (diffPathTarget.size() - 1)) diffPathTarget = diffPathTarget.Mid(0, diffPathTarget.size() - 1);
	}
	//AndroidMake.Program.Print("getRelativePath diffPath:" + diffPathTarget, ConsoleColor.White);

	wxString diffPathStart = startPath.Mid(t);

	if (diffPathStart != wxEmptyString)
	{
		//diffPathStart = diffPathStart.Replace('\\', '/');


		if (diffPathStart.IndexOf('\\') == 0) diffPathStart = diffPathStart.Mid(1);

		if (diffPathStart.size() > 1)
			if (diffPathStart.LastIndexOf('\\') == (diffPathStart.size() - 1)) diffPathStart = diffPathStart.Mid(0, diffPathStart.size() - 1);
	}
	//AndroidMake.Program.Print("getRelativePath diffPath:" + diffPathStart, ConsoleColor.White);

	int countDotDot = 0;
	if (diffPathStart.size() > 0)
	{
		int lastIndex = -1;
		do
		{
			lastIndex = diffPathStart.IndexOf('\\', lastIndex + 1);
			countDotDot++;
		} while (lastIndex != -1);
	}
	//AndroidMake.Program.Print("getRelativePath countDotDot:" + countDotDot, ConsoleColor.White);

	wxString relativePath = diffPathTarget;

	for (t = 0; t < countDotDot; t++)
	{
		relativePath = wxString(wxT("..\\")) + relativePath;
	}


	if (basePathIsRoot)
		relativePath = wxString(wxT("\\")) + relativePath;

	//AndroidMake.Program.Print("getRelativePath relativePath:" + relativePath, ConsoleColor.White);


	//AndroidMake.Program.Print("getRelativePath ---------------------------------", ConsoleColor.White);

	//wxPrintf(wxString(wxT("relativePath=")) + relativePath + wxT("\n"));
	return relativePath;

}

bool sln2gccApp::ToolchainCheck(wxString tool, Declarations& s_declarations)
{
	wxString errorMsg = wxEmptyString;
	wxString exe = s_declarations[tool];
	exe = sln2gccApp::ExpandExpression(exe, s_declarations, errorMsg);
	wxFileName exeFile(exe);
	bool absolutePath = exeFile.IsAbsolute();
	if (absolutePath && !wxFileName::FileExists(exe))
	{
		ERROR_PRINTF(wxString(wxT("file")), wxT("the path for ") + tool + wxT("=") + exe + wxT(" does not exist!\n"));
		return false;
	}
	else
		if (!absolutePath)
		{
			if (!wxFileName::FileExists(s_toolChain + SEP + exe))
			{
				if (wxFileName::FileExists(s_toolChainBackup + SEP + exe))
				{
					WARNING_PRINTF(wxString(wxT("file")), wxT("The tool") + tool + wxT("=") + exe + wxT(" does not exist in TOOLCHAIN='") + s_toolChain + wxT("'!\n"));
					WARNING_PRINTF(wxString(wxT("file")), wxT("But exists in TOOLCHAIN_BACKUP='") + s_toolChainBackup + wxT("'!\n"));
					s_declarations[tool] = s_toolChainBackup + SEP + exe;
					absolutePath = true;
					WARNING_PRINTF(wxString(wxT("file")), wxT("The tool") + tool + wxT("=") + exe + wxT(" will be used with full path from TOOLCHAIN_BACKUP: '") + s_declarations[tool] + wxT("'!\n"));
					return true;
				}
				else
				{
					ERROR_PRINTF(wxString(wxT("file")), wxT("the tool ") + tool + wxT("=") + exe + wxT(" does not exist in TOOLCHAIN='") + s_toolChain + wxT("'!\n"));

					if (s_toolChain != s_toolChainBackup)
					{
						ERROR_PRINTF(wxString(wxT("file")), wxT("the tool ") + tool + wxT("=") + exe + wxT(" does not exist in TOOLCHAIN_BACKUP='") + s_toolChainBackup + wxT("'!\n"));
					}

					return false;
				}
			}
		}


	//if(absolutePath) //make it local
	//{
	//	s_declarations[tool] = exeFile.GetFullName();
	//}

	///if the tool chains are ok, we will check and stop any processes that runs , so you cannot compile in the same time two projects
	//KillProcessByName((const char*)(exeFile.GetFullName().ToAscii()));


	return true;
}

#ifdef __WXMSW__
/*
int test()
{
SC_HANDLE hSCM    = NULL;
PUCHAR  pBuf    = NULL;
ULONG  dwBufSize   = 0x00;
ULONG  dwBufNeed   = 0x00;
ULONG  dwNumberOfService = 0x00;
LPENUM_SERVICE_STATUS_PROCESS pInfo = NULL;
hSCM = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS  | SC_MANAGER_CONNECT);
if (hSCM == NULL)
{
printf_s("OpenSCManager fail \n");
return 0xffff0001;
}
EnumServicesStatusEx(
hSCM,
SC_ENUM_PROCESS_INFO,
SERVICE_WIN32, // SERVICE_DRIVER
SERVICE_STATE_ALL,
NULL,
dwBufSize,
&dwBufNeed,
&dwNumberOfService,
NULL,
NULL);
if (dwBufNeed < 0x01)
{
printf_s("EnumServicesStatusEx fail ?? \n");
return 0xffff0002;
}
dwBufSize = dwBufNeed + 0x10;
pBuf  = (PUCHAR) malloc(dwBufSize);
EnumServicesStatusEx(
hSCM,
SC_ENUM_PROCESS_INFO,
SERVICE_WIN32,  // SERVICE_DRIVER,
SERVICE_ACTIVE,  //SERVICE_STATE_ALL,
pBuf,
dwBufSize,
&dwBufNeed,
&dwNumberOfService,
NULL,
NULL);
pInfo = (LPENUM_SERVICE_STATUS_PROCESS)pBuf;
for (ULONG i=0;i<dwNumberOfService;i++)
{
wprintf_s(L"Display Name \t : %s \n", pInfo[i].lpDisplayName);
wprintf_s(L"Service Name \t : %s \n", pInfo[i].lpServiceName);
wprintf_s(L"Process Id \t : %04x (%d) \n", pInfo[i].ServiceStatusProcess.dwProcessId, pInfo[i].ServiceStatusProcess.dwProcessId);
}
free(pBuf);
CloseServiceHandle(hSCM);
return 0;
}

#include <tlhelp32.h>
BOOL ListProcessModules( DWORD dwPID )
{
HANDLE hModuleSnap = INVALID_HANDLE_VALUE;
MODULEENTRY32 me32;

// Take a snapshot of all modules in the specified process.
hModuleSnap = CreateToolhelp32Snapshot( TH32CS_SNAPMODULE, dwPID );
if( hModuleSnap == INVALID_HANDLE_VALUE )
{
_tprintf( TEXT("CreateToolhelp32Snapshot (of modules)\n") );
return( FALSE );
}

// Set the size of the structure before using it.
me32.dwSize = sizeof( MODULEENTRY32 );

// Retrieve information about the first module,
// and exit if unsuccessful
if( !Module32First( hModuleSnap, &me32 ) )
{
_tprintf( TEXT("Module32First") );  // show cause of failure
CloseHandle( hModuleSnap );           // clean the snapshot object
return( FALSE );
}

// Now walk the module list of the process,
// and display information about each module
do
{
//_tprintf( TEXT("     MODULE NAME:     %s\n"),   me32.szModule );
//_tprintf( TEXT("\n     Executable     = %s"),     me32.szExePath );
//_tprintf( TEXT("\n     Process ID     = 0x%08X"),         me32.th32ProcessID );
//_tprintf( TEXT("\n     Ref count (g)  = 0x%04X"),     me32.GlblcntUsage );
//_tprintf( TEXT("\n     Ref count (p)  = 0x%04X"),     me32.ProccntUsage );
//_tprintf( TEXT("\n     Base address   = 0x%08X"), (DWORD) me32.modBaseAddr );
//_tprintf( TEXT("\n     Base size      = %d"),             me32.modBaseSize );

} while( Module32Next( hModuleSnap, &me32 ) );

CloseHandle( hModuleSnap );
return( TRUE );
}
*/

bool sln2gccApp::KillProcessByPID(long pid)
{
	wxMutexLocker scope(*s_mutexMain);

	PRINTF(COLOR_RED, wxT("Try to Kill Pid %lu using KillProcessByPID function!\n"), pid);

	//if nothing works you have to use system("taskkill /F /T /IM program.exe");
	wxString taskkill = wxString::Format(wxString(wxT("taskkill /F /T /pid ")) + wxT(" %d"), (int)pid);
	//PRINTF(COLOR_RED, taskkill + wxT("\n"));
	wxArrayString stdOut;
	wxArrayString stdErr;

	wxDISABLE_DEBUG_SUPPORT();
	wxSetAssertHandler(Handler);

	long returnCode = wxExecute(taskkill, stdOut, stdErr, wxEXEC_SYNC);
	bool errorFound = false;
	if (stdOut.size() > 0)
	{

		PRINTF(COLOR_RED, wxT("\n"));
		for (unsigned int i = 0; i < stdOut.size(); i++)
		{
			PRINTF(COLOR_RED, stdOut[i] + wxT("\n"));
			if (stdOut[i].Find(wxT("ERROR: ")) != wxNOT_FOUND)
			{
				errorFound = true;
			}
		}
	}
	if (stdErr.size() > 0)
	{
		PRINTF(COLOR_RED, wxT("\n"));
		for (unsigned int i = 0; i < stdErr.size(); i++)
		{
			PRINTF(COLOR_RED, stdErr[i] + wxT("\n"));
			if (stdErr[i].Find(wxT("ERROR: ")) != wxNOT_FOUND)
			{
				errorFound = true;
			}
		}
	}

	if (errorFound)
	{
		return false;
	}

	if (returnCode != 0)
	{
		HANDLE hProcessToKill = OpenProcess(PROCESS_TERMINATE, 0, pid);  // gets handle to process
		if (hProcessToKill != NULL)
		{
			TerminateProcess(hProcessToKill, 0);   // Terminate process by handle
			WaitForSingleObject(hProcessToKill, 3000);
			CloseHandle(hProcessToKill);  // close the handle   
			return true;
		}
	}
	else
	{
		return true;
	}

	PRINTF(COLOR_RED, wxT("Pid %lu cannot be killed by KillProcessByPID function!\n"), pid);
	return false;
}

bool sln2gccApp::KillProcessByName(const char *szProcessToKill)
{
	bool ret = false;

	PRINTF(COLOR_YELLOW, wxT("Seek and destroy %s ...\n"), szProcessToKill);


	{
		// get system processes snapshot (all processes)
		HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);

		int currentPID = GetCurrentProcessId();

		if (hSnapshot)
		{
			PROCESSENTRY32 pe32;

			pe32.dwSize = sizeof(PROCESSENTRY32);

			if (Process32First(hSnapshot, &pe32))
			{
				// iterate over processes
				do
				{
					// careful not to suicide :D
					if (pe32.th32ProcessID == currentPID)
						continue;

					// convert process name from wide chars to chars
					wstring currentW(pe32.szExeFile);
					string current;
					current.assign(currentW.begin(), currentW.end());

					// compare and kill
					if (!strcmp(current.c_str(), szProcessToKill))
					{
						if (KillProcessByPID(pe32.th32ProcessID))
						{
							PRINTF(COLOR_RED, wxT(", %u"), pe32.th32ProcessID);
							ret = true;
						}
						else
						{
							PRINTF(COLOR_RED, wxT("Failed to kill process %s(%d) with KillProcessByPID!\n"), szProcessToKill, pe32.th32ProcessID);
							ret = false;
						}
					}

				} while (Process32Next(hSnapshot, &pe32));
			}

			CloseHandle(hSnapshot);
		}

		return ret;
	}
}

void sln2gccApp::ChangeColour(WORD theColour)
{
	HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);  // Get handle to standard output

	SetConsoleTextAttribute(hConsole, theColour);  // set the text attribute of the previous handle
}
#else

bool sln2gccApp::KillProcessByName(const char *szProcessToKill)
{
	return true;
}

void sln2gccApp::ChangeColour(int theColour)
{
	//HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);  // Get handle to standard output

	//SetConsoleTextAttribute(hConsole,theColour);  // set the text attribute of the previous handle
}
#endif

