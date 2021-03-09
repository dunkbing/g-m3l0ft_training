#include "CompileThread.h"


#include <wx/sysopt.h>
#include <wx/tokenzr.h>
#include <wx/regex.h>
#include <wx/textfile.h>
#include "wx/except.h"

#include "MemMgr.h"


#include <list>
extern std::list<wxString> allErrors;


void Handler(const wxString& file, int line, const wxString& func, const wxString& cond, const wxString& msg);

///==========================================================================================
ExternalCommandThread::ExternalCommandThread(CommandDescriptor* cmdDesc) : wxThread()
{
	m_cmdDesc = cmdDesc;
	m_cmdDesc->SetThread(this);

	m_mutexProtect = new wxMutex(wxMUTEX_RECURSIVE);

}

ExternalCommandThread::~ExternalCommandThread()
{
	delete m_mutexProtect;
	m_mutexProtect = NULL;
}


void ExternalCommandThread::OnExit()
{
	//wxPrintf(m_cmdDesc->m_name);
	//PRINTF(COLOR_RED, wxT(" OnExit!\n"));
}

void* ExternalCommandThread::Entry()
{
	wxLogNull logNo;

	m_cmdDesc->SetIsInExecution(true);

	bool hasErrors = false;
	wxTRY
	{
		hasErrors = m_cmdDesc->Execute();
	}
	wxCATCH_ALL(hasErrors = true;)


		m_mutexProtect->Lock();

	m_cmdDesc->setHasErrors(hasErrors);
	m_cmdDesc->SetIsJobEnd();

	m_mutexProtect->Unlock();

	m_cmdDesc->SetIsInExecution(false);
	return NULL;
}
///==========================================================================================




///==========================================================================================
ProcessesArray CommandDescriptor::s_processesArray;
int CommandDescriptor::s_commandsExecuted = 0;
int CommandDescriptor::s_commandsTotalToExecute = 0;

CommandDescriptor::CommandDescriptor(wxString target, wxString workingDir, wxString label, bool waitToExit, bool waitForAllPrevJobsToEnd)
{
	m_mutex = new wxMutex(wxMUTEX_RECURSIVE);

	m_type = COMMAND_TYPE_NOT_DEFINED;

	m_warningLevel = 0;

	m_label = label;
	m_target = target;

	m_isJobEnd = false;
	m_inExecution = false;
	m_hasErrors = false;
	//m_tryToRunLocally = false;
	m_started = false;

	m_command = wxEmptyString;

	m_workingDir = workingDir;

	//PRINTF(COLOR_RED, wxT("CommandDescriptor m_target=") + m_target + wxT("\n"));
	//PRINTF(COLOR_RED, wxT("CommandDescriptor m_label=") + m_label + wxT("\n"));
	//PRINTF(COLOR_RED, wxT("CommandDescriptor m_workingDir=") + m_workingDir + wxT("\n"));

	m_process = NEW wxProcess;

	m_waitUntilJobEnds = waitToExit;

	m_waitForAllPrevJobsToEnd = waitForAllPrevJobsToEnd;

	CommandDescriptor::s_processesArray.push_back(m_process);
}


CommandDescriptor::~CommandDescriptor()
{
	//PRINTF(COLOR_YELLOW, wxString(wxT("\nCommandDescriptor::~CommandDescriptor ")) + this->m_label + wxT("\n"));

	m_mutex->Lock();


	///do not call delete on a detached thread. The thread will be deleted by himself after
	///finish the job
	///delete m_thread;
	///m_thread = NULL;

	if (m_process)
	{
		m_process->Detach();
	}

	delete m_process;
	m_process = NULL;

	m_mutex->Lock();

	delete m_mutex;
	m_mutex = 0;

}

void CommandDescriptor::PrintStatus()
{
	wxMutexLocker scope(*m_mutex);

	PRINTF(COLOR_YELLOW, wxT("\nCommandDescriptor::PrintStatus:\n"));
	PRINTF(COLOR_YELLOW, wxString(wxT("m_label=%s\n")), m_label);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_type=%d\n")), m_type);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_warningLevel=%d\n")), m_warningLevel);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_target=%s\n")), m_target);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_isJobEnd=%d\n")), m_isJobEnd);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_inExecution=%d\n")), m_inExecution);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_hasErrors=%d\n")), m_hasErrors);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_started=%d\n")), m_started);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_command=%s\n")), m_command);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_workingDir=%s\n")), m_workingDir);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_process=%d\n")), m_process ? TRUE : FALSE);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_thread=%d\n")), m_thread ? TRUE : FALSE);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_waitUntilJobEnds=%d\n")), m_waitUntilJobEnds);
	PRINTF(COLOR_YELLOW, wxString(wxT("m_waitForAllPrevJobsToEnd=%d\n")), m_waitForAllPrevJobsToEnd);
}

void CommandDescriptor::KillAllProcesses()
{
	PRINTF(COLOR_YELLOW, wxT("\nKill all processes: "));

	for (unsigned int i = 0; i < s_processesArray.size(); i++)
	{
		wxProcess* proc = s_processesArray[i];
		int killResponse = wxKILL_OK;
		if (proc)
		{
			long pid = proc->GetPid();
			if (wxProcess::Exists(pid))
			{
				sln2gccApp::KillProcessByPID(pid);
			}
		}
	}


	PRINTF(COLOR_YELLOW, wxT("OK.\n"));
}
/*
void CommandDescriptor::DeleteProcesses()
{
wxMutexLocker scope(*m_mutex);

for (unsigned int i=0;i<s_processesArray.size(); i++ )
{
wxProcess* proc = s_processesArray[i];

if(proc)
{
delete proc;
}
}
}
*/

int CommandDescriptor::GetWarningLevel()
{
	wxMutexLocker scope(*m_mutex);

	return m_warningLevel;
}

void CommandDescriptor::SetWarningLevel(int warningLevel)
{
	wxMutexLocker scope(*m_mutex);

	m_warningLevel = warningLevel;
}


int CommandDescriptor::GetType()
{
	wxMutexLocker scope(*m_mutex);

	return m_type;
}

bool CommandDescriptor::Execute()
{
	wxMutexLocker scope(*m_mutex);

	m_startTime = wxDateTime::UNow();
	m_started = true;
	return false;
}

