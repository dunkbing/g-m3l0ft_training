#include "VcSln.h"

#include <wx/textfile.h>
#include <wx/tokenzr.h>
#include <wx/filename.h>

#include "MemMgr.h"


VcSln::VcSln()
{
	SetFileName(wxEmptyString);
}

VcSln::VcSln(const wxString &fileName)
{
	SetFileName(fileName);
}

VcSln::~VcSln(void)
{
	int size = m_prj.size();
	for (int i = 0; i < size; i++)
	{
		if (m_prj[i] == NULL) continue;

		delete m_prj[i];
	}

	m_prj.clear();
}

wxString VcSln::GetFileName()
{
	return m_fileName;
}
void VcSln::SetFileName(const wxString& fileName)
{
	m_fileName = fileName;
}

int VcSln::GetProjectsCount()
{
	return m_prj.size();
}
VcProject* VcSln::GetProject(unsigned int index)
{
	if ((index < 0) || (index >= m_prj.size())) return NULL;

	return m_prj[index];
}

VcProject* VcSln::GetProject(const wxString& name)
{
	int size = m_prj.size();
	for (int i = 0; i < size; i++)
	{
		if (m_prj[i] == NULL) continue;

		if (m_prj[i]->GetName() == name) return m_prj[i];
	}

	return NULL;
}

void VcSln::RemoveProjectFromList(VcProject* prj)
{
	int size = m_prj.size();
	for (int i = 0; i < size; i++)
	{
		if (m_prj[i] == prj)
		{
			m_prj.erase(m_prj.begin() + i);
			break;
		}
	}
}

VcProjectList::iterator VcSln::RemoveProjectFromList(VcProjectList::iterator it)
{
	return m_prj.erase(it);
}


bool VcSln::Parse()
{
	//wxPrintf(wxT("Parse: ") + m_fileName + wxT("\n"));

	wxTextFile file(m_fileName);

	if (!file.Open()) return false;

	for (unsigned int i = 0; i < file.GetLineCount(); i++)
	{
		//wxPrintf(wxT("line%d=") + file[i] + wxT("\n"), i);

		if (file[i].Find(wxT("Project(\"{")) == 0)
		{
			wxString temp = file[i].Mid(file[i].IndexOf('=') + 1);

			wxString prjName = wxEmptyString;
			wxString prjPath = wxEmptyString;

			wxStringTokenizer tkz(temp, wxT(","));

			if (tkz.HasMoreTokens())
			{
				prjName = tkz.GetNextToken();
				prjName.Replace(wxT("\""), wxT(" "));
				prjName.Trim(true).Trim(false);
				//wxPrintf(prjName + wxT(" "));
			}
			else
			{
				return false;
			}
			if (tkz.HasMoreTokens())
			{
				prjPath = tkz.GetNextToken();
				prjPath.Replace(wxT("\""), wxT(" "));
				prjPath.Trim(true).Trim(false);
				//wxPrintf(prjPath + wxT("\n"));
			}
			else
			{
				return false;
			}

			if ((prjName != wxEmptyString) && (prjPath != wxEmptyString))
			{
				if (wxFileName(prjPath).IsRelative(wxPATH_NATIVE))
				{
					prjPath = wxFileName(this->m_fileName).GetPath(wxPATH_GET_VOLUME | wxPATH_GET_SEPARATOR) + prjPath;
				}

				VcProject* prj = NEW VcProject(prjName, prjPath);

				if (sln2gccApp::GetEnableVerbose())
					wxPrintf(wxString(wxT("Detected '")) + prjName + wxT("' ") + prjPath + wxT("\n"));

				this->m_prj.push_back(prj);
				//PRINTF(COLOR_RED, _T("ADDED------------------- ") + prj->GetName() + _T("\n"));
			}

		}
	}

	return true;
}