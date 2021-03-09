#ifndef __PLATFORM_H__
#define __PLATFORM_H__

#include "../PlatformBaseInterface/PlatformBase.h"
#include "GeoLocator.h"

namespace platform
{

	class PlatformImpl : public BaseInterface<PlatformImpl, GeoLocator>
	{
		friend class BaseInterface < PlatformImpl, GeoLocator > ;
	private:
		PlatformImpl() {}
		~PlatformImpl() {}
	};

} // platform

#endif