bool CommandDescriptor::ExecuteExternalProgram(wxString command, wxString workingDir)
{
	m_command = command;

	if (!wxFileName(workingDir).DirExists())
	{
		ERROR_PRINTF(wxString(wxT("file")), wxString(wxT("Working directory does not exists!(")) + workingDir + wxT(")\n"));
		return true;
	}
	//PRINTF(COLOR_RED, wxT("3 m_workingDir=") + m_workingDir + wxT("\n"));

	//wxPrintf(wxT("Executing command :\n[%s]\n"), command);


	/*
	struct wxExecuteEnv
	{
	wxString cwd;               // If empty, CWD is not changed.
	wxEnvVariableHashMap env;   // If empty, environment is unchanged.
	};
	*/
	wxExecuteEnv ex;
	ex.cwd = workingDir;

	wxDISABLE_DEBUG_SUPPORT();
	wxSetAssertHandler(Handler);

	long returnCode = wxExecute(command, m_output, m_errors, 1 /* wxEXEC_SYNC */, m_process, &ex);

	return (returnCode != 0); ///true means has errors
}

bool CommandDescriptor::IsJobEnd()
{
	wxMutexLocker scope(*m_mutex);

	return m_isJobEnd;
}

void CommandDescriptor::SetIsJobEnd(bool isJobEnd)
{
	wxMutexLocker scope(*m_mutex);

	m_isJobEnd = isJobEnd;
}

void CommandDescriptor::setHasErrors(bool hasErrors)
{
	wxMutexLocker scope(*m_mutex);

	m_hasErrors = hasErrors;
}

wxString CommandDescriptor::GetTarget()
{
	wxMutexLocker scope(*m_mutex);

	return m_target;
}

wxString CommandDescriptor::GetLabel()
{
	wxMutexLocker scope(*m_mutex);

	return m_label;
}


bool CommandDescriptor::HasErrors()
{
	wxMutexLocker scope(*m_mutex);

	return m_hasErrors;
}

//bool CommandDescriptor::TryToRunLocally()
//{
//    return m_tryToRunLocally;
//}



bool CommandDescriptor::IsStarted()
{
	wxMutexLocker scope(*m_mutex);

	return m_started;
}

bool CommandDescriptor::IsInExecution()
{
	wxMutexLocker scope(*m_mutex);

	return m_inExecution;
}

void CommandDescriptor::SetIsInExecution(bool isInExecution)
{
	wxMutexLocker scope(*m_mutex);

	m_inExecution = isInExecution;
}

/** Used to convert some special key characters (for the Regex Expressions) to some
 * other things that does not break the rules.
 * @param dest is a string that will return the converted string.
 * @param source is the input string.
 */
void SanitizeString(wxString &dest, const wxString& source)
{
	dest.clear();
	if (source.empty()) return;

	for (unsigned int i = 0; i < source.size(); ++i)
	{
		if (source[i] == '%')
		{
			dest = dest + wxT("%%");
		}
		else
			dest = dest + source[i];
	}
}

