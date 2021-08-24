#ifndef DISPLAY_H_
#define DISPLAY_H_

#include "Setting.h"
#include <iostream>
#include <string>
using namespace std;

class Display : public Setting {
public:
    Display();
    explicit Display(const Setting& s);
    ~Display() override;
    void inputInfo(const std::set<std::string>& keys) override;
    void outputInfo() override;
    static void outputInfoLabel();
    
    // getters and setters function.
    int getLightLevel() const;
    int getScreenLightLevel() const;
    int getTaploLightLevel() const;
    void setLightLevel(int lightLevel);
    void setScreenLightLevel(int lightLevel);
    void setTaploLightLevel(int lightLevel);
private:
    int _lightLevel;
    int _screenLightLevel;
    int _taploLightLevel;
};

#endif /* DISPLAY_H_ */

