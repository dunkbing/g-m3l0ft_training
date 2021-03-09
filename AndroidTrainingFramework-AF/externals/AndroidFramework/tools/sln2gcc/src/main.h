#ifndef MAIN_H_INCLUDED
#define MAIN_H_INCLUDED

#include <wx/setup.h>
#include <wx/app.h>
#include <wx/filename.h>
#include <wx/process.h>
#include <wx/dynarray.h>
#include <wx/cmdline.h>
#include <vector>
#include <wx/hashmap.h>
#include <wx/xml/xml.h>
#include <stdio.h>


/**
 * @author Cristian Vasile <vasile.cristian@gmail.com>
 */


#define VERSION wxT("1.8.0")



#define CHECK_MACRO_ON_PROJECT(_project_, MacroName, mapToStore, defaultValue)           \
                        {                                                                \
                            VcProject* __prj__ = _project_;                              \
                            wxString fileWithProblem = (__prj__ && !IS_FALSE(__prj__->m_s2gFile)) ? __prj__->m_s2gFile : sln2gccApp::s_inputFile; \
                            if(mapToStore[MacroName] == wxT("not_set"))                  \
                            {                                                            \
                                mapToStore[MacroName]=defaultValue;						 \
                                WARNING_PRINTF(fileWithProblem, wxString(wxT("'")) + MacroName + wxT("' not pressent in config! Will assume default value '") + defaultValue wxT("'.\n")); \
                            }                                                            \
                            else if((mapToStore[MacroName] == wxT("")) && (defaultValue != wxT(""))) \
                            {                                                            \
                                mapToStore[MacroName]=defaultValue;						 \
                                WARNING_PRINTF(fileWithProblem, wxString(wxT("'")) + MacroName + wxT("' does not have a value! Will assume default value '") + defaultValue wxT("'.\n")); \
                            }                                                            \
                        }                       

#define CHECK_MACRO(MacroName, mapToStore, defaultValue)      CHECK_MACRO_ON_PROJECT(NULL, MacroName, mapToStore, defaultValue)


#define IS_TRUE(str) ((str == wxT("1")) || (str.Upper() == wxT("TRUE")) || (str.Upper() == wxT("YES")))

#define IS_FALSE(str) ((str == wxEmptyString) || (str == wxT("0")) || (str.Upper() == wxT("FALSE")) || (str.Upper() == wxT("NO")))

#define HAVE_MACROS(str) (str.Contains(wxT("$(")))

#define CHECK_MACRO_IF_IS_ENABLED(MacroName, mapToStore) IS_TRUE(mapToStore[MacroName])



#define STR_DEBUG wxT("debug")
#define STR_RELEASE wxT("release")

#define COMPILE_COMMANDS_FILE wxT("CompileCommands.txt")
#define LINK_COMMANDS_FILE wxT("LinkCommands.txt")

WX_DECLARE_STRING_HASH_MAP( int, MapStringToInt );

WX_DECLARE_STRING_HASH_MAP( wxString, MapStringToString );

WX_DECLARE_STRING_HASH_MAP( long, MapLongToString );

class Declarations: public MapStringToString
{
public:
    bool IsDefined(wxString def)
    {
        if(GetValue(def) == wxEmptyString)
            return false;
        else
            return true;
    }

    inline wxString GetValue(wxString def)
    {
        return (*this)[def];
    }
};


/////////////////////////////////////////////////////////////////////////////////////////////////

class VcProject;