wxString CommandDescriptor::GccToMsvcErrorsAndWarnings(const wxString &errString, bool convertErrorsToWarnings)
{
	//wxMutexLocker scope(*m_mutex);

	wxString newErrString; //= errString;
	SanitizeString(newErrString, errString);
	newErrString.Trim().Trim(false);



	//wxPrintf(wxT(">>>>>>>newErrString data ")+ newErrString +wxT("<<<<<<<<<<<<<"));
	//wxPrintf ()

	wxRegEx regularExp1(wxT(":[0-9]:[0-9],"));
	wxRegEx regularExp2(wxT(":[0-9]+:"));
	wxRegEx regularExp3(wxT(":[0-9]*,"));

	//c:\program files\microsoft visual studio 9.0\vc\include\codeanalysis\sourceannotations.h(19) : error C2054: expected '(' to follow 'dfg'

	if ((newErrString.Find(wxT("In file included from ")) != wxNOT_FOUND)
		|| (newErrString.Find(wxT("from ")) != wxNOT_FOUND)
		)
	{
		wxString tempErrString = wxEmptyString;

		wxStringTokenizer* tkn = NEW wxStringTokenizer(newErrString, wxT("\n\r"));

		while (tkn->HasMoreTokens())
		{
			wxString newTok = tkn->GetNextToken();
			if (newTok != wxEmptyString)
			{

				newTok.Trim().Trim(false);
				wxString key = wxEmptyString;
				wxString lineNumber = wxEmptyString;



				if (regularExp1.Matches(newTok))
				{
					size_t start = 0;
					size_t len = 0;
					if (regularExp1.GetMatch(&start, &len))
					{
						key = newTok.Mid(start, len);

						lineNumber = key;
						lineNumber.Replace(wxT(":"), wxT("("), false);
						lineNumber.Replace(wxT(":"), wxT("):"), false);
						//lineNumber.Replace(wxT(","), wxT(")"));

						//wxPrintf(wxT("==================1BEFORE=") + newTok + wxT("\n"));

						newTok.Replace(key, lineNumber);

						int startOfFilename = newTok.LastIndexOf(' ');
						int lengthOfFilename = start - startOfFilename;
						wxString fileWithError = newTok.Mid(startOfFilename, lengthOfFilename).Trim().Trim(false);

						//wxPrintf(wxT("==================1AFTER=") + newTok + wxT("\n"));

						if (wxFileName(fileWithError).IsRelative(wxPATH_NATIVE))
						{
							//wxPrintf(wxT("==================1FILEWITHERROR=") + fileWithError + wxT("\n"));

							//wxPrintf(wxT("==================1FILEWITHERROR=") + this->m_workingDir + fileWithError + wxT("\n"));

							newTok.Replace(fileWithError, this->m_workingDir + fileWithError);

						}


					}
				}
				else
					if (regularExp2.Matches(newTok))
					{
						size_t start = 0;
						size_t len = 0;
						if (regularExp2.GetMatch(&start, &len))
						{
							key = newTok.Mid(start, len);

							lineNumber = key;
							lineNumber.Replace(wxT(":"), wxT(" "));
							lineNumber.Trim().Trim(false);

							//wxPrintf(wxT("==================1BEFORE=") + newTok + wxT("\n"));

							newTok.Replace(key, wxT("(") + lineNumber + wxT("):"));


							int startOfFilename = newTok.LastIndexOf(' ');
							int lengthOfFilename = start - startOfFilename;
							wxString fileWithError = newTok.Mid(startOfFilename, lengthOfFilename).Trim().Trim(false);

							//wxPrintf(wxT("==================2AFTER=") + newTok + wxT("\n"));

							if (wxFileName(fileWithError).IsRelative(wxPATH_NATIVE))
							{
								//wxPrintf(wxT("==================2FILEWITHERROR=") + fileWithError + wxT("\n"));

								//wxPrintf(wxT("==================2FILEWITHERROR=") + this->m_workingDir + fileWithError + wxT("\n"));

								newTok.Replace(fileWithError, this->m_workingDir + fileWithError);

							}
						}
					}
					else
						if (regularExp3.Matches(newTok))
						{
							size_t start = 0;
							size_t len = 0;
							if (regularExp3.GetMatch(&start, &len))
							{
								key = newTok.Mid(start, len);

								lineNumber = key;
								lineNumber.Replace(wxT(":"), wxT("("), false);
								lineNumber.Replace(wxT(","), wxT("):"));

								//wxPrintf(wxT("==================3BEFORE=") + newTok + wxT("\n"));

								newTok.Replace(key, lineNumber);

								int startOfFilename = newTok.LastIndexOf(' ');
								int lengthOfFilename = start - startOfFilename;
								wxString fileWithError = newTok.Mid(startOfFilename, lengthOfFilename).Trim().Trim(false);

								//wxPrintf(wxT("==================3AFTER=") + newTok + wxT("\n"));

								if (wxFileName(fileWithError).IsRelative(wxPATH_NATIVE))
								{
									//wxPrintf(wxT("==================3FILEWITHERROR=") + fileWithError + wxT("\n"));

									//wxPrintf(wxT("==================3FILEWITHERROR=") + this->m_workingDir + fileWithError + wxT("\n"));

									newTok.Replace(fileWithError, this->m_workingDir + fileWithError);

								}


							}
						}

				if (key != wxEmptyString && lineNumber != wxEmptyString)
				{
					int pos = newTok.LastIndexOf(' ');
					wxString msg = newTok.Mid(0, pos);
					wxString newString = newTok.Mid(pos + 1);
					newString.Trim().Trim(false);

					tempErrString += newString + wxT(" : ") + msg;
				}
			}
		}

		delete tkn;

		newErrString = tempErrString;
	}
	else
		if (newErrString.Find(wxT(": In function")) != wxNOT_FOUND)
		{
			if (convertErrorsToWarnings)
				newErrString.Replace(wxT(": In function"), wxT(": warning: In function"));
			else
				newErrString.Replace(wxT(": In function"), wxT(": error: In function"));
		}
		else
			// linking error
			if (newErrString.Find(wxT(": undefined reference to")) != wxNOT_FOUND)
			{
				if (convertErrorsToWarnings)
					newErrString.Replace(wxT(": undefined reference to"), wxT(": warning: undefined reference to"));
				else
					newErrString.Replace(wxT(": undefined reference to"), wxT(": error: undefined reference to"));

				/**
				 * Linking error sample
				 *
				 *  c:/android-ndk-r8d/toolchains/arm-linux-androideabi-4.6/prebuilt/windows/bin/../lib/gcc/arm-linux-androideabi/4.6/../../../../arm-linux-androideabi/bin/ld.exe:SNSManager.o:
				 *  in function initSNSManager():D:/GLLegacy2012Working/trunk/Externals/GLLegacy/src/GLLegacy/LibImpl/SNSManager.cpp:30: error: error:
				 *  undefined reference to 'sociallib::ClientSNSInterface::initSNS(sociallib::ClientSNSEnum)'
				 *
				 */

				int objectPos = newErrString.Find(wxT(".o"));

				wxString leftOfObjectName = newErrString.Mid(0, objectPos + 2);

				wxString rightOfObjectName = newErrString.Mid(objectPos + 2);

				// object file name (*.o)
				wxString objectName = leftOfObjectName.AfterLast(':').Trim().Trim(false);

				// absolute file path containing error line as well
				wxString pathErr = rightOfObjectName.Mid(rightOfObjectName.Find(wxT(":/")) - 1);

				// function in which the error occurred
				wxString inFunctionName = rightOfObjectName.Mid(1, rightOfObjectName.Find(wxT(":/")) - 3).Trim().Trim(false);

				// reformat error line
				if (regularExp2.Matches(pathErr))
				{
					size_t start = 0;
					size_t len = 0;

					if (regularExp2.GetMatch(&start, &len))
					{
						wxString key = pathErr.Mid(start, len);

						wxString lineNumber = key;
						lineNumber.Replace(wxT(":"), wxT(" "));
						lineNumber.Trim().Trim(false);

						pathErr.Replace(key, wxT("(") + lineNumber + wxT(") : "));

					}
				}

				// undefined reference name
				wxString undefinedRef = pathErr.Mid(pathErr.Find(wxT("undefined")));

				pathErr = pathErr.Mid(0, pathErr.Find(wxT("undefined")));

				// error keyword
				wxString errorKey = pathErr.BeforeLast(':').AfterLast(':').Trim().Trim(false) + wxT(":");

				pathErr = pathErr.BeforeLast(':').BeforeLast(':').BeforeLast(':').Trim().Trim(false);

				// re-format error according to visual studio standard
				// path(line) : info
				newErrString = pathErr + wxT(" : ") + errorKey + wxT(" (") + objectName + wxT(")") + inFunctionName + wxT(" ") + undefinedRef;
			}
			else
			{
				if (regularExp2.Matches(newErrString))
				{
					size_t start = 0;
					size_t len = 0;
					if (regularExp2.GetMatch(&start, &len))
					{
						wxString key = newErrString.Mid(start, len);

						wxString lineNumber = key;
						lineNumber.Replace(wxT(":"), wxT(" "));
						lineNumber.Trim().Trim(false);

						//wxPrintf(wxT("==================5BEFORE=") + newErrString + wxT("\n"));

						newErrString.Replace(key, wxT("(") + lineNumber + wxT("):"));


						int startOfFilename = 0;
						int lengthOfFilename = newErrString.IndexOf('(');
						wxString fileWithError = newErrString.Mid(startOfFilename, lengthOfFilename).Trim().Trim(false);

						//wxPrintf(wxT("==================5AFTER=") + newErrString + wxT("\n"));

						if (wxFileName(fileWithError).IsRelative(wxPATH_NATIVE))
						{
							//wxPrintf(wxT("==================5FILEWITHERROR=") + fileWithError + wxT("\n"));

							//wxPrintf(wxT("==================5FILEWITHERROR=") + this->m_workingDir + fileWithError + wxT("\n"));

							newErrString.Replace(fileWithError, this->m_workingDir + fileWithError);

						}

					}
				}
			}




	return newErrString;
}

