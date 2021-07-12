#ifndef SETTING_H_
#define SETTING_H_

#include <string>


class Setting {
public:
    Setting();
    virtual ~Setting();
    virtual void inputInfo();
    virtual void outputInfo();

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
private:
    std::string _carName;
    std::string _personalKey; 	// Chuoi 8 ky tu so
    std::string _email;			// email format abc@xyz.com
    int _odo;
    int _serviceRemind;
};
#endif /* SETTING_H_ */