class
sln2gccApp : public wxAppConsole
{
	public:

		sln2gccApp(){};
        ~sln2gccApp();


    protected:

        static int      s_argc;
        static wxChar **s_argv;


        virtual int OnRun();

        static wxMutex* s_mutexMain;

    private:
        /** overall time elapsed */
        static wxTimeSpan s_elapsedTime;

        /** the start time */
        static wxDateTime s_startTime;

        /** Command line parser. */
        static wxCmdLineParser *m_parser;

        /** the error code */
        static int s_errorcode;

        /** This parameter will be set to true to siglan all threads that the program is aborted. */
        static bool s_bAbortProgram;

        /** this will enable or disable the verbose output (much more info) */
        static bool s_verbose;

        /** this will store a parameter form command line. Can have value "all" or a name of a project.*/
        static wxString s_projectToBuild;

        /** the maximul number of processes that can run in the same time on the local computer. */
        static long s_jobs;

        /** a list with the possible distcc hosts */
        static wxArrayString s_distccHosts;

        /** the maximul number of processes that can run in the same time using distcc. */
        static long     s_distccJobs;

        static bool     s_distccEnable;

        static bool     s_distccUseFullPath;

        static wxString s_toolChain;
        static wxString s_toolChainBackup;

    public:

        static wxString s_inputFile;
        static wxString s_outputPath;
		static wxString s_precompiledPath;

        static wxString s_typeOfBuild; //can be debug, release, clean
        static wxString s_gccConfig;//special config

        //static long s_errorsNoMax;
        static long s_errorsNo;
        static bool s_gcc2msvc;



		virtual bool OnInit();
		//virtual void OnInitCmdLine(wxCmdLineParser& parser);

        static void Clean();

        /**
         * This function will load the data from <Project></Project> xml node!
         */
        bool LoadProjectSettings(wxXmlNode* projectNode, VcProject* prjTemp);


        static void SetToolChainPath(wxString toolChainPath)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_toolChain = toolChainPath;
        }
        static wxString GetToolChainPath()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_toolChain;
        }

        static void SetToolChainBackupPath(wxString toolChainBackupPath)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_toolChainBackup = toolChainBackupPath;
        }
        static wxString GetToolChainBackupPath()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_toolChainBackup;
        }


        static void SetDistccUseFullPath(bool distccUseFullPath)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_distccUseFullPath = distccUseFullPath;
        }
        static bool GetDistccUseFullPath()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_distccUseFullPath;
        }

        static void SetDistccEnable(bool distccEnable)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_distccEnable = distccEnable;
        }
        static bool IsDistccEnable()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_distccEnable;
        }

        static void SetErrorCode(int errorcode)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_errorcode = errorcode;
        }
        static int GetErrorCode()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_errorcode;
        }

        static void SetAbortProgram(bool abortProgram)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_bAbortProgram = abortProgram;
        }
        static bool GetAbortProgram()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_bAbortProgram;
        }

        static void SetJobs(int jobs)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_jobs = jobs;
        }
        static int GetJobj()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_jobs;
        }

        static void SetEnableVerbose(bool verbose)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_verbose = verbose;
        }
        static bool GetEnableVerbose()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_verbose;
        }

        static void SetDISTCCJobs(int jobs)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_distccJobs = jobs;
        }
        static int GetDISTCCJobs()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_distccJobs;
        }

        static void SetDISTCCHosts(wxArrayString hostslist)
        {
            wxMutexLocker scope(*s_mutexMain);            
            s_distccHosts = hostslist;
            int size = s_distccHosts.size();
            wxString val = wxEmptyString;
            for(int i=0; i<size; i++)
            {
                val += s_distccHosts[i] + wxT(" ");
            }
            wxSetEnv(wxT("DISTCC_HOSTS"), val);
        }
        static wxArrayString GetDISTCCHosts()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_distccHosts;
        }
        static void RemoveDISTCCHosts(unsigned int hostIndex)
        {            
            wxMutexLocker scope(*s_mutexMain);
            
            if(hostIndex >= s_distccHosts.size())
                return;

            s_distccHosts.RemoveAt(hostIndex);
            

            SetDISTCCHosts(s_distccHosts);
        }
        static void RemoveDISTCCHosts(wxString host)
        {
            wxMutexLocker scope(*s_mutexMain);
             
            int size = s_distccHosts.size();
            for(int i=0; i<size; i++)
            {
               if(host.compare(s_distccHosts[i]) == 0)
               {
                    RemoveDISTCCHosts(i);
                    SetDISTCCHosts(s_distccHosts);
                    break;
               }
            }                
        }


        static void SetEnableGCC2MSVC(bool gcc2msvc)
        {
            wxMutexLocker scope(*s_mutexMain);
            s_gcc2msvc = gcc2msvc;
        }
        static bool GetEnableGCC2MSVC()
        {
            wxMutexLocker scope(*s_mutexMain);
            return s_gcc2msvc;
        }

		static wxFileName* s_executableFilePath;

        static MapStringToInt s_fileChangedMap;

        static Declarations s_declarations;


        static wxString ExpandExpression(wxString expression, Declarations& declarations, wxString& warningMsg, bool putSpaceIfCannotTranslate = true);
        static wxString GetRelativePath(wxString startPath, wxString targetPath);
		static bool ToolchainCheck(wxString tool, Declarations& declarations);
        static bool KillProcessByPID(long pid);
        static bool KillProcessByName(const char *szProcessToKill);
        static void Abort();
		#ifdef __WXMSW__
		static void ChangeColour(WORD theColour);
		#else
		static void ChangeColour(int theColour);
		#endif

		// needed to convert paths
		static const wxString msc_backslash;
		static const wxString msc_slash;
		static const wxString msc_doubleSlash;

		//static const wxString msc_debugLibsDir;
		//static const wxString msc_releaseLibsDir;
};

