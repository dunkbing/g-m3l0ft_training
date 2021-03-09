#pragma once

#include "RapidXmlWrapper.h"
#include "ExpressionHandler.h"
#include "Helper.h"
#include "Globals.h"
#include "FileSystem.h"
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include <set>

class XMLParser
{
public:
	class PreprocessData{
	public:
		std::vector<std::string> includes;
		std::vector<std::string> macros;
	};

	class DirData{
	public:
		DirData() : path("") {};
		std::string path;
		std::vector<std::pair<bool, std::string>> fileIgnores;
		std::vector<std::pair<bool, std::string>> folderIgnores;
	};
	class J2AData{
	public:
		struct BuildType
		{
			enum type { Copy, JavaLibrary, AndroidLibrary, AndroidApplication, COUNT };
		private:
			BuildType();
			BuildType(const BuildType&);
			BuildType operator=(const BuildType&);
		};
		struct Directories
		{
			enum type { Java, AIDL, Res, Assets, Libs, JniLibs, Native, COUNT };
		private:
			Directories();
			Directories(const Directories&);
			Directories operator=(const Directories&);
		};
		struct Use
		{
			enum type { Yes, Maybe, No, COUNT };
		private:
			Use();
			Use(const Use&);
			Use operator=(const Use&);
		};
		struct Tag
		{
			enum type { Manifest, Proguard, Java, AIDL, Res, Assets, Libs, JniLibs, Native, COUNT };
		private:
			Tag();
			Tag(const Tag&);
			Tag operator=(const Tag&);
		};
		static const Use::type UseTag[BuildType::COUNT][Tag::COUNT];
		static const bool IsSupported[BuildType::COUNT];

		//no native 'cause were doing java only... (get rid of it)
		//name                  - art - manifest, proguard, java, aidl, res, assets, libs, jniLibs, native
		//copy                  -     -       no,  ??may??,  may,  may, may,    may,  may,     may, ??????
		//java library          - jar -       no,      may,  may,  may,  no,    may,  may,      no,     no
		//android library       - aar -      yes,      may,  may,  may, may,    may,  may,     may, ??????
		//android application   - apk -      yes,      may,  may,  may, may,    may,  may,     may, native

		J2AData();

		std::string name;
		BuildType::type buildType;
		std::string buildArtefact;
		size_t minSdkVersion;
		size_t targetSdkVersion;
		size_t compileSdkVersion;
		size_t buildToolsVersion[3];
		bool useMinSdkVersion;
		bool useTargetSdkVersion;
		//bool useCompileSdkVersion;
		//bool useBuildToolsVersion;
		
		std::string manifestPath;
		std::string proguardPath;
		std::vector<DirData> dirs[Directories::COUNT];
		PreprocessData javaPreprocess;
		std::vector<std::string> jniLibsType;
		
		bool useBuildArtefact;
		
		bool useManifest;
		bool useProguard;
		bool useDirs[Directories::COUNT];
		bool useJavaPreprocess;
	};

	class Java2Android{
	public:
		Java2Android();

		int minSDK;
		int targetSDK;
		int compileSDK;
		int buildTools[3];
		std::string applicationID;
		std::string signatureKeystorePath;
		std::string signatureKeystoreRelativePath;
		std::string signatureKeystorePass;
		std::string signatureKeyName;
		std::string signatureKeyPass;
		//std::string defaultJniType;
		static const std::string unspecifiedJniType;
		bool incrementalBuild;
		std::string lintOptions;
		std::string packageOptions;
		std::string workspaceFolder;
		std::string workspaceLocalManifest;
		std::string workspaceLocalProguard;
		std::string workspaceLocalJava;
		std::string workspaceLocalAIDL;
		std::string workspaceLocalRes;
		std::string workspaceLocalAssets;
		std::string workspaceLocalLibs;
		std::string workspaceLocalJniLibs;
		std::string workspaceManifest;
		std::string workspaceProguard;
		std::string workspaceJava;
		std::string workspaceAIDL;
		std::string workspaceRes;
		std::string workspaceAssets;
		std::string workspaceLibs;
		std::string workspaceJniLibs;
		std::string workspaceRelativeFolder;
		std::string workspaceRelativeManifest;
		std::string workspaceRelativeProguard;
		std::string workspaceRelativeJava;
		std::string workspaceRelativeAIDL;
		std::string workspaceRelativeRes;
		std::string workspaceRelativeAssets;
		std::string workspaceRelativeLibs;
		std::string workspaceRelativeJniLibs;
		std::vector<std::string> flagsPathList;
		std::vector<std::string> includesList;
		std::vector<std::string> macrosList;
		std::vector<std::vector<std::string>> j2aPathsList;
		std::vector<std::size_t> architectureTypeIndices;
		std::map<std::string, std::set<std::string>> excludedJarClassPaths;

