#ifndef GLOBALS_H
#define GLOBALS_H

#include <string>
#include <vector>
#include <map>

class Global
{
public:

//static std::string filePathJava2Android;
//static std::string filePathDefaults;
//static std::string filePathConfig;
//static std::string filePathEnvironment;
//static std::string filePathFlags;

//static std::string folderPathWorkspace;
//static std::string filePathManifest;
//static std::string filePathProguard;
//static std::string folderPathJava;
//static std::string folderPathAIDL;
//static std::string folderPathRes;
//static std::string folderPathAssets;
//static std::string folderPathLibs;
//static std::string folderPathJniLibs;
//static std::string folderPathNative;
//
//static std::string localFilePathManifest;
//static std::string localFilePathProguard;
//static std::string localFolderPathJava;
//static std::string localFolderPathAIDL;
//static std::string localFolderPathRes;
//static std::string localFolderPathAssets;
//static std::string localFolderPathLibs;
//static std::string localFolderPathJniLibs;
//static std::string localFolderPathNative;

//static std::string filePathExternals;


static std::string varJ2ARoot; //src workspace

static std::string varTmpFolderPathWorkspace;
static std::string varTmpFilePathManifest;
static std::string varTmpFilePathProguard;
static std::string varTmpFolderPathJava;
static std::string varTmpFolderPathAIDL;
static std::string varTmpFolderPathRes;
static std::string varTmpFolderPathAssets;
static std::string varTmpFolderPathLibs;
static std::string varTmpFolderPathJniLibs;
//static std::string varTmpFolderPathNative;

static std::string varTmpLocalFilePathManifest;
static std::string varTmpLocalFilePathProguard;
static std::string varTmpLocalFolderPathJava;
static std::string varTmpLocalFolderPathAIDL;
static std::string varTmpLocalFolderPathRes;
static std::string varTmpLocalFolderPathAssets;
static std::string varTmpLocalFolderPathLibs;
static std::string varTmpLocalFolderPathJniLibs;
//static std::string varTmpLocalFolderPathNative;

static std::string varRootDirectory;

static std::string varCopyDestinationFolder;
static std::string varCheckedExtension;
static std::string varJNIType;
static std::string varPreprocessApp;
static std::string varZipApp;
static std::string varInitialPreprocessParameters;
static std::string varPreprocessParameters;
static std::string varPreprocessScript;

//static std::string varJava2AndroidFolder;
//static std::string varJNILibsDefaultType;
static const size_t varJNITypesCount;
static const std::string varJNITypes[];
static const std::string varJNITypesBoolNames[];
static const std::string constLocalFolderPathSrcTemp;
static std::string globalFolderPathSrcTemp;
static const std::string constLocalFolderPathGradleTemplate;
static const std::string constLocalFolderPathAndroidStudioTemplate;
static const std::string constLocalFolderPathAndroidStudioProject;
static std::string varLocalFolderPathAndroidStudioProject;

static std::vector<std::string> libs;
static std::vector<std::string> foldersToDelete;
static std::map<std::string, bool> boolFlags;

static bool alwaysRebuild;
static bool printFlags;

//static std::string generalPreprocessIncludes;
//static std::string generalPreprocessMacros;
static std::string pathOffset;

//static int compileSdkVersion;
//static std::string buildToolsVersion;
//static int targetSdKVersion;
//static int minSdkVersion;
//
//static std::string packageName;
//
//static std::string keystorePath;
//static std::string keystorePass;
//static std::string keyName;
//static std::string keyPass;
//
//static std::string packagingOptions;

static std::vector<std::string> java2androidPathList;
static std::vector<std::string> java2androidFolderList;

static std::vector<std::size_t> architectureIndexList;

//static XMLParser::Java2Android projectData;

static std::vector<std::string> externalsAAR;

static bool showOverwriteWarning;

static bool hideWarnings;

};

#endif