void CommandDescriptor::ConvertErrorsToWarnings()
{
	//wxMutexLocker scope(*m_mutex);

	unsigned int outCount = m_output.GetCount();
	for (unsigned int i = 0; i < outCount; i++)
	{
		m_output[i].Replace(wxT("error:"), wxT("warning:"));
		m_output[i].Replace(wxT("Error:"), wxT("warning:"));
		m_output[i].Replace(wxT("ERROR:"), wxT("warning:"));
		m_output[i].Replace(wxT("ERROR:"), wxT("warning:"));
	}

	unsigned int errCount = m_errors.GetCount();
	for (unsigned int i = 0; i < errCount; i++)
	{
		m_errors[i].Replace(wxT("error:"), wxT("warning:"));
		m_errors[i].Replace(wxT("Error:"), wxT("warning:"));
		m_errors[i].Replace(wxT("ERROR:"), wxT("warning:"));
		m_errors[i].Replace(wxT("ERROR:"), wxT("warning:"));
		m_output.Add(m_errors[i]);
	}
	m_errors.Clear();
}

void CommandDescriptor::CheckOutput(bool convertErrorsToWarnings)
{
	wxMutexLocker scope(*m_mutex);

	if (convertErrorsToWarnings) ConvertErrorsToWarnings();

	unsigned int outCount = m_output.GetCount();
	for (unsigned int i = 0; i < outCount; i++)
	{
		if (sln2gccApp::GetEnableVerbose())
			wxPrintf(wxT("output[%d]=") + m_output[i] + wxT("\n"), i);

		if (sln2gccApp::GetEnableGCC2MSVC())
			m_output[i] = GccToMsvcErrorsAndWarnings(m_output[i], convertErrorsToWarnings);


		if ((this->m_hasErrors) && ((m_output[i].Find(wxT("error:")) != wxNOT_FOUND)
			|| (m_output[i].Find(wxT("ERROR:")) != wxNOT_FOUND)))
		{
			if (sln2gccApp::GetEnableVerbose()) PRINTF(COLOR_RED, wxT("Command: ") + this->m_command + wxT("\n"));

			PRINTF(COLOR_RED, m_output[i] + wxT("\n"));

			sln2gccApp::SetErrorCode(1);
		}
		else
			if ((!this->m_hasErrors) && ((m_output[i].Find(wxT("error:")) != wxNOT_FOUND)
				|| (m_output[i].Find(wxT("ERROR:")) != wxNOT_FOUND)
				|| (m_output[i].Find(wxT("WARNING:")) != wxNOT_FOUND)
				|| (m_output[i].Find(wxT("warning:")) != wxNOT_FOUND)
				|| (m_output[i].Find(wxT("Warning:")) != wxNOT_FOUND)))
			{
				if (sln2gccApp::GetEnableGCC2MSVC())
				{
					m_output[i].Replace(wxT("error:"), wxT("warning:"));
					PRINTF(COLOR_YELLOW, m_output[i] + wxT("\n"));
				}
				else
				{
					if (sln2gccApp::GetEnableVerbose()) PRINTF(COLOR_RED, wxT("Command: ") + this->m_command + wxT("\n"));
					PRINTF(COLOR_RED, m_output[i] + wxT("\n"));
					sln2gccApp::SetErrorCode(1);
				}
			}
			else
				if ((m_output[i].Find(wxT("warning:")) != wxNOT_FOUND)
					|| (m_output[i].Find(wxT("WARNING:")) != wxNOT_FOUND)
					|| (m_output[i].Find(wxT("Warning:")) != wxNOT_FOUND))
				{
					PRINTF(COLOR_YELLOW, m_output[i] + wxT("\n"));
				}
				else
				{
					PRINTF(COLOR_WHITE, m_output[i] + wxT("\n"));
				}
	}


	unsigned int errCount = m_errors.GetCount();

	if (errCount > 0)
	{
		wxString localError = "";

		for (unsigned int i = 0; i < errCount; i++)
			localError += m_errors[i] + "\n";

		localError.Replace("compilation terminated.", "", true);
		allErrors.push_back(localError);
	}

	for (unsigned int i = 0; i < errCount; i++)
	{
		if (sln2gccApp::GetEnableVerbose())
			wxPrintf(wxT("error[%d]=") + m_errors[i] + wxT("\n"), i);


		if (sln2gccApp::GetEnableGCC2MSVC())
			m_errors[i] = GccToMsvcErrorsAndWarnings(m_errors[i], convertErrorsToWarnings);


		if (this->m_hasErrors)
		{
			if (sln2gccApp::GetEnableVerbose()) PRINTF(COLOR_RED, wxT("Command: ") + this->m_command + wxT("\n"));

			PRINTF(COLOR_RED, m_errors[i] + wxT("\n"));

			sln2gccApp::SetErrorCode(1);
		}
		else
			if ((m_errors[i].Find(wxT("warning:")) != wxNOT_FOUND)
				|| (m_errors[i].Find(wxT("WARNING:")) != wxNOT_FOUND)
				|| (m_errors[i].Find(wxT("Warning:")) != wxNOT_FOUND))
			{
				PRINTF(COLOR_YELLOW, m_errors[i] + wxT("\n"));
			}
			else
			{
				if ((!this->m_hasErrors) && ((m_errors[i].Find(wxT("error:")) != wxNOT_FOUND)
					|| (m_errors[i].Find(wxT("ERROR:")) != wxNOT_FOUND)))
				{
					if (sln2gccApp::GetEnableGCC2MSVC())
					{
						m_errors[i].Replace(wxT("error:"), wxT("warning:"));
						m_errors[i].Replace(wxT("ERROR:"), wxT("warning:"));
						PRINTF(COLOR_YELLOW, m_errors[i] + wxT("\n"));
					}
					else
					{
						if (sln2gccApp::GetEnableVerbose()) PRINTF(COLOR_RED, wxT("Command: ") + this->m_command + wxT("\n"));
						PRINTF(COLOR_RED, m_errors[i] + wxT("\n"));
						sln2gccApp::SetErrorCode(1);
					}
				}
				else
				{
					PRINTF(COLOR_WHITE, m_errors[i] + wxT("\n"));
				}
			}

		///manage the distcc fails!
		///reduce the number of jobs for distcc and remove the failed host from list!
		int distccJobs = sln2gccApp::GetDISTCCJobs();
		if (distccJobs >= 4)
		{
			if ((m_errors[i].Find(wxT("running locally instead")) != wxNOT_FOUND)
				|| (m_errors[i].Find(wxT("failed with exit code")) != wxNOT_FOUND))
			{

				wxArrayString hosts = sln2gccApp::GetDISTCCHosts();
				int size = hosts.size();
				for (int j = 0; j < size; j++)
				{
					if (m_errors[i].Find(hosts[j]) != wxNOT_FOUND)
					{
						sln2gccApp::SetDISTCCJobs(distccJobs - distccJobs / size);
						WARNING_PRINTF(wxString(wxT("")), wxString(wxT("Decrease the jobs for distcc (s_distccJobs=%d) because host '%s' failed!\n")), sln2gccApp::GetDISTCCJobs(), hosts[j]);
						WARNING_PRINTF(wxString(wxT("")), wxString(wxT("Remove host '%s' from distcc hosts list because failed!\n")), hosts[j]);
						sln2gccApp::RemoveDISTCCHosts(hosts[j]);
						break;
					}
				}
			}
		}
	}
}


