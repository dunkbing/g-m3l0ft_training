#ifndef SOUND_H_
#define SOUND_H_

#include "Setting.h"
using namespace std;

class Sound : public Setting {
public:
    Sound();
    explicit Sound(const Setting& s);
    ~Sound() override;
    void inputInfo() override;
    void outputInfo() override;
    int getMediaLevel() const;
    int getCallLevel() const;
    int getNaviLevel() const;
    int getNotificationLevel() const;
    void setMediaLevel(int level);
    void setCallLevel(int level);
    void setNaviLevel(int level);
    void setNotificationLevel(int level);
    void printSettingInfo();
private:
    int _mediaLevel;
    int _callLevel;
    int _naviLevel;
    int _notificationLevel;
};
#endif // SOUND_H_