void AbortProgram(int);

#define FILE_CHANGED_NOT_SET    0
#define FILE_CHANGED            1
#define FILE_CHANGED_NOT        2



#define CHILD_PROCESS_TIMEOUT_WARNING1   (600)  ///10 minutes
#define CHILD_PROCESS_TIMEOUT_WARNING2   (900)  ///15 minutes
#define CHILD_PROCESS_TIMEOUT_WARNING3  (1140)  ///19 minutes
#define CHILD_PROCESS_TIMEOUT           (1200)  ///20 minutes
#define DISTCC_PROCESS_TIMEOUT           (300)  /// 5 minutes

//#define CHILD_PROCESS_TIMEOUT_WARNING1   30  ///10 minutes
//#define CHILD_PROCESS_TIMEOUT_WARNING2   40  ///15 minutes
//#define CHILD_PROCESS_TIMEOUT_WARNING3   50  ///19 minutes
//#define CHILD_PROCESS_TIMEOUT            60  ///20 minutes


#define SEP wxFileName::GetPathSeparator()

#define EXE_PATH sln2gccApp::s_executableFilePath->GetPath(wxPATH_GET_VOLUME|wxPATH_GET_SEPARATOR)


#define REPLACE_SEPARATORS_WITH_SPACE(str)                                                    \
                            str.Replace(wxT("\n"), wxT(" "));                                 \
                            str.Replace(wxT("\r"), wxT(" "));                                 \
                            str.Replace(wxT("\t"), wxT(" "));                                 \
                            str.Replace(wxT(";"),  wxT(" "));                                 \
                            str.Replace(wxT("%"),  wxT("$"));                                 \
                            str.Replace(wxT("\\\\"),  wxT("\\"));                             \
                            str.Replace(wxT("//"),  wxT("/"));                                \
                            while(str.Replace(wxT("  "),  wxT(" ")));

#ifdef __WXMSW__


#define COLOR_WHITE			FOREGROUND_BLUE|FOREGROUND_RED|FOREGROUND_GREEN|FOREGROUND_INTENSITY
#define COLOR_GRAY		    FOREGROUND_BLUE|FOREGROUND_GREEN|FOREGROUND_RED
#define COLOR_RED			FOREGROUND_RED|FOREGROUND_INTENSITY
#define COLOR_YELLOW		FOREGROUND_RED|FOREGROUND_GREEN|FOREGROUND_INTENSITY
#define COLOR_GREEN			FOREGROUND_GREEN|FOREGROUND_INTENSITY
#define COLOR_BLUE			FOREGROUND_BLUE|FOREGROUND_INTENSITY
#define COLOR_PURPLE		FOREGROUND_RED|FOREGROUND_BLUE
#define COLOR_BLUE_LIGHT    FOREGROUND_GREEN|FOREGROUND_BLUE|FOREGROUND_INTENSITY
#define COLOR_YELLOW_DARK        FOREGROUND_RED|FOREGROUND_GREEN
#else
#define COLOR_WHITE     0
#define COLOR_GRAY      0
#define COLOR_RED       0
#define COLOR_YELLOW    0
#define COLOR_GREEN     0
#define COLOR_BLUE      0
#endif


#define PRINTF(color, ...)                  \
{                                           \
    sln2gccApp::ChangeColour(color);        \
    wxPrintf(__VA_ARGS__);                  \
    sln2gccApp::ChangeColour(COLOR_WHITE);  \
}


#define WARNING_PRINTF(file, ...)                                       \
              {                                                         \
				sln2gccApp::ChangeColour(COLOR_YELLOW);                 \
                wxPrintf(file + wxT("(1): warning: ") + __VA_ARGS__);   \
                sln2gccApp::ChangeColour(COLOR_WHITE);                  \
              }


#define ERROR_PRINTF(file, ...)                         \
{                                                       \
    sln2gccApp::ChangeColour(COLOR_RED);                \
    wxPrintf(file + wxT("(1): error: ") + __VA_ARGS__); \
    sln2gccApp::ChangeColour(COLOR_WHITE);              \
}


#define VERBOSE(color, ...)                                     \
              {                                                 \
                if(sln2gccApp::GetEnableVerbose())              \
                {                                               \
                    sln2gccApp::ChangeColour(color);            \
                    wxPrintf(__VA_ARGS__);                      \
                    sln2gccApp::ChangeColour(COLOR_WHITE);      \
                }                                               \
              }

/////////////////////////////////////////////////////////////////////////////////////////////////


#endif // MAIN_H_INCLUDED
