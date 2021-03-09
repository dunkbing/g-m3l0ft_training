#ifndef __GEOLOCATOR_H__
#define __GEOLOCATOR_H__

#include "../PlatformBaseInterface/PlatformBase.h"
#include "acp_utils.h"

namespace platform
{	
	class GeoLocator : public GeoLocatorBase<GeoLocator>
	{
		friend class GeoLocatorBase < GeoLocator > ;
	private:
		GeoLocator();
		~GeoLocator();
	public:
		void Enable() override;
		void Disable() override;
		bool IsEnabled() override;
		Location GetLocation() override;
		bool HasValidCoordinates() override;
		
		acp_utils::helpers::EUserLocationStatus GetStatus();

	};

} // namespace platform

#endif //__GEOLOCATOR_H__