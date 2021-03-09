#ifndef VCSLN_H_INCLUDED
#define VCSLN_H_INCLUDED


#include <wx/wx.h>
#include <wx/string.h>

#include "VcProject.h"

#include <vector>



typedef std::vector<VcProject*> VcProjectList;

class VcSln
{
    /**
     * This is the file name to the VisualStudio solution file (*.sln) .
     */
    wxString m_fileName;

public:
    /**
     * std::vector with all the projects from solution that was added to sln2gcc.xml .
     * A project from sln that you want to be added to this list (m_prj) must be added
     * to sln2gcc.xml with a <Project> </Project> node.
     */
    VcProjectList m_prj;

public:
    VcSln();
    VcSln(const wxString &fileName);
    virtual ~VcSln(void);

    bool Parse();

    wxString GetFileName();

    void SetFileName(const wxString& fileName);

    int GetProjectsCount();
    VcProject* GetProject(unsigned int index);
    VcProject* GetProject(const wxString& name);

    void RemoveProjectFromList(VcProject* prj);
    VcProjectList::iterator RemoveProjectFromList(VcProjectList::iterator it);
};

#endif //SLNPARSER_H_INCLUDED