#ifndef GENERAL_H
#define GENERAL_H
#include <string>
#include <vector>
#include "Setting.h"

class General final : public Setting {
public:
    General();
    explicit General(const Setting& s);
    ~General() override;
    void inputInfo() override;
    void outputInfo() override;
    std::string getLanguage() const;
    std::string getTimeZone() const;
    void setTimeZone(const std::string& timeZone);
    void setLanguage(const std::string& language);
    static void loadTimeZones();
    static void loadLanguages();

    static int selectTimeZone();
    static int selectLanguage();
private:
    struct CommonInfo
    {
        std::string number;
        std::string name;
    };

    std::string _timeZone;
    std::string _language;
    static std::vector<CommonInfo> _timeZones;
    static std::vector<CommonInfo> _languages;
};

#endif // GENERAL_H