wxArrayString CommandDescriptor::GetStdOutput()
{
	wxMutexLocker scope(*m_mutex);

	return m_output;
}
wxArrayString CommandDescriptor::GetStdError()
{
	wxMutexLocker scope(*m_mutex);

	return m_errors;
}

wxDateTime CommandDescriptor::GetStartTime()
{
	wxMutexLocker scope(*m_mutex);

	return m_startTime;
}

ExternalCommandThread* CommandDescriptor::GetThread()
{
	wxMutexLocker scope(*m_mutex);

	return m_thread;
}

wxProcess* CommandDescriptor::GetProcess()
{
	wxMutexLocker scope(*m_mutex);

	return m_process;
}

void CommandDescriptor::SetThread(ExternalCommandThread* commandThread)
{
	wxMutexLocker scope(*m_mutex);

	m_thread = commandThread;
}
///==========================================================================================

///==========================================================================================

CompileCommandDescriptor::CompileCommandDescriptor(wxString inputFile,
	wxString dependencyFile,
	wxString outputFile,
	wxString workingDir,
	wxString compileCommand,
	bool waitToExit,
	wxString label,
	wxString distcc,
	wxString ccache)
	:CommandDescriptor(inputFile, workingDir, label, waitToExit)
{
	m_type = COMMAND_TYPE_COMPILE;

	m_distccIsEnabled = false;
	m_distccHasErrors = false;


	//m_ccacheIsEnabled = false;

	m_inputFile = inputFile;
	m_dependencyFile = dependencyFile;
	//m_dependencyCommand = dependencyCommand;
	m_outputFile = outputFile;
	m_compileCommand = compileCommand;

	//m_commonHeaders = commonHeaders;
	
	SetDistcc(distcc);
	//SetCcache(ccache);
	SetUnityBuildFile(false);
}

CompileCommandDescriptor::~CompileCommandDescriptor()
{
	wxMutexLocker scope(*m_mutex);


}

bool CompileCommandDescriptor::Execute()
{
	//m_mutex->Lock();

	CommandDescriptor::Execute();

	if (this->m_process->m_markThisToBeKilled)
		return false;

	m_distccHasErrors = false;
	m_hasErrors = false;

	bool runOnLocalComputer = false;

	wxString outFileABS = this->m_outputFile;
	wxString depFileABS = this->m_dependencyFile;

	wxString compileCommandForLocalComputer = m_compileCommand;

	//m_mutex->Unlock();

	if (m_distccIsEnabled)
	{
		//m_mutex->Lock();

		wxString compileCommandForHost = wxEmptyString;

		wxString programExe = m_compileCommand.BeforeFirst(' ');
		wxString arguments = m_compileCommand.AfterFirst(' ');
		wxString pathToProgramExe = sln2gccApp::GetToolChainPath();

		wxFileName programExeFN(programExe);
		if (programExeFN.IsAbsolute())
		{
			pathToProgramExe = programExeFN.GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR);
			programExe = programExeFN.GetFullName();
		}

		if (!sln2gccApp::GetDistccUseFullPath())
			pathToProgramExe = wxEmptyString;

		compileCommandForHost = m_distcc + wxT(" ") + pathToProgramExe + programExe + wxT(" ") + arguments;

		if (wxFileName::FileExists(outFileABS)) wxRemoveFile(outFileABS);
		if (wxFileName::FileExists(depFileABS)) wxRemoveFile(depFileABS);
		//wxThread::Sleep(50);

		//m_mutex->Unlock();

		if (this->m_process->m_markThisToBeKilled)
			return false;

		m_distccHasErrors = ExecuteExternalProgram(compileCommandForHost, m_workingDir);

		if (this->m_process->m_markThisToBeKilled)
			return false;

		//m_mutex->Lock();

		if ((!m_distccHasErrors))
		{
			if (!IsOutputObjectValid(outFileABS))
			{
				m_distccHasErrors = true;
				//wxString errorMessage = wxEmptyString;
				//errorMessage += wxT("error: The file \"") + this->m_outputFile + wxT("\" DOES NOT EXISTS! \n");
				//m_output.Add(errorMessage);
			}
		}

		//#ifdef _DEBUG 
		//PRINTF(COLOR_RED, wxString(wxT("m_distccHasErrors=%d  ")) + this->m_outputFile + wxT(" \n"), m_distccHasErrors);
		//#endif

		if (m_distccHasErrors)
		{
			//if distcc returns some errors the file will be compiled locally
			//m_hasErrors = ExecuteExternalProgram(compileCommandForLocalComputer);
			runOnLocalComputer = true;
			m_distccIsEnabled = false;
		}
		else
		{
			// If the distcc does not return any error i want to check if the file was compiled on the target or on the host.
			// distcc can do this and i can see this from output.
			unsigned int errorsCount = m_errors.GetCount();
			for (unsigned int i = 0; i < errorsCount; i++)
			{
				if (this->m_process->m_markThisToBeKilled)
					return false;

				if (m_errors[i].Find(wxT(", running locally instead")) != wxNOT_FOUND) ///this means that the file was compiled locally by the DISTCC
				{
					m_distccIsEnabled = false;
					break;
				}
			}

		}

		//m_mutex->Unlock();
	}
	else
	{
		runOnLocalComputer = true;
	}


	if (runOnLocalComputer)
	{
		if (this->m_process->m_markThisToBeKilled)
			return false;


		//m_mutex->Lock();
		m_distccHasErrors = false;//reset this 

		if (wxFileName::FileExists(outFileABS)) wxRemoveFile(outFileABS);
		if (wxFileName::FileExists(depFileABS)) wxRemoveFile(depFileABS);

		//m_mutex->Unlock();

		m_hasErrors = ExecuteExternalProgram(compileCommandForLocalComputer, m_workingDir);
	}

	if (this->m_process->m_markThisToBeKilled)
		return false;


	//m_mutex->Lock();
	if (!m_distccHasErrors && !m_hasErrors)
	{
		if (!IsOutputObjectValid(outFileABS))
		{
			if (runOnLocalComputer)
				m_hasErrors = true;
			else
				m_distccHasErrors = true;
		}
	}

	if (this->m_process->m_markThisToBeKilled)
		return false;

	if (!m_distccHasErrors && !m_hasErrors)
	{
		WriteTimeStampInDependencyFile(depFileABS, m_inputFile);
	}
	else
	{
		if (wxFileName::FileExists(outFileABS)) wxRemoveFile(outFileABS);
		if (wxFileName::FileExists(depFileABS)) wxRemoveFile(depFileABS);
	}

	//m_mutex->Unlock();

	return m_hasErrors;
}


