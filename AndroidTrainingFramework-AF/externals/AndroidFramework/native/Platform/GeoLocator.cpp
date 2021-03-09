#include "GeoLocator.h"

using namespace platform;

GeoLocator::GeoLocator()
{
}

GeoLocator::~GeoLocator()
{
	Disable();
}

	
void GeoLocator::Enable()
{
	acp_utils::api::PackageUtils::EnableUserLocation();
}

void GeoLocator::Disable()
{
	acp_utils::api::PackageUtils::DisableUserLocation();
}

bool GeoLocator::IsEnabled()
{
	acp_utils::helpers::UserLocation userLocation = acp_utils::api::PackageUtils::GetUserLocation();	
	return userLocation.status == acp_utils::helpers::eLocationEnabled;
}


acp_utils::helpers::EUserLocationStatus GeoLocator::GetStatus()
{
	acp_utils::helpers::UserLocation userLocation = acp_utils::api::PackageUtils::GetUserLocation();
	return userLocation.status;
}

GeoLocator::Location GeoLocator::GetLocation()
{
	acp_utils::helpers::UserLocation userLocation = acp_utils::api::PackageUtils::GetUserLocation();

	GeoLocator::Location location;	
	location.latitude = userLocation.latitude;
	location.longitude = userLocation.longitude;
	location.altitude = 0.0;
	location.accuracy = userLocation.accuracy;

	return location;
}

bool GeoLocator::HasValidCoordinates()
{
	return IsEnabled();		
}