		bool useMinSDK;
		bool useTargetSDK;
		bool useCompileSDK;
		bool useBuildTools;
		bool useApplicationID;
		bool useSignatureKeystorePath;
		bool useSignatureKeystorePass;
		bool useSignatureKeyName;
		bool useSignatureKeyPass;
		//bool useDefaultJniType;
		bool useIncrementalBuild;
		bool useLintOptions;
		bool usePackageOptions;
		bool useWorkspaceFolder;
		bool useWorkspaceLocalManifest;
		bool useWorkspaceLocalProguard;
		bool useWorkspaceLocalJava;
		bool useWorkspaceLocalAIDL;
		bool useWorkspaceLocalRes;
		bool useWorkspaceLocalAssets;
		bool useWorkspaceLocalLibs;
		bool useWorkspaceLocalJniLibs;
		bool useFlagsPathList;
		bool useIncludesList;
		bool useMacrosList;
		bool useJ2aPathsList;
		bool useArchitectureTypeIndices;
		bool useExcludedJarClassPaths;
	};
	
private:
	static void toXMLDoc					(const std::string &filename,		std::string &content, rapidxml::xml_document<> &doc,	bool &errorOccurred, std::string &errorMsg);

	//static void processJava2AndroidProject	(const rapidxml::xml_node<> *node,	std::vector<std::string> &j2apaths,						bool &errorOccurred, std::string &errorMsg);
	//static void processJava2AndroidGropup	(const rapidxml::xml_node<> *node,	std::vector<std::string> &j2apaths,						bool &errorOccurred, std::string &errorMsg);
	//static void processJava2Android			(const rapidxml::xml_node<> *node,	std::vector<std::string> &j2apaths,						bool &errorOccurred, std::string &errorMsg);
	static void processJava2AndroidProject	(const rapidxml::xml_node<> *node,	const std::string &folder,	Java2Android &data,										bool &errorOccurred, std::string &errorMsg);
	static void processJava2AndroidGropup	(const rapidxml::xml_node<> *node,	Java2Android &data,										bool &errorOccurred, std::string &errorMsg);
	static void processJava2Android			(const rapidxml::xml_node<> *node,	const std::string &folder,	Java2Android &data,										bool &errorOccurred, std::string &errorMsg);

	static void processJ2ADirs				(const rapidxml::xml_node<> *node,	J2AData::Directories::type dir,		J2AData &data,		bool &errorOccurred, std::string &errorMsg);
	static void processJ2AjniType			(const rapidxml::xml_node<> *node,	std::string jnidefaulttype,			J2AData &data,		bool &errorOccurred, std::string &errorMsg);
	static void processJ2APreprocess		(const rapidxml::xml_node<> *node,										J2AData &data,		bool &errorOccurred, std::string &errorMsg);
	static void processJ2A					(const rapidxml::xml_node<> *node,	std::string jnidefaulttype,			J2AData &data,		bool &errorOccurred, std::string &errorMsg);
public:
	//static void ProcessJava2Android			(const std::string &filename,		std::vector<std::string> &j2apaths,						bool &errorOccurred, std::string &errorMsg);
	//static void ProcessJava2AndroidProject	(const std::string &filename,		std::vector<std::string> &j2apaths,						bool &errorOccurred, std::string &errorMsg);
	//static void ProcessJava2AndroidLibs		(const std::string &filename,		std::vector<std::string> &j2apaths,						bool &errorOccurred, std::string &errorMsg);
	static void ProcessJava2Android			(const std::string &filename,		Java2Android &data,										bool &errorOccurred, std::string &errorMsg);
	static void ProcessJava2AndroidProject	(const std::string &filename,		Java2Android &data,										bool &errorOccurred, std::string &errorMsg);
	static void ProcessJava2AndroidLibs		(const std::string &filename,		Java2Android &data,										bool &errorOccurred, std::string &errorMsg);
	static void ProcessJ2A					(const std::string &filename,		const std::string &jniDefaultType,	J2AData &data,		bool &errorOccurred, std::string &errorMsg);
};
