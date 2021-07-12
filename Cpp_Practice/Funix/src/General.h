#ifndef GENERAL_H
#define GENERAL_H
#include "Setting.h"
#include <string>

#include "CommonInfo.h"

class General final : public Setting {
public:
    General();
    ~General() override;
    void inputInfo() override;
    void outputInfo() override;
    string getLanguage() const;
    string getTimeZone() const;
    void setTimeZone(const string& timeZone);
    void setLanguage(const string& language);
    static void loadTimeZones();
    static void loadLanguages();

    static int selectTimeZone();
    static int selectLanguage();
private:
    string _timeZone;
    string _language;
    static vector<CommonInfo> _timeZones;
    static vector<CommonInfo> _languages;
};

#endif // GENERAL_H
