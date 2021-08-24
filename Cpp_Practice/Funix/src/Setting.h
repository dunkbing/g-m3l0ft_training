#ifndef SETTING_H_
#define SETTING_H_

#include <string>
#include <set>


class Setting {
public:
    Setting();
    Setting(const Setting& s);
    virtual ~Setting();
    // for inputing information
    virtual void inputInfo(const std::set<std::string>& keys);
    // for outputing information
    virtual void outputInfo();

    // getters and setters
    std::string getCarName() const;
    std::string getPersonalKey() const;
    int getOdo () const;
    int getServiceRemind() const;
    std::string getEmail() const;
    void setCarName(const std::string& name);
    void setPersonalKey(std::string key);
    void setEmail(const std::string& email);
    void setOdo(int odo);
    void setServiceRemind(int service);
    friend bool operator == (const Setting& s1, const Setting& s2);
    friend bool operator < (const Setting& s1, const Setting& s2);
    friend bool operator > (const Setting& s1, const Setting& s2);
private:
    std::string _carName;
    std::string _personalKey; 	// Chuoi 8 ky tu so
    std::string _email;			// email format abc@xyz.com
    int _odo;
    int _serviceRemind;
};
#endif /* SETTING_H_ */