void CompileCommandDescriptor::SetDistcc(wxString distcc)
{
	wxMutexLocker scope(*m_mutex);

	m_distccIsEnabled = false;
	if (distcc != wxEmptyString)
	{
		if (wxFileName::FileExists(distcc))
		{
			m_distcc = distcc;
			m_distccIsEnabled = true;
		}
		else
		{
			m_distcc = wxEmptyString;
		}
	}
	else
	{
		m_distcc = wxEmptyString;
	}
}


void CompileCommandDescriptor::SetUnityBuildFile(bool UBFile)
{
	wxMutexLocker scope(*m_mutex);

	m_isUnityBuildFile = UBFile;
}

bool CompileCommandDescriptor::IsUnityBuildFile()
{
	wxMutexLocker scope(*m_mutex);

	return m_isUnityBuildFile;
}

bool CompileCommandDescriptor::DistccHasErrors()
{
	wxMutexLocker scope(*m_mutex);

	return m_distccHasErrors;
}

bool CompileCommandDescriptor::DistccIsEnabled()
{
	wxMutexLocker scope(*m_mutex);

	return m_distccIsEnabled;
}



void CompileCommandDescriptor::CheckOutput(bool convertErrorsToWarnings)
{
	wxMutexLocker scope(*m_mutex);

	s_commandsExecuted++;
	wxPrintf(wxT("(%d/%d) "), s_commandsExecuted, s_commandsTotalToExecute);
	wxPrintf(GetLabel());

	wxTimeSpan elapsedTime = wxDateTime::UNow().Subtract(GetStartTime());

	long minutes = 0;
	long seconds = elapsedTime.GetSeconds().ToLong();
	if (seconds > 60)
	{
		minutes = seconds / 60;
		seconds = seconds % 60;
	}

	if (HasErrors())
	{
		if (this->m_isUnityBuildFile)
		{
			PRINTF(COLOR_YELLOW, wxT(" UnityBuild failed!"));
			if (minutes > 0){ PRINTF(COLOR_WHITE, wxT(" (%dm%ds)"), minutes, seconds); }
			else { PRINTF(COLOR_WHITE, wxT(" (%ds)"), seconds); }
			//PRINTF(COLOR_WHITE, wxT(" (%ds)"), elapsedTime.GetSeconds());
		}


		wxPrintf(wxT("\n"));
	}
	else
	{

		PRINTF(COLOR_GREEN, wxT(" DONE!"));
		if (minutes > 0){ PRINTF(COLOR_WHITE, wxT(" (%dm%ds)"), minutes, seconds); }
		else { PRINTF(COLOR_WHITE, wxT(" (%ds)"), seconds); }
		if (DistccIsEnabled() && !DistccHasErrors()) PRINTF(COLOR_BLUE, wxT(" using distcc."));
		PRINTF(COLOR_WHITE, wxT("\n"));

		//PRINTF(COLOR_GREEN, wxT(" DONE!"));
		//PRINTF(COLOR_WHITE, wxT(" (%ds)"), elapsedTime.GetSeconds());
		//if(DistccIsEnabled() && !DistccHasErrors()) PRINTF(COLOR_BLUE, wxT(" using distcc."));
		//PRINTF(COLOR_WHITE, wxT("\n"));
	}

	CommandDescriptor::CheckOutput(convertErrorsToWarnings);
}

bool CompileCommandDescriptor::IsOutputObjectValid(wxString fullPathToFile)
{
	//wxMutexLocker scope(*m_mutex);

	bool returnCode = true;

	if (wxFileName::FileExists(fullPathToFile))
	{
		wxFile obj;
		if (!obj.Open(fullPathToFile))
		{
			wxString errorMessage = wxEmptyString;
			errorMessage += wxT("error: The file \"") + this->m_outputFile + wxT("\" could not be opened!\n");
			m_output.Add(errorMessage);

			returnCode = false;
		}
		else
		{
			wxFileOffset fo = obj.SeekEnd();
			if (fo == wxInvalidOffset || fo == 0)
			{
				wxString errorMessage = wxEmptyString;
				errorMessage += wxT("error: The size of file \"") + this->m_outputFile + wxT("\" is invalid! \n");
				m_output.Add(errorMessage);

				returnCode = false;
			}
			//PRINTF(COLOR_GREEN, wxString(wxT("size of \""))  + this->m_outputFile + wxT("\" is %d ! \n"),  (int)fo);

			obj.Close();
		}
	}
	else
	{
		wxString errorMessage = wxEmptyString;
		errorMessage += wxT("error: The file \"") + this->m_outputFile + wxT("\" DOES NOT EXISTS! \n");
		m_output.Add(errorMessage);

		returnCode = false;
	}

	return returnCode;
}

