-------------------------------------------------------------------------------
--
-------------------------------------------------------------------------------

include "../tools/premake/common/"

-------------------------------------------------------------------------------
--
-------------------------------------------------------------------------------

solution "AndroidCorePackage"
	
  	--startproject "AndroidCorePackage"

	addCommonConfig()

-------------------------------------------------------------------------------
--
-------------------------------------------------------------------------------
				
project "AndroidCorePackage"

	files 
	{ 
		"../native/**.h",
		"../native/**.cpp",		
        "../native/**.c",		
	}
    
    excludes "../native/GLiveHTML5/**"
    excludes "../native/DirectIGP/**"
    excludes "../native/Loader/**"
    excludes "../native/AResLoader/**"
    excludes "../native/GameUtils/_internal/UnitTests/**"
	
	includedirs 
	{ 
        "../java/",
        "../native/",
        "../native/GameUtils/",
        "$(EXTERNALS_FOLDER)/acp_config"
	}
    
    buildoptions
    {
        --TODO: does not seem to have any effect - modify android_s2g generator
        "-fpermissive -fPIC -shared -fno-rtti"
    }

	kind "StaticLib"
	
	uuid "E12A7625-6EDF-774B-8758-2EB0EA8384F2"

	targetname( "AndroidCorePackage" )	

	targetdir ("../lib/" .. GetPathFromPlatform())
						
-------------------------------------------------------------------------------
--
-------------------------------------------------------------------------------