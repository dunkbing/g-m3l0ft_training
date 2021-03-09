#ifndef COMPILETHREAD_H_INCLUDED
#define COMPILETHREAD_H_INCLUDED


#include <wx/process.h>
#include <wx/thread.h>
#include <wx/log.h>
#include <vector>
#include <wx/datetime.h>


#include "main.h"

class CommandDescriptor;

enum
{
    COMMAND_TYPE_NOT_DEFINED    = 0, ///not defined
    COMMAND_TYPE_COMPILE        = 1, ///compile the src
    COMMAND_TYPE_LINK           = 2, ///link
    COMMAND_TYPE_DSYM           = 3, ///generate debug symbols file = dsym
    COMMAND_TYPE_STRIP          = 4  ///strip all debug symbols from the output
};


class ExternalCommandThread : public wxThread
{
public:
    ExternalCommandThread(CommandDescriptor* cmdDesc);
    ~ExternalCommandThread();

    // thread execution starts here
    virtual void* Entry();

    // called when the thread exits - whether it terminates normally or is
    // stopped with Delete() (but not when it is Kill()ed!)
    virtual void OnExit();

    wxMutex *m_mutexProtect;
    CommandDescriptor* m_cmdDesc;
};

typedef std::vector<ExternalCommandThread*> ThreadsArray;
typedef std::vector<wxProcess*> ProcessesArray;

class CommandDescriptor
{

protected:
    int m_type;

    bool m_isJobEnd;
    bool m_hasErrors;
    //bool m_tryToRunLocally;//used only for distcc processes that failed.

    wxArrayString m_output;
    wxArrayString m_errors;

    wxString m_target;
    wxString m_label;


    ExternalCommandThread* m_thread;
    wxProcess *m_process;
    bool m_started;
    bool m_inExecution;
    wxDateTime m_startTime;

    wxMutex* m_mutex;

    int m_warningLevel;

public:


    /**
     * Used if you want to wait until this job is finished.
     */
    bool m_waitUntilJobEnds;


    /**
     * Process only if all the previous jobs are finished
     */
    bool m_waitForAllPrevJobsToEnd;

    
    static ProcessesArray s_processesArray;
    static void KillAllProcesses();
    //static void DeleteProcesses();
    static int s_commandsExecuted;
    static int s_commandsTotalToExecute;

    wxString m_command;

    /**
    * This represent the working folder for this command.
    */
    wxString m_workingDir;//working path

    

    CommandDescriptor(wxString target, wxString workingDir, wxString label = wxEmptyString, bool waitToExit = false, bool waitForAllPrevJobsToEnd = false);
    virtual ~CommandDescriptor();

    virtual bool Execute();

    int GetWarningLevel();
    int GetType();
    wxString GetTarget();
    wxString GetLabel();
    wxDateTime GetStartTime();
    ExternalCommandThread* GetThread();
    wxProcess* GetProcess();

    void SetThread(ExternalCommandThread* commandThread);

    wxArrayString GetStdOutput();
    wxArrayString GetStdError();

    void SetWarningLevel(int warningLevel);
    void SetIsJobEnd(bool isJobEnd = true);
    void setHasErrors(bool hasErrors = true);
    bool IsJobEnd();
    bool HasErrors();
    bool IsStarted();
    bool IsInExecution();
    void SetIsInExecution(bool isInExecution = true);


    void PrintStatus();

    //bool TryToRunLocally();

    void ConvertErrorsToWarnings();

    virtual wxString GccToMsvcErrorsAndWarnings(const wxString &string, bool convertErrorsToWarnings = false);
    virtual void CheckOutput(bool convertErrorsToWarnings = false);

    bool ExecuteExternalProgram(wxString command, wxString workingDir = wxEmptyString);

};





class CompileCommandDescriptor: public CommandDescriptor
{

protected:
    bool m_distccHasErrors;
    bool m_distccIsEnabled;

    bool m_isUnityBuildFile;

	//bool m_ccacheIsEnabled;

    wxString m_distcc;
	wxString m_ccache;

	//MapLongToString *m_commonHeaders;

public:
    wxString m_inputFile;
    wxString m_dependencyFile;
    wxString m_dependencyCommand;
    wxString m_outputFile;
    wxString m_compileCommand;

    CompileCommandDescriptor(wxString inputFile,
                             wxString dependencyFile,
                             wxString outputFile,
                             wxString workingDir,
                             wxString compileCommand,
                             bool waitToExit = false, /*wait for this job to end before to go to other jobs*/
                             wxString label = wxEmptyString,
                             wxString distcc = wxEmptyString,
							 wxString ccache = wxEmptyString);

    virtual ~CompileCommandDescriptor();

    void SetDistcc(wxString distcc);

	//void SetCcache(wxString distcc);

    void SetUnityBuildFile(bool UBFile);
    bool IsUnityBuildFile();

    virtual bool Execute();

    virtual void CheckOutput(bool convertErrorsToWarnings = false);

    bool DistccHasErrors();
    bool DistccIsEnabled();

	//bool CcacheIsEnabled();

    bool IsOutputObjectValid(wxString fullPathToFile);

    void WriteTimeStampInDependencyFile(const wxString& depFile, wxString srcFile = wxEmptyString);
};





class LinkCommandDescriptor: public CommandDescriptor
{
public:
    //wxString m_targetFile;
    wxString m_linkCommand;
    
    void RemoveObjectFromCommand(wxString objFile);

    void CheckOutput(bool convertErrorsToWarnings);

    LinkCommandDescriptor(wxString linkCommand, wxString targetFile, wxString workingDir, wxString label = wxEmptyString, bool waitForAllPrevJobsToEnd = false);

    virtual ~LinkCommandDescriptor();



    bool Execute();
};


class DsymCommandDescriptor: public CommandDescriptor
{
public:
    //wxString m_targetFile;
    wxString m_dsymCommand;

    void CheckOutput(bool convertErrorsToWarnings);

    DsymCommandDescriptor(wxString dsymCommand, wxString targetFile, wxString workingDir, wxString label = wxEmptyString, bool waitForAllPrevJobsToEnd = true);

    virtual ~DsymCommandDescriptor();

    bool Execute();
};




class StripCommandDescriptor: public CommandDescriptor
{
public:
    
    wxString m_stripCommand;

    void CheckOutput(bool convertErrorsToWarnings);

    StripCommandDescriptor(wxString stripCommand, wxString targetFile, wxString workingDir, wxString label = wxEmptyString, bool waitForAllPrevJobsToEnd = true);

    virtual ~StripCommandDescriptor();

    bool Execute();
};



typedef std::vector<CommandDescriptor*> CommandDescriptorArray;


#endif //COMPILETHREAD_H_INCLUDED