void CompileCommandDescriptor::WriteTimeStampInDependencyFile(const wxString& depFile, wxString srcFile)
{
	//wxMutexLocker scope(*m_mutex);

	///recreate the timeStamp file
	//wxPrintf(wxT("call WriteTimeStampInDependencyFile ") + file + wxT("\n"));
	MapLongToString timeStampMap;

	bool dependencyFileMustExists = true;

	if (srcFile != wxEmptyString)
	{
		//wxMutexLocker scope(*m_mutex);

		wxFileName srcFileName(srcFile);
		if (srcFileName.GetExt() == wxT("s")  ///Raw assembler source (no cpp pass).
			|| srcFileName.GetExt() == wxT("i")) ///Raw C source (no cpp pass). 
		{
			///because .s .i type of files must not pass the preprocessor the dependency file will not be created
			dependencyFileMustExists = false;

			if (wxFileName::FileExists(srcFile))
			{
				long fileTimeStamp = srcFileName.GetModificationTime().GetAsDOS();

				//if(fileTimeStamp == 0)
				//    WARNING_PRINTF(wxString(wxT("file")), wxT("------------fileTimeStamp=0 for ") + srcFile + wxT("\n"));

				timeStampMap[srcFile] = fileTimeStamp;
			}
			else
			{
				//WARNING_PRINTF(wxString(wxT("file")), wxT("------------FILE DOES NOT EXIST: ") + srcFile + wxT("\n"));
			}

		}
	}


	//if srcFile it is empty it will be detected from the dependency file
	if (dependencyFileMustExists)
	{
		//wxMutexLocker scope(*m_mutex);

		if (wxFileName::FileExists(depFile))
		{
			wxTextFile dependencyFile(depFile);//this is the standard dependency file generated by the gcc
			if (dependencyFile.Open())
			{
				//Read line by line the standard dependency file
				int lines = dependencyFile.GetLineCount();
				for (int k = 0; k < lines; k++)
				{
					if (this->m_process->m_markThisToBeKilled)
						return;


					wxString line = dependencyFile[k];
					//wxString str000 = line;
					line.Trim().Trim(false);
					//wxString str001 = line;

					if (line.GetChar(line.Len() - 1) == wxChar(wxT('\\')))
					{
						//remove the '\' from the end of the line
						line = line.Mid(0, line.Len() - 1);
					}

					line.Trim().Trim(false);


					wxStringTokenizer* tkn = NEW wxStringTokenizer(line, wxT(" "));
					while (tkn->HasMoreTokens())
					{
						wxString newTok = tkn->GetNextToken();
						newTok.Trim().Trim(false);

						if (newTok != wxEmptyString)
						{
							wxString str002 = newTok;


							if (newTok.GetChar(newTok.Len() - 1) == wxChar(wxT(':')))
							{
								//this is the first file and must be the Object file (aka *.o)
								//the path to this file can be absolute or relative
								//remove the ':' from the end of the line
								newTok = newTok.Mid(0, newTok.Len() - 1);
							}

							wxString fn = newTok;


							//if(k == 0) ///first line it is special
							//{
							//    fn = line.Mid(line.IndexOf(':')+1);
							//}
							//else
							//{
							//    fn = line;
							//}
							//wxString str00 = fn;

							fn.Trim().Trim(false);

							//wxString str01 = fn;

							if (fn == wxEmptyString) continue;


							// added here to remove the / or \ from the beginning of a relative path because
							// the normalizing function failed with it.
							if ((fn.IndexOf('\\') == 0) || (fn.IndexOf('/') == 0)) fn = fn.Mid(1);
							//wxString str1 = fn;

							wxFileName fileName(fn);

							bool normalizeFailed = false;
							normalizeFailed = !fileName.Normalize();

							//wxString str2 = fileName.GetFullPath();
							fileName.SetVolume(fileName.GetVolume().Upper());
							//wxString str3 = fileName.GetFullPath();
							fn = fileName.GetFullPath();

							//if(normalizeFailed)
							//{
							//    wxPrintf(str1 + wxT(" | ") + fn + wxT("\n"));
							//}


							if ((srcFile == wxEmptyString) && (m_inputFile == fn))
							{
								srcFile = fn;
							}


							if (wxFileName::FileExists(fn))
							{
								long fileTimeStamp = fileName.GetModificationTime().GetAsDOS();

								//if(fileTimeStamp == 0)
								//    WARNING_PRINTF(wxString(wxT("file")), wxT("=============fileTimeStamp=0 for ") + fn + wxT("\n"));

								timeStampMap[fn] = fileTimeStamp;
							}
							else
							{
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============FILE DOES NOT EXIST: ") + fn + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str000: ") + str000 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str001: ") + str001 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str002: ") + str002 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str00: ") + str00 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str01: ") + str01 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str1: ") + str1 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str2: ") + str2 + wxT("\n"));
								//WARNING_PRINTF(wxString(wxT("file")), wxT("=============str3: ") + str3 + wxT("\n"));
							}
						}
					}

					delete tkn;

				}

				dependencyFile.Close();
			}
			else
			{
				WARNING_PRINTF(wxString(wxT("file")), wxT("CANNOT OPEN file ") + depFile + wxT("\n"));
				return;
			}
		}
		else
		{
			WARNING_PRINTF(wxString(wxT("f")), wxT("DOES NOT EXIST file ") + depFile + wxT("\n"));
			return;
		}
	}

	wxYield();
	wxSleep(2);

	if (this->m_process->m_markThisToBeKilled)
		return;


	::wxRenameFile(depFile, depFile + wxT("_std"), true);

	///write NEW timeStamps
	wxFile timeStamp(depFile, wxFile::write);

	bool openOK = timeStamp.Create(depFile, true);

	if (openOK)
	{
		//wxMutexLocker scope(*m_mutex);

		wxString str = wxEmptyString;

		str = m_compileCommand + wxT("\n");
		if (!timeStamp.Write(str))
		{
			WARNING_PRINTF(wxString(wxT("file")), wxT("Cannot write string '") + str + wxT("' in the file '") + depFile + wxT("'\n"));
		}

		wxString strTimeStamp = wxEmptyString;
		if (srcFile != wxEmptyString)
		{
			strTimeStamp.Printf(wxT("%ld"), timeStampMap[srcFile]);
			str = strTimeStamp + wxT(";") + srcFile + wxT("\n");
			if (!timeStamp.Write(str))
			{
				WARNING_PRINTF(wxString(wxT("file")), wxT("Cannot write string '") + str + wxT("' in the file '") + depFile + wxT("'\n"));
			}

		}


		MapLongToString::iterator it;
		for (it = timeStampMap.begin(); it != timeStampMap.end(); ++it)
		{
			if (this->m_process->m_markThisToBeKilled)
				return;


			wxString file = it->first;
			long timeS = it->second;

			if (file != srcFile)
			{
				strTimeStamp.Printf(wxT("%ld"), timeS);
				str = strTimeStamp + wxT(";") + file + wxT("\n");
				if (!timeStamp.Write(str))
				{
					WARNING_PRINTF(wxString(wxT("file")), wxT("Cannot write string '") + str + wxT("' in the file '") + file + wxT("'\n"));
				}

				//if(m_commonHeaders) (*m_commonHeaders)[file]++;
			}

		}

		timeStamp.Flush();
		timeStamp.Close();
	}
	else
	{
		WARNING_PRINTF(wxString(wxT("file")), wxT("Cannot create file ") + depFile + wxT("\n"));
		return;
	}
	///--------------------
	///--------------------
}
///==========================================================================================



///==========================================================================================

LinkCommandDescriptor::LinkCommandDescriptor(wxString linkCommand, wxString targetFile, wxString workingDir, wxString label, bool waitForAllPrevJobsToEnd)
	:CommandDescriptor(targetFile, workingDir, label, false, waitForAllPrevJobsToEnd)
{
	//PRINTF(COLOR_YELLOW, wxT("LinkCommandDescriptor target:") + label + wxT("\n"));
	//PRINTF(COLOR_YELLOW, wxT("LinkCommandDescriptor working Dir:") + workingDir + wxT("\n"));

	//m_targetFile = targetFile;
	m_linkCommand = linkCommand;

	m_type = COMMAND_TYPE_LINK;
}

LinkCommandDescriptor::~LinkCommandDescriptor()
{
	wxMutexLocker scope(*m_mutex);

}


