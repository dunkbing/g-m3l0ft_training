#include "Globals.h"

//std::string Global::filePathJava2Android;
//std::string Global::filePathDefaults;
//std::string Global::filePathConfig;
//std::string Global::filePathEnvironment;
//std::string Global::filePathFlags;

//std::string Global::folderPathWorkspace;
//std::string Global::filePathManifest;
//std::string Global::filePathProguard;
//std::string Global::folderPathJava;
//std::string Global::folderPathAIDL;
//std::string Global::folderPathRes;
//std::string Global::folderPathAssets;
//std::string Global::folderPathLibs;
//std::string Global::folderPathJniLibs;
//std::string Global::folderPathNative;
//
//std::string Global::localFilePathManifest;
//std::string Global::localFilePathProguard;
//std::string Global::localFolderPathJava;
//std::string Global::localFolderPathAIDL;
//std::string Global::localFolderPathRes;
//std::string Global::localFolderPathAssets;
//std::string Global::localFolderPathLibs;
//std::string Global::localFolderPathJniLibs;
//std::string Global::localFolderPathNative;

//std::string Global::filePathExternals;


std::string Global::varJ2ARoot; //src workspace

std::string Global::varTmpFolderPathWorkspace;
std::string Global::varTmpFilePathManifest;
std::string Global::varTmpFilePathProguard;
std::string Global::varTmpFolderPathJava;
std::string Global::varTmpFolderPathAIDL;
std::string Global::varTmpFolderPathRes;
std::string Global::varTmpFolderPathAssets;
std::string Global::varTmpFolderPathLibs;
std::string Global::varTmpFolderPathJniLibs;
//std::string Global::varTmpFolderPathNative;

std::string Global::varTmpLocalFilePathManifest;
std::string Global::varTmpLocalFilePathProguard;
std::string Global::varTmpLocalFolderPathJava;
std::string Global::varTmpLocalFolderPathAIDL;
std::string Global::varTmpLocalFolderPathRes;
std::string Global::varTmpLocalFolderPathAssets;
std::string Global::varTmpLocalFolderPathLibs;
std::string Global::varTmpLocalFolderPathJniLibs;
//std::string Global::varTmpLocalFolderPathNative;

std::string Global::varRootDirectory;

std::string Global::varCopyDestinationFolder;
std::string Global::varCheckedExtension;
std::string Global::varJNIType;
std::string Global::varPreprocessApp;
std::string Global::varZipApp;
std::string Global::varInitialPreprocessParameters;
std::string Global::varPreprocessParameters;
std::string Global::varPreprocessScript;

//std::string Global::varJava2AndroidFolder;
//std::string Global::varJNILibsDefaultType;
const size_t Global::varJNITypesCount = 8;
const std::string Global::varJNITypes[] = { "armeabi", "armeabi-v7a", "mips", "x86", "armeabi-x64", "armeabi-v7a-x64", "mips-x64", "x86-x64" };
const std::string Global::varJNITypesBoolNames[] = { "use_armeabi", "use_armeabi_v7a", "use_mips", "use_x86", "use_armeabi_x64", "use_armeabi_v7a_x64", "use_mips_x64", "use_x86_x64" };
const std::string Global::constLocalFolderPathSrcTemp = "java2android-temp";
std::string Global::globalFolderPathSrcTemp;
const std::string Global::constLocalFolderPathGradleTemplate = "gradleTemplate";
const std::string Global::constLocalFolderPathAndroidStudioTemplate = "androidStudiProjectTemplate";
const std::string Global::constLocalFolderPathAndroidStudioProject = "android_studio_project";
std::string Global::varLocalFolderPathAndroidStudioProject;

std::vector<std::string> Global::libs;
std::vector<std::string> Global::foldersToDelete;
std::map<std::string, bool> Global::boolFlags;

bool Global::alwaysRebuild;
bool Global::printFlags;

//std::string Global::generalPreprocessIncludes;
//std::string Global::generalPreprocessMacros;
std::string Global::pathOffset;

//int Global::compileSdkVersion;
//std::string Global::buildToolsVersion;
//int Global::targetSdKVersion;
//int Global::minSdkVersion;
//
//std::string Global::packageName;
//
//std::string Global::keystorePath;
//std::string Global::keystorePass;
//std::string Global::keyName;
//std::string Global::keyPass;
//
//std::string Global::packagingOptions;

std::vector<std::string> Global::java2androidPathList;
std::vector<std::string> Global::java2androidFolderList;

std::vector<std::size_t> Global::architectureIndexList;

//XMLParser::Java2Android Global::projectData;

std::vector<std::string> Global::externalsAAR;

bool Global::showOverwriteWarning;

bool Global::hideWarnings;