bool LinkCommandDescriptor::Execute()
{
	CommandDescriptor::Execute();

	m_hasErrors = false;

	//PRINTF(COLOR_RED, wxT("2 m_workingDir=") + m_workingDir + wxT("\n"));
	m_hasErrors = ExecuteExternalProgram(m_linkCommand, m_workingDir);

	return m_hasErrors;
}

void LinkCommandDescriptor::RemoveObjectFromCommand(wxString objFile)
{
	wxMutexLocker scope(*m_mutex);

	//wxPrintf(objFile + wxT(" RemoveObjectFromCommand \n"));
	m_linkCommand.Replace(objFile, wxT(" "));

	while (m_linkCommand.Replace(wxT("  "), wxT(" ")));
}


void LinkCommandDescriptor::CheckOutput(bool convertErrorsToWarnings)
{
	wxMutexLocker scope(*m_mutex);

	s_commandsExecuted++;
	wxPrintf(wxT("(%d/%d) "), s_commandsExecuted, s_commandsTotalToExecute);
	wxPrintf(GetLabel());

	wxTimeSpan elapsedTime = wxDateTime::UNow().Subtract(GetStartTime());

	if (!HasErrors())
	{

		long minutes = 0;
		long seconds = elapsedTime.GetSeconds().ToLong();
		if (seconds > 60)
		{
			minutes = seconds / 60;
			seconds = seconds % 60;
		}

		PRINTF(COLOR_GREEN, wxT(" DONE!"));
		if (minutes > 0){ PRINTF(COLOR_WHITE, wxT(" (%dm%ds)"), minutes, seconds); }
		else { PRINTF(COLOR_WHITE, wxT(" (%ds)"), seconds); }
		PRINTF(COLOR_WHITE, wxT("\n"));

		//PRINTF(COLOR_GREEN, wxT(" DONE!"));
		//PRINTF(COLOR_WHITE, wxT(" (%ds)"), elapsedTime.GetSeconds());
		//PRINTF(COLOR_WHITE, wxT("\n"));
	}

	CommandDescriptor::CheckOutput(convertErrorsToWarnings);
}




///==========================================================================================

DsymCommandDescriptor::DsymCommandDescriptor(wxString dsymCommand, wxString targetFile, wxString workingDir, wxString label, bool waitForAllPrevJobsToEnd)
	:CommandDescriptor(targetFile, workingDir, label, false, waitForAllPrevJobsToEnd)
{
	//m_targetFile = targetFile;
	m_dsymCommand = dsymCommand;

	m_type = COMMAND_TYPE_DSYM;
}

DsymCommandDescriptor::~DsymCommandDescriptor()
{
	wxMutexLocker scope(*m_mutex);

}


bool DsymCommandDescriptor::Execute()
{
	CommandDescriptor::Execute();

	m_hasErrors = false;

	m_hasErrors = ExecuteExternalProgram(m_dsymCommand, m_workingDir);

	return m_hasErrors;
}


void DsymCommandDescriptor::CheckOutput(bool convertErrorsToWarnings)
{
	wxMutexLocker scope(*m_mutex);

	s_commandsExecuted++;
	wxPrintf(wxT("(%d/%d) "), s_commandsExecuted, s_commandsTotalToExecute);
	wxPrintf(GetLabel());

	wxTimeSpan elapsedTime = wxDateTime::UNow().Subtract(GetStartTime());

	if (!HasErrors())
	{
		long minutes = 0;
		long seconds = elapsedTime.GetSeconds().ToLong();
		if (seconds > 60)
		{
			minutes = seconds / 60;
			seconds = seconds % 60;
		}

		PRINTF(COLOR_GREEN, wxT(" DONE!"));
		if (minutes > 0){ PRINTF(COLOR_WHITE, wxT(" (%dm%ds)"), minutes, seconds); }
		else { PRINTF(COLOR_WHITE, wxT(" (%ds)"), seconds); }
		PRINTF(COLOR_WHITE, wxT("\n"));

		//PRINTF(COLOR_GREEN, wxT(" DONE!"));
		//PRINTF(COLOR_WHITE, wxT(" (%ds)"), elapsedTime.GetSeconds());
		//PRINTF(COLOR_WHITE, wxT("\n"));
	}

	CommandDescriptor::CheckOutput(convertErrorsToWarnings);
}





///==========================================================================================

StripCommandDescriptor::StripCommandDescriptor(wxString stripCommand, wxString targetFile, wxString workingDir, wxString label, bool waitForAllPrevJobsToEnd)
	:CommandDescriptor(targetFile, workingDir, label, false, waitForAllPrevJobsToEnd)
{
	//m_targetFile = targetFile;
	m_stripCommand = stripCommand;

	m_type = COMMAND_TYPE_STRIP;
}

StripCommandDescriptor::~StripCommandDescriptor()
{
	wxMutexLocker scope(*m_mutex);

}


bool StripCommandDescriptor::Execute()
{
	CommandDescriptor::Execute();

	m_hasErrors = false;

	/// make a copy of original files aka the file with symbols
	if (wxCopyFile(m_workingDir + wxT("/") + m_target, m_workingDir + wxT("/") + m_target + wxT("_with_dsym")))
	{
		m_hasErrors = ExecuteExternalProgram(m_stripCommand, m_workingDir);
	}
	else
	{
		m_output.Add(wxT("\nCannot create a copy of '") + m_target + wxT("' in folder ") + m_workingDir + wxT(" with name '") + m_target + wxT("_with_dsym' !"));
		m_hasErrors = true;
	}

	return m_hasErrors;
}


void StripCommandDescriptor::CheckOutput(bool convertErrorsToWarnings)
{
	wxMutexLocker scope(*m_mutex);

	s_commandsExecuted++;
	wxPrintf(wxT("(%d/%d) "), s_commandsExecuted, s_commandsTotalToExecute);
	wxPrintf(GetLabel());

	wxTimeSpan elapsedTime = wxDateTime::UNow().Subtract(GetStartTime());

	if (!HasErrors())
	{
		long minutes = 0;
		long seconds = elapsedTime.GetSeconds().ToLong();
		if (seconds > 60)
		{
			minutes = seconds / 60;
			seconds = seconds % 60;
		}

		PRINTF(COLOR_GREEN, wxT(" DONE!"));
		if (minutes > 0){ PRINTF(COLOR_WHITE, wxT(" (%dm%ds)"), minutes, seconds); }
		else { PRINTF(COLOR_WHITE, wxT(" (%ds)"), seconds); }
		PRINTF(COLOR_WHITE, wxT("\n"));
	}

	CommandDescriptor::CheckOutput(convertErrorsToWarnings);
}