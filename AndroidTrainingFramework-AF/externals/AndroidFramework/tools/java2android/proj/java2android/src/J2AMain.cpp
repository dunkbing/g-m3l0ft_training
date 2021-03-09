#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include <algorithm>
#include <locale>

#include "Globals.h"
#include "FileSystem.h"
#include "ExpressionHandler.h"
#include "XMLParser.h"
#include "Helper.h"

//#define DEBUG
#ifdef DEBUG
	#define dbg(x) printDBG((x))
#else
	#define dbg(x)
#endif

////#define MY_ASD "asd"
////#define MY_QWE "qwe"
////#define MY_ZXC (MY_ASD MY_QWE)
////
////char* asd = "asd" "qwe";
////std::string qwe = "asd" "qwe";
////std::string zxc = MY_ZXC;

XMLParser::Java2Android projectData;

void applyRercursively(const std::string &origpath, std::string folderpath, void(*filefunc)(const std::string &path, const std::string &localpath), void(*folderfunc)(const std::string &path, const std::string &localpath) = nullptr, const std::vector<std::pair<bool, std::string>> &ignoreFolders = std::vector<std::pair<bool, std::string>>(), const std::vector<std::pair<bool, std::string>> &ignoreFiles = std::vector<std::pair<bool, std::string>>())
{
	std::vector<std::string> folders;
	std::vector<std::string> files;

	std::string folderpathDir = folderpath == "" ? "" : folderpath + "\\";

	FileSystem::FileFinder finder(origpath + "\\" + folderpathDir + "*");
	while (finder.isValid())
	{
		std::string name = finder.getCurrentFile();
		if (name != "." && name != "..")
		{
			bool ignored = false;
			if (FileSystem::IsFolder(origpath + "\\" + folderpathDir + name))
			{
				std::string relativePath = folderpathDir + name;
				for (std::vector<std::pair<bool, std::string>>::const_iterator i = ignoreFolders.begin(); i != ignoreFolders.end(); i++)
				{
					bool isNameAndNotPath = i->first;
					if (isNameAndNotPath && name == i->second || !isNameAndNotPath && relativePath == i->second)
					{
						ignored = true;
						break;
					}
				}
				if (!ignored)
				{
					folders.push_back(name);
				}
			}
			else
			{
				if (Global::varCheckedExtension != "" && toLower(FileSystem::FileExtension(name)) != Global::varCheckedExtension)
				{
					ignored = true;
				}
				if (!ignored)
				{
					std::string relativePath = folderpathDir + name;
					for (std::vector<std::pair<bool, std::string>>::const_iterator i = ignoreFiles.begin(); i != ignoreFiles.end(); i++)
					{
						bool isNameAndNotPath = i->first;
						if (isNameAndNotPath && name == i->second || !isNameAndNotPath && relativePath == i->second)
						{
							ignored = true;
							break;
						}
					}
				}
				if (!ignored)
				{
					files.push_back(name);
				}
			}
		}
		finder.findNextFile();
	}

	for (std::vector<std::string>::iterator i = files.begin(); i != files.end(); i++)
	{
		if (filefunc != nullptr)
		{
			filefunc(origpath, folderpathDir + *i);
		}
	}
	for (std::vector<std::string>::iterator i = folders.begin(); i != folders.end(); i++)
	{
		if (folderfunc != nullptr)
		{
			folderfunc(origpath, folderpathDir + *i);
		}
		applyRercursively(origpath, folderpathDir + *i, filefunc, folderfunc, ignoreFolders, ignoreFiles);
	}
}

void applyRercursively(const std::string &folderpath, void(*filefunc)(const std::string &path, const std::string &file), void(*folderfunc)(const std::string &path, const std::string &file) = nullptr, const std::vector<std::pair<bool, std::string>> &ignoreFolders = std::vector<std::pair<bool, std::string>>(), const std::vector<std::pair<bool, std::string>> &ignoreFiles = std::vector<std::pair<bool, std::string>>())
{
	applyRercursively(folderpath, "", filefunc, folderfunc, ignoreFolders, ignoreFiles);
}


void printname(const std::string &path, const std::string &name)
{
	std::cout << "\"" << FileSystem::CanonicalPath(path + "\\" + name) << "\"" << std::endl;
}

void createFolderIfNotExists(const std::string &path, const std::string &name)
{
	//std::string newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, name);
	std::string newpath = name;
	if (Global::pathOffset != "")
	{
		newpath = FileSystem::CombinePaths(Global::pathOffset, newpath);
	}
	newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, newpath);
	if (!FileSystem::FileExists(newpath))
	{
		bool success = false;

		FileSystem::CreateFolder(newpath, success);

		if (!success) errorFunction("Failed to create folder \"" + newpath + "\"");
	}

	//if (Global::pathOffset != "")
	//{
	//	std::cout << putInQuotes(newpath) << std::endl;
	//}
}

void copyFile(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);
	//std::string newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, name);
	std::string newpath = name;
	if (Global::pathOffset != "")
	{
		newpath = FileSystem::CombinePaths(Global::pathOffset, newpath);
	}
	newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, newpath);

	bool success = false;

	//if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) std::cout << "  Warning: \"" << newpath << "\" rewritten by \"" << oldpath << "\"." << std::endl;
	if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) warningFunction(putInQuotes(newpath) + " rewritten by " + putInQuotes(oldpath) + ".");

	FileSystem::BinaryFileCopy(oldpath, newpath, success);

	if (!success) errorFunction("Failed to copy file from \"" + oldpath + "\" to \"" + newpath);
}

void preprocessJavaFile(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);
	//std::string newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, name);
	std::string newpath = name;
	if (Global::pathOffset != "")
	{
		newpath = FileSystem::CombinePaths(Global::pathOffset, newpath);
	}
	newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, newpath);

	Global::varPreprocessScript += putInQuotes(Global::varPreprocessApp) + Global::varPreprocessParameters + " " + putInQuotes(oldpath) + " " + putInQuotes(newpath) + ">NUL\n";

	bool success = false;

	FileSystem::BinaryFileCopy(oldpath, newpath, success);

	if (!success) errorFunction("Failed to copy file from \"" + oldpath + "\" to \"" + newpath);

	//if (Global::pathOffset != "")
	//{
	//	std::cout << putInQuotes(newpath) << std::endl;
	//}
}

void copyFileInPackage(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);

	bool success = false;
	std::string filename = FileSystem::FileName(name, success);
	if (!success)
	{
		errorFunction("Could not get file name for \"" + oldpath + "\"");
	}

	std::vector<std::string> package;
	bool errorOccured = false;
	std::string errorMsg = "";
	extractPackageFromFile(oldpath, package, errorOccured, errorMsg);
	if (errorOccured)
	{
		errorFunction(errorMsg);
	}
	
	//std::string newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, name);
	std::string newpath = Global::varCopyDestinationFolder;
	if (Global::pathOffset != "")
	{
		newpath = FileSystem::CombinePaths(newpath, Global::pathOffset);
		if (!FileSystem::FileExists(newpath)) { FileSystem::CreateFolder(newpath, success); if (!success) errorFunction("Failed to create folder \"" + newpath + "\""); }
	}
	for (size_t i = 0; i < package.size(); i++)
	{
		newpath = FileSystem::CombinePaths(newpath, package[i]);
		if (!FileSystem::FileExists(newpath)) { FileSystem::CreateFolder(newpath, success); if (!success) errorFunction("Failed to create folder \"" + newpath + "\""); }
	}
	
	newpath = FileSystem::CombinePaths(newpath, filename);
	
	//if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) std::cout << "  Warning: \"" << newpath << "\" rewritten by \"" << oldpath << "\"." << std::endl;
	if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) warningFunction(putInQuotes(newpath) + " rewritten by " + putInQuotes(oldpath) + ".");

	FileSystem::BinaryFileCopy(oldpath, newpath, success);

	if (!success) errorFunction("Failed to copy file from \"" + oldpath + "\" to \"" + newpath);
}

void copyJarFile(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);
	std::string newpath = FileSystem::CombinePaths(Global::varCopyDestinationFolder, FileSystem::FileName(name));

	bool success = false;

	//if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) std::cout << "  Warning: \"" << newpath << "\" rewritten by \"" << oldpath << "\"." << std::endl;
	if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) warningFunction(putInQuotes(newpath) + " rewritten by " + putInQuotes(oldpath) + ".");

	FileSystem::BinaryFileCopy(oldpath, newpath, success);

	if (!success) errorFunction("Failed to copy file from \"" + oldpath + "\" to \"" + newpath);

	Global::libs.push_back(FileSystem::FileName(name));
}

void copySoFile(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);
	
	std::string typeUsed = Global::varJNIType;
	//if (Global::varJNIType == Global::varJNILibsDefaultType)
	//if (Global::varJNIType == projectData.defaultJniType)
	if (typeUsed == XMLParser::Java2Android::unspecifiedJniType)
	{
		std::string foldername = FileSystem::FileName(FileSystem::FolderPath(oldpath));

		size_t index = 0;
		bool found = false;
		for (size_t i = 0; i < Global::varJNITypesCount; i++)
		{
			if (foldername == Global::varJNITypes[i])
			{
				found = true;
				index = i;
				typeUsed = foldername;
				break;
			}
		}
		if (!found)
		{
			errorFunction("Could not determine unspecified architecture type for file " + FileSystem::CombinePaths(path, name));
		}
	}

	bool setInPojectProperties = false;
	for (size_t i = 0; i < projectData.architectureTypeIndices.size(); i++)
	{
		if (typeUsed == Global::varJNITypes[projectData.architectureTypeIndices[i]])
		{
			setInPojectProperties = true;
			break;
		}
	}

	if (setInPojectProperties)
	{
		std::string destfoldername = FileSystem::CombinePaths(Global::varCopyDestinationFolder, typeUsed);
		std::string newpath = FileSystem::CombinePaths(destfoldername, FileSystem::FileName(name));

		bool success = false;

		if (!FileSystem::FileExists(destfoldername))
		{
			FileSystem::CreateFolder(destfoldername, success);

			if (!success) errorFunction("Failed to create folder \"" + destfoldername + "\"");
		}

		//if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) std::cout << "  Warning: \"" << newpath << "\" rewritten by \"" << oldpath << "\"." << std::endl;
		if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) warningFunction(putInQuotes(newpath) + " rewritten by " + putInQuotes(oldpath) + ".");

		FileSystem::BinaryFileCopy(oldpath, newpath, success);

		if (!success) errorFunction("Failed to copy file from \"" + oldpath + "\" to \"" + newpath);
	}
}

void deleteFile(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);
	
	bool success = false;

	FileSystem::FileDelete(oldpath, success);

	if (!success) errorFunction("Failed to delete file \"" + oldpath);
}

void saveFolderForRemoval(const std::string &path, const std::string &name)
{
	std::string oldpath = FileSystem::CombinePaths(path, name);
	
	Global::foldersToDelete.push_back(oldpath);
}


void CopyFilesToWorkspace(const XMLParser::J2AData &data)
{
	bool success = false;
	
	if (data.useManifest)
	{
		Global::showOverwriteWarning = true;
		std::string oldpath = FileSystem::CombinePaths(Global::varJ2ARoot, data.manifestPath);
		std::string newpath = Global::varTmpFilePathManifest;
		if (!FileSystem::FileExists(oldpath)) errorFunction(putInQuotes(oldpath) + " does not exist.");
		//if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) std::cout << "  Warning: \"" << newpath << "\" rewritten by \"" << oldpath << "\"." << std::endl;
		if (Global::showOverwriteWarning && FileSystem::FileExists(newpath)) warningFunction(putInQuotes(newpath) + " rewritten by " + putInQuotes(oldpath) + ".");
		//FileSystem::BinaryFileCopy(FileSystem::CombinePaths(Global::varJ2ARoot, data.manifestPath), Global::varTmpFilePathManifest, success);
		FileSystem::BinaryFileCopy(oldpath, newpath, success);
		if (!success)
		{
			errorFunction("Failed to copy manifest file");
		}
		Global::showOverwriteWarning = false;
	}
	
	
	if (data.useProguard)
	{
		std::string oldpath = FileSystem::CombinePaths(Global::varJ2ARoot, data.proguardPath);
		std::string newpath = Global::varTmpFilePathProguard;
		if (!FileSystem::FileExists(oldpath)) errorFunction(putInQuotes(oldpath) + " does not exist.");
		std::stringstream stream;
		stream << std::endl;
		stream << std::endl;
		stream << "# === \"" << data.name << "\" proguard rules === " << std::endl;
		stream << std::endl;
		//FileSystem::WriteToFile(stream, varTmpFilePathProguard, success);
		FileSystem::AppendToFile(stream, Global::varTmpFilePathProguard, success);
		FileSystem::AppendCopyFile(oldpath, newpath, success);
		FileSystem::AppendToFile("\n\n", Global::varTmpFilePathProguard, success);
		if (!success)
		{
			errorFunction("Failed to append proguard file");
		}
	}
	
	
	Global::varPreprocessScript = "@echo off\n";
	if (data.useDirs[XMLParser::J2AData::Directories::Java])
	{
		Global::showOverwriteWarning = true;
		// clear tmp folder
		Global::varCopyDestinationFolder = "";
		Global::varCheckedExtension = "";
		Global::foldersToDelete.clear();
		applyRercursively(Global::globalFolderPathSrcTemp, deleteFile, saveFolderForRemoval);
		for (int i = Global::foldersToDelete.size() - 1; i >= 0; i--)
		{
			FileSystem::RemoveFolder(Global::foldersToDelete[i], success);
			if (!success) errorFunction("Failed to delete folder \"" + Global::foldersToDelete[i]);
		}
		
		// (preprocess and) copy to tmp
		//Global::varCopyDestinationFolder = Global::varTmpFolderPathJava;
		Global::varCopyDestinationFolder = Global::globalFolderPathSrcTemp;
		Global::varCheckedExtension = "java";
		Global::varPreprocessParameters = Global::varInitialPreprocessParameters;

		if (data.useJavaPreprocess)
		{
			for (int i = 0; i < projectData.includesList.size(); i++)
			{
				Global::varPreprocessParameters += " -include " + putInQuotes(projectData.includesList[i]);
			}
			for (int i = 0; i < data.javaPreprocess.includes.size(); i++)
			{
				Global::varPreprocessParameters += " -include " + putInQuotes(FileSystem::CombinePaths(Global::varJ2ARoot, data.javaPreprocess.includes[i]));
			}
			for (int i = 0; i < projectData.macrosList.size(); i++)
			{
				Global::varPreprocessParameters += " -imacros " + putInQuotes(projectData.macrosList[i]);
			}
			for (int i = 0; i < data.javaPreprocess.macros.size(); i++)
			{
				Global::varPreprocessParameters += " -imacros " + putInQuotes(FileSystem::CombinePaths(Global::varJ2ARoot, data.javaPreprocess.macros[i]));
			}
		}
		//for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::Java].size(); i++)
		//{
		//	const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::Java][i];
		//	bool success = false;
		//	std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
		//	std::string foldername = FileSystem::FileName(folderpath, success); if (!success) errorFunction("Failed to extract folder name from \"" + folderpath + "\"");
		//	std::string folderpathLibName = FileSystem::CombinePaths(Global::varCopyDestinationFolder, data.name);
		//	std::string folderpathPackageName = FileSystem::CombinePaths(folderpathLibName, foldername);
		//	Global::pathOffset = data.name + "\\" + foldername;
		//	//Global::pathOffset = "";
		//	if (!FileSystem::FileExists(folderpathLibName)) { FileSystem::CreateFolder(folderpathLibName, success); if (!success) errorFunction("Failed to create folder \"" + folderpathLibName + "\""); }
		//	if (!FileSystem::FileExists(folderpathPackageName)) { FileSystem::CreateFolder(folderpathPackageName, success); if (!success) errorFunction("Failed to create folder \"" + folderpathPackageName + "\""); }
		//	if (data.useJavaPreprocess)
		//	{
		//		applyRercursively(folderpath, preprocessJavaFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
		//	}
		//	else
		//	{
		//		applyRercursively(folderpath, copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
		//	}
		//}
		for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::Java].size(); i++)
		{
			const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::Java][i];
			//bool success = false;
			std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
			if (!FileSystem::FileExists(folderpath)) errorFunction(putInQuotes(folderpath) + " does not exist.");
			//std::string foldername = FileSystem::FileName(folderpath, success); if (!success) errorFunction("Failed to extract folder name from \"" + folderpath + "\"");
			//std::string folderpathLibName = FileSystem::CombinePaths(Global::varCopyDestinationFolder, data.name);
			//std::string folderpathPackageName = FileSystem::CombinePaths(folderpathLibName, foldername);
			//Global::pathOffset = data.name + "\\" + foldername;
			////Global::pathOffset = "";
			//if (!FileSystem::FileExists(folderpathLibName)) { FileSystem::CreateFolder(folderpathLibName, success); if (!success) errorFunction("Failed to create folder \"" + folderpathLibName + "\""); }
			//if (!FileSystem::FileExists(folderpathPackageName)) { FileSystem::CreateFolder(folderpathPackageName, success); if (!success) errorFunction("Failed to create folder \"" + folderpathPackageName + "\""); }
			if (data.useJavaPreprocess)
			{
				applyRercursively(folderpath, preprocessJavaFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
			}
			else
			{
				applyRercursively(folderpath, copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
			}
		}

		std::string preprocessbatname = FileSystem::CombinePaths(Global::varRootDirectory, "preprocessJava.bat");
		std::ofstream out(preprocessbatname);
		out << Global::varPreprocessScript;
		out.close();
		system(("call " + putInQuotes(preprocessbatname)).c_str());
		FileSystem::FileDelete(preprocessbatname.c_str(), success);
		if (!success) errorFunction("Failed to delete file \"" + preprocessbatname + "\"");

		Global::varCopyDestinationFolder = Global::varTmpFolderPathJava;
		//bool success = false;
		std::string folderpathLibName = Global::varCopyDestinationFolder;
		if (data.buildType == XMLParser::J2AData::BuildType::Copy)
		{
			folderpathLibName = FileSystem::CombinePaths(folderpathLibName, data.name);
			Global::pathOffset = data.name;
		}
		if (!FileSystem::FileExists(folderpathLibName)) { FileSystem::CreateFolder(folderpathLibName, success); if (!success) errorFunction("Failed to create folder \"" + folderpathLibName + "\""); }
		//applyRercursively(Global::globalFolderPathSrcTemp, copyFile, createFolderIfNotExists);
		applyRercursively(Global::globalFolderPathSrcTemp, copyFileInPackage, nullptr);
		Global::showOverwriteWarning = false;
	}
	Global::pathOffset = "";

	//std::cout << "-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-";
	//system("pause");

	if (data.useDirs[XMLParser::J2AData::Directories::AIDL])
	{
		Global::showOverwriteWarning = true;
		// clear tmp folder
		Global::varCopyDestinationFolder = "";
		Global::varCheckedExtension = "";
		Global::foldersToDelete.clear();
		applyRercursively(Global::globalFolderPathSrcTemp, deleteFile, saveFolderForRemoval);
		for (int i = Global::foldersToDelete.size() - 1; i >= 0; i--)
		{
			FileSystem::RemoveFolder(Global::foldersToDelete[i], success);
			if (!success) errorFunction("Failed to delete folder \"" + Global::foldersToDelete[i]);
		}

		//Global::varCopyDestinationFolder = Global::varTmpFolderPathAIDL;
		Global::varCopyDestinationFolder = Global::globalFolderPathSrcTemp;
		Global::varCheckedExtension = "aidl";
		//for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::AIDL].size(); i++)
		//{
		//	const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::AIDL][i];

		//	applyRercursively(FileSystem::CombinePaths(Global::varJ2ARoot, dir.path), copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
		//}
		for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::AIDL].size(); i++)
		{
			const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::AIDL][i];
			std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
			if (!FileSystem::FileExists(folderpath)) errorFunction(putInQuotes(folderpath) + " does not exist.");

			applyRercursively(folderpath, copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
		}

		Global::varCopyDestinationFolder = Global::varTmpFolderPathAIDL;
		applyRercursively(Global::globalFolderPathSrcTemp, copyFileInPackage, nullptr);
		Global::showOverwriteWarning = false;
	}


	if (data.useDirs[XMLParser::J2AData::Directories::Res])
	{
		Global::showOverwriteWarning = true;
		Global::varCopyDestinationFolder = Global::varTmpFolderPathRes;
		Global::varCheckedExtension = "";
		for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::Res].size(); i++)
		{
			const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::Res][i];
			std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
			if (!FileSystem::FileExists(folderpath)) errorFunction(putInQuotes(folderpath) + " does not exist.");
			applyRercursively(folderpath, copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
		}
		Global::showOverwriteWarning = false;
	}


	if (data.useDirs[XMLParser::J2AData::Directories::Assets])
	{
		Global::showOverwriteWarning = true;
		Global::varCopyDestinationFolder = Global::varTmpFolderPathAssets;
		Global::varCheckedExtension = "";
		for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::Assets].size(); i++)
		{
			const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::Assets][i];
			std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
			if (!FileSystem::FileExists(folderpath)) errorFunction(putInQuotes(folderpath) + " does not exist.");
			applyRercursively(folderpath, copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
		}
		Global::showOverwriteWarning = false;
	}


	if (data.useDirs[XMLParser::J2AData::Directories::Libs])
	{
		Global::showOverwriteWarning = true;
		Global::varCopyDestinationFolder = Global::varTmpFolderPathLibs;
		Global::varCheckedExtension = "jar";
		for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::Libs].size(); i++)
		{
			const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::Libs][i];
			std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
			if (!FileSystem::FileExists(folderpath)) errorFunction(putInQuotes(folderpath) + " does not exist.");
			applyRercursively(folderpath, copyJarFile, nullptr, dir.folderIgnores, dir.fileIgnores);
		}
		Global::showOverwriteWarning = false;
	}


	if (data.useDirs[XMLParser::J2AData::Directories::JniLibs])
	{
		Global::showOverwriteWarning = true;
		Global::varCopyDestinationFolder = Global::varTmpFolderPathJniLibs;
		Global::varCheckedExtension = "so";
		for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::JniLibs].size(); i++)
		{
			Global::varJNIType = data.jniLibsType[i];
			const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::JniLibs][i];
			std::string folderpath = FileSystem::CombinePaths(Global::varJ2ARoot, dir.path);
			if (!FileSystem::FileExists(folderpath)) errorFunction(putInQuotes(folderpath) + " does not exist.");
			applyRercursively(folderpath, copySoFile, nullptr, dir.folderIgnores, dir.fileIgnores);
		}
		Global::showOverwriteWarning = false;
	}


	//if (data.useDirs[XMLParser::J2AData::Directories::Native])
	//{
	//	Global::varCopyDestinationFolder = Global::varTmpFolderPathNative;
	//	Global::varCheckedExtension = "";
	//	for (int i = 0; i < data.dirs[XMLParser::J2AData::Directories::Native].size(); i++)
	//	{
	//		const XMLParser::DirData &dir = data.dirs[XMLParser::J2AData::Directories::Native][i];
	//		applyRercursively(FileSystem::CombinePaths(Global::varJ2ARoot, dir.path), copyFile, createFolderIfNotExists, dir.folderIgnores, dir.fileIgnores);
	//	}
	//}
}

void generateGradleBuildScript(const XMLParser::J2AData &data, std::stringstream &out)
{
	out.str(" ");
	//DO NOT SUPPORT APK
	//DO NOT SUPPORT NATIVE FOLDER
	if (data.buildType == XMLParser::J2AData::BuildType::Copy)
	{
		return;
	}
	
	out << "buildscript {"																															<< std::endl;
	out << "	repositories {"																														<< std::endl;
	out << "		jcenter()"																														<< std::endl;
	out << "	}"																																	<< std::endl;
	out << "	dependencies {"																														<< std::endl;
	out << "		classpath 'com.android.tools.build:gradle:1.1.0'"																				<< std::endl;
	out << "	}"																																	<< std::endl;
	out << "}"																																		<< std::endl;
	out << ""																																		<< std::endl;
	out << "allprojects {"																															<< std::endl;
	out << "	repositories {"																														<< std::endl;
	out << "		jcenter()"																														<< std::endl;
	out << "	}"																																	<< std::endl;
	out << "}"																																		<< std::endl;
	out << ""																																		<< std::endl;
	switch (data.buildType)
	{
	case XMLParser::J2AData::BuildType::JavaLibrary:
	{
	out << "apply plugin: 'java'"																													<< std::endl;
	}
	break;
	case XMLParser::J2AData::BuildType::AndroidLibrary:
	{
	out << "apply plugin: 'com.android.library'"																									<< std::endl;
	}
	break;
	case XMLParser::J2AData::BuildType::AndroidApplication:
	{
	out << "apply plugin: 'com.android.application'"																								<< std::endl;
	}
	break;
	}
	out << ""																																		<< std::endl;
	out << "repositories {"																															<< std::endl;
 	out << "	flatDir {"																															<< std::endl;
	out << "		dirs '../" << Global::varTmpLocalFolderPathLibs << "'" << std::endl;
	out << "	}"																																	<< std::endl;
	out << "}"																																		<< std::endl;
	out << ""																																		<< std::endl;
	out << "dependencies {"																															<< std::endl;
	for (std::vector<std::string>::iterator i = Global::libs.begin(); i != Global::libs.end(); i++)
	{
	out << "	compile(name:'" << FileSystem::FileRemoveExtension(*i) << "', ext:'jar')"															<< std::endl;
	}
	out << "}"																																		<< std::endl;
	out << ""																																		<< std::endl;
	if (data.buildType != XMLParser::J2AData::BuildType::JavaLibrary)
	{
	out << "android {"																																<< std::endl;
	out << "	compileSdkVersion " << data.compileSdkVersion																						<< std::endl;
	out << "	buildToolsVersion \"" << data.buildToolsVersion[0] << "." << data.buildToolsVersion[1] << "." << data.buildToolsVersion[2] << "\""	<< std::endl;
	out << ""																																		<< std::endl;
	out << "	defaultConfig {"																													<< std::endl;
	if (data.useMinSdkVersion)
	out << "		minSdkVersion " << data.minSdkVersion																							<< std::endl;
	if (data.useTargetSdkVersion)
	out << "		targetSdkVersion " << data.targetSdkVersion																						<< std::endl;
	out << "	}"																																	<< std::endl;
	out << ""																																		<< std::endl;
	}
	out << "	sourceSets {"																														<< std::endl;
	out << "		main {"																															<< std::endl;
	if (data.useManifest)
	out << "			manifest.srcFile '../" << Global::varTmpLocalFilePathManifest << "'"														<< std::endl;
	if (data.useDirs[XMLParser::J2AData::Directories::Java])
	out << "			java.srcDirs = ['../" << Global::varTmpLocalFolderPathJava << "']"															<< std::endl;
	if (data.useDirs[XMLParser::J2AData::Directories::Res])
	out << "			res.srcDirs = ['../" << Global::varTmpLocalFolderPathRes << "']"															<< std::endl;
	if (data.useDirs[XMLParser::J2AData::Directories::AIDL])
	out << "			aidl.srcDirs = ['../" << Global::varTmpLocalFolderPathAIDL << "']"															<< std::endl;
	if (data.useDirs[XMLParser::J2AData::Directories::Assets])
	out << "			assets.srcDirs = ['../" << Global::varTmpLocalFolderPathAssets <<"']"														<< std::endl;
	if (data.useDirs[XMLParser::J2AData::Directories::JniLibs])
	out << "			jniLibs.srcDir '../" << Global::varTmpLocalFolderPathJniLibs << "'"															<< std::endl;
	out << "		}"																																<< std::endl;
	out << "	}"																																	<< std::endl;
	if (data.buildType != XMLParser::J2AData::BuildType::JavaLibrary)
	{
	out << "}"																																		<< std::endl;
	}
}

// Local paths need refining
//void generateMainGradleBuildScript(const XMLParser::Java2Android &data, std::stringstream &out)
//{
//	//std::cout << "--- to forward slashes... -------------------" << std::endl;
//	//std::cout << FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.signatureKeystoreRelativePath)) << std::endl;
//	//std::cout << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.signatureKeystoreRelativePath))) << std::endl;
//	//system("pause");
//
//	out.str(" ");
//	out << "apply plugin: 'com.android.application'" << std::endl;
//	out << "" << std::endl;
//	out << "android {" << std::endl;
//	out << "	compileSdkVersion " << data.compileSDK << "" << std::endl;
//	out << "	buildToolsVersion \"" << data.buildTools[0] << "." << data.buildTools[1] << "." << data.buildTools[2] << "\"" << std::endl;
//	out << "" << std::endl;
//	out << "	defaultConfig {" << std::endl;
//	if (data.useApplicationID)
//		out << "		applicationId \"" << data.applicationID << "\"" << std::endl;
//	if (data.useMinSDK)
//		out << "		minSdkVersion " << data.minSDK << "" << std::endl;
//	if (data.useTargetSDK)
//		out << "		targetSdkVersion " << data.targetSDK << "" << std::endl;
//	out << "	}" << std::endl;
//	out << "" << std::endl;
//	out << "	signingConfigs {" << std::endl;
//	out << "		release {" << std::endl;
//	out << "			storeFile file(\"" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.signatureKeystoreRelativePath))) << "\")" << std::endl;
//	out << "			storePassword \"" << data.signatureKeystorePass << "\"" << std::endl;
//	out << "			keyAlias \"" << data.signatureKeyName << "\"" << std::endl;
//	out << "			keyPassword \"" << data.signatureKeyPass << "\"" << std::endl;
//	out << "		}" << std::endl;
//	out << "	}" << std::endl;
//	out << "" << std::endl;
//	out << "	buildTypes {" << std::endl;
//	out << "		debug {" << std::endl;
//	out << "			debuggable true" << std::endl;
//	out << "			jniDebugBuild true" << std::endl;
//	out << "			signingConfig signingConfigs.release" << std::endl;
//	out << "		}" << std::endl;
//	out << "		release {" << std::endl;
//	if (data.useWorkspaceLocalProguard)
//	{
//	out << "			runProguard true" << std::endl;
//	out << "			proguardFiles getDefaultProguardFile('proguard-android.txt'), '" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeProguard))) << "'" << std::endl;
//	}
//	out << "			signingConfig signingConfigs.release" << std::endl;
//	out << "		}" << std::endl;
//	out << "	}" << std::endl;
//	out << "" << std::endl;
//	out << "	lintOptions {" << std::endl;
////	out << "		abortOnError false" << std::endl;
//	out << "" << std::endl;
//	out << "" << data.lintOptions << "" << std::endl;
//	out << "" << std::endl;
//	out << "	}" << std::endl;
//	out << "" << std::endl;
//	out << "	packagingOptions {" << std::endl;
////	out << "		exclude 'META-INF/LICENSE.txt'" << std::endl;
////	out << "		exclude 'META-INF/NOTICE.txt'" << std::endl;
//	out << "" << std::endl;
//	out << "" << data.packageOptions<< "" << std::endl;
//	out << "" << std::endl;
//	out << "	}" << std::endl;
//	out << "" << std::endl;
//	out << "	sourceSets.main {" << std::endl;
//	out << "		manifest.srcFile '" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeManifest))) << "'" << std::endl;
//	if (data.useWorkspaceLocalJava)
//	out << "		java.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeJava))) << "']" << std::endl;
//	if (data.useWorkspaceLocalRes)
//	out << "		res.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeRes))) << "']" << std::endl;
//	if (data.useWorkspaceLocalAIDL)
//	out << "		aidl.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeAIDL))) << "']" << std::endl;
//	if (data.useWorkspaceLocalAssets)
//	out << "		assets.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeAssets))) << "']" << std::endl;
//	if (data.useWorkspaceLocalJniLibs)
//	out << "		jniLibs.srcDir '" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeJniLibs))) << "'" << std::endl;
//	out << "	}" << std::endl;
//	out << "}" << std::endl;
//	out << "" << std::endl;
//	out << "repositories {" << std::endl;
//	out << "	flatDir {" << std::endl;
//	out << "		dirs '" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeLibs))) << "'" << std::endl;
//	out << "	}" << std::endl;
//	out << "}" << std::endl;
//	out << "" << std::endl;
//	out << "dependencies {" << std::endl;
//	out << "	compile fileTree(dir: '" << FileSystem::CanonicalToForwardSlashesPath(FileSystem::CanonicalPath(FileSystem::CombinePaths("..\\..", data.workspaceRelativeLibs))) << "', include: ['*.jar'])" << std::endl;
//	out << "" << std::endl;
//	for (std::vector<std::string>::iterator i = Global::externalsAAR.begin(); i != Global::externalsAAR.end(); i++)
//	out << "compile(name:'" << *i << "', ext : 'aar')" << std::endl;
//	out << "" << std::endl;
//	out << "}" << std::endl;
//}

void generateMainGradleBuildScript(const XMLParser::Java2Android &data, std::stringstream &out)
{
	out.str(" ");
	out << "apply plugin: 'com.android.application'" << std::endl;
	out << "" << std::endl;
	out << "android {" << std::endl;
	out << "	compileSdkVersion " << data.compileSDK << "" << std::endl;
	out << "	buildToolsVersion \"" << data.buildTools[0] << "." << data.buildTools[1] << "." << data.buildTools[2] << "\"" << std::endl;
	out << "" << std::endl;
	out << "	defaultConfig {" << std::endl;
	if (data.useApplicationID)
		out << "		applicationId \"" << data.applicationID << "\"" << std::endl;
	if (data.useMinSDK)
		out << "		minSdkVersion " << data.minSDK << "" << std::endl;
	if (data.useTargetSDK)
		out << "		targetSdkVersion " << data.targetSDK << "" << std::endl;
	out << "	}" << std::endl;
	out << "" << std::endl;
	out << "	signingConfigs {" << std::endl;
	out << "		release {" << std::endl;
	out << "			storeFile file(\"" << FileSystem::CanonicalToForwardSlashesPath(data.signatureKeystorePath) << "\")" << std::endl;
	out << "			storePassword \"" << data.signatureKeystorePass << "\"" << std::endl;
	out << "			keyAlias \"" << data.signatureKeyName << "\"" << std::endl;
	out << "			keyPassword \"" << data.signatureKeyPass << "\"" << std::endl;
	out << "		}" << std::endl;
	out << "	}" << std::endl;
	out << "" << std::endl;
	out << "	buildTypes {" << std::endl;
	out << "		debug {" << std::endl;
	out << "			debuggable true" << std::endl;
	out << "			jniDebuggable true" << std::endl;
	out << "			signingConfig signingConfigs.release" << std::endl;
	out << "		}" << std::endl;
	out << "		release {" << std::endl;
	if (data.useWorkspaceLocalProguard)
	{
		out << "			minifyEnabled true" << std::endl;
		out << "			proguardFiles getDefaultProguardFile('proguard-android.txt'), '" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceProguard) << "'" << std::endl;
	}
	out << "			signingConfig signingConfigs.release" << std::endl;
	out << "		}" << std::endl;
	out << "	}" << std::endl;
	out << "" << std::endl;
	out << "	lintOptions {" << std::endl;
	//	out << "		abortOnError false" << std::endl;
	out << "" << std::endl;
	out << "" << data.lintOptions << "" << std::endl;
	out << "" << std::endl;
	out << "	}" << std::endl;
	out << "" << std::endl;
	out << "	packagingOptions {" << std::endl;
	//	out << "		exclude 'META-INF/LICENSE.txt'" << std::endl;
	//	out << "		exclude 'META-INF/NOTICE.txt'" << std::endl;
	out << "" << std::endl;
	out << "" << data.packageOptions << "" << std::endl;
	out << "" << std::endl;
	out << "	}" << std::endl;
	out << "" << std::endl;
	out << "	sourceSets.main {" << std::endl;
	out << "		manifest.srcFile '" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceManifest) << "'" << std::endl;
	if (data.useWorkspaceLocalJava)
		out << "		java.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceJava) << "']" << std::endl;
	if (data.useWorkspaceLocalRes)
		out << "		res.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceRes) << "']" << std::endl;
	if (data.useWorkspaceLocalAIDL)
		out << "		aidl.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceAIDL) << "']" << std::endl;
	if (data.useWorkspaceLocalAssets)
		out << "		assets.srcDirs = ['" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceAssets) << "']" << std::endl;
	if (data.useWorkspaceLocalJniLibs)
		out << "		jniLibs.srcDir '" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceJniLibs) << "'" << std::endl;
	out << "	}" << std::endl;
	out << "}" << std::endl;
	out << "" << std::endl;
	out << "repositories {" << std::endl;
	out << "	flatDir {" << std::endl;
	out << "		dirs '" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceLibs) << "'" << std::endl;
	out << "	}" << std::endl;
	out << "}" << std::endl;
	out << "" << std::endl;
	out << "dependencies {" << std::endl;
	out << "	compile fileTree(dir: '" << FileSystem::CanonicalToForwardSlashesPath(data.workspaceLibs) << "', include: ['*.jar'])" << std::endl;
	out << "" << std::endl;
	for (std::vector<std::string>::iterator i = Global::externalsAAR.begin(); i != Global::externalsAAR.end(); i++)
		//out << "compile(name:'" << *i << "', ext : 'aar')" << std::endl;
	{
		out << "debugCompile(name:'" << *i << "-debug" << "', ext : 'aar')" << std::endl;
		out << "releaseCompile(name:'" << *i << "-release" << "', ext : 'aar')" << std::endl;
	}
	out << "" << std::endl;
	out << "}" << std::endl;
}


void printUsage()
{
	std::cout << "Usage is 'Java2Android [paramName paramValue]'" << std::endl;
	std::cout << "The following parameters are supported:" << std::endl;
	std::cout << std::endl;
	std::cout << "   --java2android       = List of semicolon separated paths to the java2android files." << std::endl;
//	std::cout << std::endl;
//	std::cout << "   --defaults           = Path to the file containing the default boolean flags." << std::endl;
//	std::cout << "   --config             = Path to the file containing boolean flags overrides." << std::endl;
//	std::cout << "   --environment        = Path to the file containing environment specific boolean flags." << std::endl;
	std::cout << std::endl;
	std::cout << "   --project            = Path to the generated Android Studio Project." << std::endl;
	std::cout << std::endl;
	//std::cout << "   --flags              = Path to the file containing the boolean flags used to select libraries." << std::endl;
	std::cout << "   --printflags         = Pring list of bool flags and their values. Can be set to \"true\" of \"false\"." << std::endl;
	std::cout << std::endl;
	//std::cout << "   --work               = Path to the workspace folder." << std::endl;
	//std::cout << "   --manifest           = Path to the android manifest file, relative to the workspace folder." << std::endl;
	//std::cout << "   --proguard           = Path to the proguard config file, relative to the workspace folder." << std::endl;
	//std::cout << "   --java               = Path to the java source folder, relative to the workspace folder." << std::endl;
	//std::cout << "   --aidl               = Path to the aidl source folder, relative to the workspace folder." << std::endl;
	//std::cout << "   --res                = Path to the resource folder, relative to the workspace folder." << std::endl;
	//std::cout << "   --asssets            = Path to the asssets folder, relative to the workspace folder." << std::endl;
	//std::cout << "   --libs               = Path to the java libraries folder, relative to the workspace folder." << std::endl;
	//std::cout << "   --jnilibs            = Path to the native libraries folder, relative to the workspace folder." << std::endl;
//	std::cout << "   --native             = Path to the native source folder, relative to the workspace folder." << std::endl;
	//std::cout << std::endl;
	std::cout << "   --preprocess         = Path to the application used to preprocess java source files." << std::endl;
//	std::cout << "   --preprocessApp      = Path to the application used to preprocess java source files." << std::endl;
//	std::cout << "   --preprocessParams   = String of parameters passed to the preprocessing application." << std::endl;
	std::cout << std::endl;
	std::cout << "   --zip				  = Path to the application used to open zip archives." << std::endl;
	std::cout << std::endl;
	//std::cout << "   --jniDefault         = The default architecture used for native libraries." << std::endl;
	//std::cout << std::endl;
	std::cout << "   --rebuild            = Rebuild all Java Libraries and Android Libraries even if their artefacts already exist in the workspace folder. Can be set to \"true\" of \"false\"." << std::endl;
	std::cout << std::endl;
	//std::cout << "   --externals          = Path to output file that will contain a list of all compiled .aar file name." << std::endl;
	//std::cout << std::endl;
	//std::cout << "   --defines            = A list of semicolon separated header file paths used for source preprocessing." << std::endl;
	//std::cout << "   --macros             = A list of semicolon separated header file paths used for source preprocessing." << std::endl;
	//std::cout << std::endl;
	std::cout << "   --architecture       = List of semicolon separated architecture names." << std::endl;
	std::cout << std::endl;
}

void errorAndUsage(const std::string &msg, int val = EXIT_FAILURE)
{
	std::cout << "Error: " << msg << std::endl;
	
	printUsage();

	//fflush(stdout);
	//fflush(stderr);

	exit(val);
}

void processArguments(int argc, char* argv[])
{
	if (argc % 2 == 0)
	{
		errorFunction("Incorrect usage.");
	}

	if (argc == 1)
	{
		printUsage();

		//fflush(stdout);
		//fflush(stderr);

		exit(EXIT_FAILURE);
	}

	bool java2androidUsed = false;

	bool projectUsed = false;

	bool defaultsUsed = false;
	bool configUsed = false;
	bool environmentUsed = false;
	//or
	bool flagsUsed = false;

	bool workUsed = false;
	bool manifestUsed = false;
	bool proguardUsed = false;
	bool javaUsed = false;
	bool aidlUsed = false;
	bool resUsed = false;
	bool assetsUsed = false;
	bool libsUsed = false;
	bool jnilibsUsed = false;
	bool nativeUsed = false;

	bool preprocessAppUsed = false;
	bool preprocessParamsUsed = false;

	bool zipUsed = false;

	bool jniDefaultUsed = false;

	bool alwaysRebuildUsed = false;
	bool printBoolFlagsUsed = false;

	bool externalsUsed = false;

	bool definesUsed = false;
	bool macrosUsed = false;

	bool hideWarningsUsed = false;

	bool architectureUsed = false;

	//--java2android		// global or relative to cd

//	//--defaults			// global or relative to cd
//	//--config				// global or relative to cd
//	//--environment			// global or relative to cd
//	////or
	//--flags				// global or relative to cd
	//--printflags			// true or false

	//--work				// global or relative to cd
	//--manifest			// local to work
	//--proguard			// local to work
	//--java				// local to work
	//--aidl				// local to work
	//--res					// local to work
	//--asssets				// local to work
	//--libs				// local to work
	//--jnilibs				// local to work
	//--native				// local to work ???

	//--preprocessApp		// global or relative to cd
//	//--preprocessParams	// " -C -P" ?????????????????????????

	//--zip					// global or relative to cd

	//--jniDefault			// one of the four

	//--rebuild				// true or false

	//--externals			// global or relative to cd

	//--defines				// path;path;path
	//--macros				// path;path.path
	
	//--hideWarnings

	//--architecture		//list of semicolon separated architecture names;


	//v	std::string filePathJava2Android;
	//v	std::string filePathDefaults;		// 
	//v	std::string filePathConfig;			// for flags... or just one
	//v	std::string filePathEnvironment;	// 
	//v std::string filePathFlags;			// sau

	//v	std::string folderPathWorkspace;
	//v	std::string localFilePathManifest;
	//v	std::string localFilePathProguard;
	//v	std::string localFolderPathJava;
	//v	std::string localFolderPathAIDL;
	//v	std::string localFolderPathRes;
	//v	std::string localFolderPathAssets;
	//v	std::string localFolderPathLibs;
	//v	std::string localFolderPathJniLibs;
	//v	std::string localFolderPathNative;

	//v	std::string varJNILibsDefaultType;

	//v	std::string varPreprocessApp;
	//v	std::string varPreprocessParameters;

	//v bool alwaysRebuild;

	bool success = false;

	for (int i = 1; i < argc; i+=2)
	{
		bool error = false;
		std::string param = argv[i];
		std::string val = argv[i+1];
		
		//if (val.size() > 2 && val[0] == '"' && val[val.size() - 1] == '"')
		//{
		//	val = val.substr(1, val.size() - 2);
		//}

		dbg(param);
		dbg(val);
		dbg("");
		
		if (param == "--java2android")
		{
			if (java2androidUsed)	{ errorAndUsage("Duplicate argument."); }
			//Global::filePathJava2Android = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
			//if (!success)			{ errorAndUsage("--java2android path is invalid."); }
			std::stringstream ss(val);
			std::string token;
			while (std::getline(ss, token, ';'))
			{
				if (token != "")
				{
					Global::java2androidPathList.push_back(FileSystem::FullPath(FileSystem::CanonicalPath(token), success));
					if (!success)			{ errorAndUsage("--java2android " + putInQuotes(token) + " path is invalid."); }
					Global::java2androidFolderList.push_back(FileSystem::FolderPath(Global::java2androidPathList[Global::java2androidPathList.size() - 1], success));
					if (!success)			{ errorAndUsage("--java2android " + putInQuotes(token) + " path is invalid."); }
				}
			}
			java2androidUsed = true;
		}
		else  if (param == "--project")
		{
			if (projectUsed)	{ errorAndUsage("Duplicate argument."); }
			Global::varLocalFolderPathAndroidStudioProject = /*FileSystem::FullPath(*/FileSystem::CanonicalPath(val)/*, success)*/;
			//if (!success)			{ errorAndUsage("--project path is invalid."); }
			projectUsed = true;
		}
		//else if (param == "--defaults")
		//{
		//	if (defaultsUsed)		{ errorAndUsage("Duplicate argument."); }
		//	Global::filePathDefaults = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
		//	if (!success)			{ errorAndUsage("Duplicate argument."); }
		//	defaultsUsed = true;
		//}
		//else if (param == "--config")
		//{
		//	if (configUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::filePathConfig = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
		//	if (!success)			{ errorAndUsage("Duplicate argument."); }
		//	configUsed = true;
		//}
		//else if (param == "--environment")
		//{
		//	if (environmentUsed)	{ errorAndUsage("Duplicate argument."); }
		//	Global::filePathEnvironment = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
		//	if (!success)			{ errorAndUsage("Duplicate argument."); }
		//	environmentUsed = true;
		//}
		//else if (param == "--flags")
		//{
		//	if (flagsUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::filePathFlags = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
		//	if (!success)			{ errorAndUsage("--flags path is invalid."); }
		//	flagsUsed = true;
		//}
		else if (param == "--printflags")
		{
			if (printBoolFlagsUsed)	{ errorAndUsage("Duplicate argument."); }
			if (val == "true")
			{
				Global::printFlags = true;
			}
			else if (val == "false")
			{
				Global::printFlags = false;
			}
			else
			{
				errorAndUsage("--printflags value is invalid.");
			}
			printBoolFlagsUsed = true;
		}
		//else if (param == "--work")
		//{
		//	if (workUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::folderPathWorkspace = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
		//	if (!success)			{ errorAndUsage("--work path is invalid."); }
		//	workUsed = true;
		//}
		//else if (param == "--manifest")
		//{
		//	if (manifestUsed)		{ errorAndUsage("Duplicate argument."); }
		//	Global::localFilePathManifest = FileSystem::CanonicalPath(val);
		//	manifestUsed = true;
		//}
		//else if (param == "--proguard")
		//{
		//	if (proguardUsed)		{ errorAndUsage("Duplicate argument."); }
		//	Global::localFilePathProguard = FileSystem::CanonicalPath(val);
		//	proguardUsed = true;
		//}
		//else if (param == "--java")
		//{
		//	if (javaUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathJava = FileSystem::CanonicalPath(val);
		//	javaUsed = true;
		//}
		//else if (param == "--aidl")
		//{
		//	if (aidlUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathAIDL = FileSystem::CanonicalPath(val);
		//	aidlUsed = true;
		//}
		//else if (param == "--res")
		//{
		//	if (resUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathRes = FileSystem::CanonicalPath(val);
		//	resUsed = true;
		//}
		//else if (param == "--assets")
		//{
		//	if (assetsUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathAssets = FileSystem::CanonicalPath(val);
		//	assetsUsed = true;
		//}
		//else if (param == "--libs")
		//{
		//	if (libsUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathLibs = FileSystem::CanonicalPath(val);
		//	libsUsed = true;
		//}
		//else if (param == "--jnilibs")
		//{
		//	if (jnilibsUsed)		{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathJniLibs = FileSystem::CanonicalPath(val);
		//	jnilibsUsed = true;
		//}
		//else if (param == "--native")
		//{
		//	if (nativeUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::localFolderPathNative = FileSystem::CanonicalPath(val);
		//	nativeUsed = true;
		//}
		//else if (param == "--preprocessApp")
		else if (param == "--preprocess")
		{
			if (preprocessAppUsed)	{ errorAndUsage("Duplicate argument."); }
			Global::varPreprocessApp = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
			if (!success)			{ errorAndUsage("--preprocess path is invalid."); }
			preprocessAppUsed = true;
		}
		//else if (param == "--preprocessParams")
		//{
		//	if (preprocessParamsUsed)	{ errorAndUsage("Duplicate argument."); }
		//	Global::varPreprocessParameters = val;
		//	preprocessParamsUsed = true;
		//}
		//else if (param == "--jniDefault")
		//{
		//	if (jniDefaultUsed)		{ errorAndUsage("Duplicate argument."); }
		//	Global::varJNILibsDefaultType = val;
		//	bool found = false;
		//	for (size_t i = 0; i < Global::varJNITypesCount; i++)
		//	{
		//		if (Global::varJNILibsDefaultType == Global::varJNITypes[i])
		//		{
		//			found = true;
		//			break;
		//		}
		//	}
		//	if (!found)
		//	{
		//		errorAndUsage("--jniDefault value is invalid.");
		//	}
		//	jniDefaultUsed = true;
		//}
		else if (param == "--zip")
		{
			if (zipUsed)	{ errorAndUsage("Duplicate argument."); }
			Global::varZipApp = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
			if (!success)			{ errorAndUsage("--zip path is invalid."); }
			zipUsed = true;
		}
		else if (param == "--rebuild")
		{
			if (alwaysRebuildUsed)	{ errorAndUsage("Duplicate argument."); }
			if (val == "true")
			{
				Global::alwaysRebuild = true;
			}
			else if (val == "false")
			{
				Global::alwaysRebuild = false;
			}
			else
			{
				errorAndUsage("--rebuild value is invalid.");
			}
			alwaysRebuildUsed = true;
		}
		//else if (param == "--externals")
		//{
		//	if (externalsUsed)			{ errorAndUsage("Duplicate argument."); }
		//	Global::filePathExternals = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
		//	if (!success)			{ errorAndUsage("--externals path is invalid."); }
		//	externalsUsed = true;
		//}
		//else if (param == "--defines")
		//{
		//	if (definesUsed)			{ errorAndUsage("Duplicate argument."); }
		//	std::stringstream ss(val);
		//	std::string token;
		//	while (std::getline(ss, token, ';'))
		//	{
		//		if (token != "")
		//		{
		//			Global::generalPreprocessIncludes += " -include " + putInQuotes(FileSystem::FullPath(FileSystem::CanonicalPath(token), success));
		//			if (!success)			{ errorAndUsage("--defines \"" + token +"\" path is invalid."); }
		//		}
		//	}
		//	definesUsed = true;
		//}
		//else if (param == "--macros")
		//{
		//	if (macrosUsed)			{ errorAndUsage("Duplicate argument."); }
		//	std::stringstream ss(val);
		//	std::string token;
		//	while (std::getline(ss, token, ';'))
		//	{
		//		if (token != "")
		//		{
		//			Global::generalPreprocessMacros += " -imacros " + putInQuotes(FileSystem::FullPath(FileSystem::CanonicalPath(token), success));
		//			if (!success)			{ errorAndUsage("--macros \"" + token + "\" path is invalid."); }
		//		}
		//	}
		//	macrosUsed = true;
		//}
		else if (param == "--hideWarnings")
		{
			if (hideWarningsUsed)	{ errorAndUsage("Duplicate argument."); }
			if (val == "true")
			{
				Global::hideWarnings= true;
			}
			else if (val == "false")
			{
				Global::hideWarnings = false;
			}
			else
			{
				errorAndUsage("--hideWarnings value is invalid.");
			}
			printBoolFlagsUsed = true;
		}
		else if (param == "--architecture")
		{
			if (architectureUsed)	{ errorAndUsage("Duplicate argument."); }
			//Global::filePathJava2Android = FileSystem::FullPath(FileSystem::CanonicalPath(val), success);
			//if (!success)			{ errorAndUsage("--java2android path is invalid."); }
			std::stringstream ss(val);
			std::string token;
			while (std::getline(ss, token, ';'))
			{
				if (token != "")
				{
					int index = 0;
					for (; index < Global::varJNITypesCount; index++)
					{
						if (token == Global::varJNITypes[index])
						{
							break;
						}
					}
					if (index < Global::varJNITypesCount)
					{
						Global::architectureIndexList.push_back(index);
					}
					else
					{
						errorAndUsage("--architecture: " + putInQuotes(token) + " is not a valid architecture.");
					}
				}
			}
			architectureUsed = true;
		}
		else
		{
			//errorFunction("Incorrect usage.");
			errorAndUsage("Incorrect usage.");
		}
	}
}

void processBoolFlagsFile(std::string filename)
{
	std::ifstream in(filename);
	if (!in.is_open())
	{
		errorFunction("Failed to open file \"" + filename + "\"");
	}

	std::string line;
	std::string content = "";
	while (std::getline(in, line))
	{
		content += line + "\n";
	}
	
	size_t crtpos = 0;
	size_t begpos = 0;
	size_t endpos = 0;
	std::vector<std::pair<size_t, size_t>> limits;
	std::string delimiter = "";
	
	while ((begpos = content.find("//", crtpos)) != std::string::npos)
	{
		if (begpos + 2 < content.size())
		{
			endpos = content.find("\n", begpos + 2);
			if (endpos != std::string::npos)
			{
				crtpos = endpos;
				limits.push_back(std::make_pair(begpos, endpos - begpos));
			}
			else
			{
				crtpos = content.size();
				limits.push_back(std::make_pair(begpos, endpos - begpos));
			}
		}
		else
		{
			crtpos = content.size();
			limits.push_back(std::make_pair(begpos, endpos - begpos));
		}
	}
	for (int i = limits.size() - 1; i >= 0; i--)
	{
		content.replace(limits[i].first, limits[i].second, "");
	}

	crtpos = 0;
	begpos = 0;
	endpos = 0;
	limits.clear();

	while ((begpos = content.find("/*", crtpos)) != std::string::npos)
	{
		size_t tmppos = content.find("*/", crtpos);
		if (tmppos < begpos)
		{
			errorFunction("In file \"" + filename + "\". \"*/\" comment is closed but never opened.");
		}
		if (begpos + 2 < content.size())
		{
			endpos = content.find("*/", begpos + 2);
			if (endpos != std::string::npos)
			{
				endpos += 2;
				crtpos = endpos;
				limits.push_back(std::make_pair(begpos, endpos - begpos));
			}
			else
			{
				errorFunction("In file \"" + filename + "\". \"/*\" comment is opened but never closed.");
			}
		}
		else
		{
			errorFunction("In file \"" + filename + "\". \"/*\" comment is opened but never closed.");
		}
	}
	for (int i = limits.size() - 1; i >= 0; i--)
	{
		std::string chunk = content.substr(limits[i].first, limits[i].second);
		if (chunk.find("\n") != std::string::npos)
		{
			delimiter = "\n";
		}
		else
		{
			delimiter = " ";
		}
		content.replace(limits[i].first, limits[i].second, delimiter);
	}

	
	std::stringstream ssin;
	ssin.str(content);
	
	std::map<std::string, bool> filedefs;

	while (std::getline(ssin, line))
	{
		if (line.size() == 0)
		{
			continue;
		}
		//if (line.size() > 2 && line[0] == '/' && line[1] == '/')
		//{
		//	continue;
		//}
		std::stringstream ss;
		ss << line;
		std::string define = "";
		std::string name = "";
		std::string value = "";
		ss >> define;
		define = toLower(define);

		if (define == "")
		{
			continue;
		}

		if (define != "#define")
		{
			if (define == "#")
			{
				ss >> define;
				if (define != "define")
				{
					errorFunction("Invalid line in file \"" + filename + "\" --- \"" + line + "\"");
				}
			}
			else
			{
				errorFunction("Invalid line in file \"" + filename + "\" --- \"" + line + "\"");
			}
		}

		ss >> name;
		name = toLower(name);

		//if (Global::boolFlags.find(name) != Global::boolFlags.end())
		if (filedefs.find(name) != filedefs.end())
		{
			//errorFunction("Flag redefinition in same file - \"" + name + "\"");
			//std::cout << "  Warning: \"" + name + "\" flag redefined in same file. - \"" + filename +"\"" << std::endl;
			warningFunction(putInQuotes(name) + " flag redefined in same file. - " + putInQuotes(filename));
		}

		ss >> value;
		value = toLower(value);

		// crop whitespaces and paranthesis ////
		//

		bool changed = true;
		while (changed)
		{
			changed = false;
			size_t posbeg = 0;
			size_t posend = value.size() - 1;
			while (posbeg <= posend && (value[posbeg] == ' ' || value[posbeg] == '\t'))
			{
				changed = true;
				posbeg++;
			}
			if (posbeg > posend)
			{
				value = "";
				break;
			}
			while (posend > posbeg && (value[posend] == ' ' || value[posend] == '\t'))
			{
				changed = true;
				posend--;
			}
			while (posend > posbeg && value[posbeg] == '(' && value[posend] == ')')
			{
				changed = true;
				posbeg++;
				posend--;
			}
			if (posbeg > posend)
			{
				value = "";
				break;
			}
			if (changed)
			{
				value = value.substr(posbeg, posend + 1 - posbeg);
			}
		}

		//std::cout << putInQuotes(name) << "=" << putInQuotes(value) << std::endl;
		
		//
		////////////////////////////////////////

		if (value == "true" || value == "1")
		{
			//Global::boolFlags.insert(std::make_pair(name, true));
			Global::boolFlags[name] = true;
			filedefs.insert(std::make_pair(name, true));
		}
		else if (value == "false" || value == "0")
		{
			//Global::boolFlags.insert(std::make_pair(name, false));
			Global::boolFlags[name] = false;
			filedefs.insert(std::make_pair(name, false));
		}
		else
		{
			//errorFunction("Invalid value for bool flag \"" + name + "\". Must be \"true\", \"false\", 1 or 0");
			//not a bool flag... ignore
		}
	}
}

void printBoolFlags()
{
	std::cout << "Flags:" << std::endl;
	for (std::map<std::string, bool>::iterator i = Global::boolFlags.begin(); i != Global::boolFlags.end(); i++)
	{
		std::cout << i->first << " = " << (i->second ? "true" : "false") << std::endl;
	}
}

int main(int argc, char* argv[])
{
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//std::vector<std::string> package;
	//std::string text = "	 package		 asd . // qaz \n		qwe. /*   wsx     \n      edc \n	*/  zxc		;	";
	//std::cout << putInQuotes(text) << std::endl;

	//extractPackage(text, package);

	//for (int i = 0; i < package.size(); i++)
	//{
	//	std::cout << putInQuotes(package[i]) << std::endl;
	//}

	//std::cout << "..." << std::endl;

	//extractPackageFromFile("D:\\SVN\\gllegacy_AF_Dev\\Externals\\in_app_purchase\\project\\android\\crm\\shops\\google_play_v3\\libs\\com\\android\\vending\\billing\\IInAppBillingService.aidl", package);

	//for (int i = 0; i < package.size(); i++)
	//{
	//	std::cout << putInQuotes(package[i]) << std::endl;
	//}
	//std::cout << "..." << std::endl;
	//getchar();
	//return EXIT_SUCCESS;
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//bool testexists = false;
	//bool testsuccess = false;
	//std::string str1 = FileSystem::GetEnvVar("APP_PACKAGE", testexists);
	//if (testexists) std::cout << "%APP_PACKAGE% exists" << std::endl;
	//if (!testexists) std::cout << "%APP_PACKAGE% does not exist" << std::endl;
	//std::string str2 = FileSystem::ReplaceEnvVars("<:APP_PACKAGE:>.MySubDir.MyClass", testsuccess);
	//if (testsuccess) std::cout << "everything is awesome " << std::endl;
	//if (!testsuccess) std::cout << "something went wrong" << std::endl;
	//std::string str3 = FileSystem::ReplaceEnvVarsAndCanonicalize("<:APP_PACKAGE:>/pam/pam/..\\this\\is/so//silly", testsuccess);
	//if (testsuccess) std::cout << "everything is awesome " << std::endl;
	//if (!testsuccess) std::cout << "something went wrong" << std::endl;

	//std::cout << "==================================" << std::endl;
	//std::cout << putInQuotes(str1) << std::endl;
	//std::cout << putInQuotes(str2) << std::endl;
	//std::cout << putInQuotes(str3) << std::endl;
	//std::cout << "==================================" << std::endl;

	//return EXIT_SUCCESS;
	////system("pause");

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//std::string teststr = "#define  (	1 ) ";
	//std::stringstream ss(teststr);
	//std::string str1;
	//std::string str2;
	//std::string str3;
	//ss >> str1;
	////std::getline(ss, str1, ' ');
	//std::getline(ss, str2);
	////std::getline(ss, str3, ' ');
	//std::cout << putInQuotes(str1) << std::endl;
	//std::cout << putInQuotes(str2) << std::endl;
	//std::cout << putInQuotes(str3) << std::endl;

	//std::string unclean = str2;
	//bool changed = true;
	//while (changed)
	//{
	//	changed = false;
	//	size_t posbeg = 0;
	//	size_t posend = unclean.size() - 1;
	//	while (posbeg <= posend && (unclean[posbeg] == ' ' || unclean[posbeg] == '\t') )
	//	{
	//		std::cout << "++" << std::endl;
	//		changed = true;
	//		posbeg++;
	//	}
	//	if (posbeg > posend)
	//	{
	//		unclean = "";
	//		break;
	//	}
	//	while (posend > posbeg && (unclean[posend] == ' ' || unclean[posend] == '\t'))
	//	{
	//		std::cout << "--" << std::endl;
	//		changed = true;
	//		posend--;
	//	}
	//	while (posend > posbeg && unclean[posbeg] == '(' && unclean[posend] == ')')
	//	{
	//		std::cout << "++--" << std::endl;
	//		changed = true;
	//		posbeg++;
	//		posend--;
	//	}
	//	if (posbeg > posend)
	//	{
	//		unclean = "";
	//		break;
	//	}
	//	if (changed)
	//	{
	//		unclean = unclean.substr(posbeg, posend + 1 - posbeg);
	//	}
	//}
	//std::string clean = unclean;

	//std::cout << putInQuotes(clean) << std::endl;

	//std::cout << "......" << std::endl;
	//getchar();

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	std::cout << std::endl;
	std::cout << "  STARTED the Java to Android process." << std::endl;
	std::cout << std::endl;

	bool errorOccurred = false;
	std::string errorMsg = "";

	Global::varRootDirectory = FileSystem::FolderPath(FileSystem::FullPath(FileSystem::CanonicalPath(argv[0])));

	// Check if console exists
	if (system(nullptr) == 0)
	{
		errorFunction("Console is not available");
	}
	
	// Set defaults

	//Global::filePathJava2Android = FileSystem::FullPath("java2android.xml");
	//Global::filePathDefaults = FileSystem::FullPath("defaults.xml");
	//Global::filePathConfig = FileSystem::FullPath("config.xml");
	//Global::filePathEnvironment = FileSystem::FullPath("environment.xml");
	//Global::filePathFlags = FileSystem::FullPath("flags.txt");

	//Global::folderPathWorkspace = FileSystem::FullPath("_work");
	//Global::localFilePathManifest = "AndroidManifest.xml";
	//Global::localFilePathProguard = "proguard.cfg";
	//Global::localFolderPathJava = "java";
	//Global::localFolderPathAIDL = "aidl";
	//Global::localFolderPathRes = "res";
	//Global::localFolderPathAssets = "assets";
	//Global::localFolderPathLibs = "libs";
	//Global::localFolderPathJniLibs = "jniLibs";
	//Global::localFolderPathNative = "native";

	//Global::varJNILibsDefaultType = "armeabi-v7a";

	Global::varPreprocessApp = FileSystem::FullPath("..\\..\\cpp\\cpp.exe");
	Global::varZipApp = FileSystem::FullPath("..\\..\\compress\\7za.exe");
	//Global::varPreprocessParameters = " -C -P";
	Global::varInitialPreprocessParameters = " -C -P";

	Global::alwaysRebuild = false;
	Global::printFlags = false;

	Global::varLocalFolderPathAndroidStudioProject = "";
	Global::hideWarnings = false;
	
	//Global::filePathExternals = FileSystem::FullPath("externals.txt");

	//Global::generalPreprocessIncludes = "";
	//Global::generalPreprocessMacros = "";

	//Global::compileSdkVersion = 19;
	//Global::buildToolsVersion = "19.1";
	//Global::targetSdKVersion = Global::compileSdkVersion;
	//Global::minSdkVersion = Global::compileSdkVersion;

	//Global::packageName = "com.at.me.bro";
	//
	//Global::keystorePath = "key.keystore";
	//Global::keystorePass = "123456";
	//Global::keyName = "myKey";
	//Global::keyPass = "123456";

	//Global::packagingOptions = "";


	// Process command line arguments
	
	processArguments(argc, argv);
	
	//dbg("filePathJava2Android: " + Global::filePathJava2Android);
	//dbg("filePathDefaults: " + Global::filePathDefaults);
	//dbg("filePathConfig: " + Global::filePathConfig);
	//dbg("filePathEnvironment: " + Global::filePathEnvironment);
	//dbg("filePathFlags: " + Global::filePathFlags);
	//dbg("folderPathWorkspace: " + Global::folderPathWorkspace);
	//dbg("localFilePathManifest: " + Global::localFilePathManifest);
	//dbg("localFilePathProguard: " + Global::localFilePathProguard);
	//dbg("localFolderPathJava: " + Global::localFolderPathJava);
	//dbg("localFolderPathAIDL: " + Global::localFolderPathAIDL);
	//dbg("localFolderPathRes: " + Global::localFolderPathRes);
	//dbg("localFolderPathAssets: " + Global::localFolderPathAssets);
	//dbg("localFolderPathLibs: " + Global::localFolderPathLibs);
	//dbg("localFolderPathJniLibs: " + Global::localFolderPathJniLibs);
	//dbg("localFolderPathNative: " + Global::localFolderPathNative);
	//dbg("varJNILibsDefaultType: " + Global::varJNILibsDefaultType);
	//dbg("varPreprocessApp: " + Global::varPreprocessApp);
	//dbg("varPreprocessParameters: " + Global::varPreprocessParameters);
	
	//Global::varJava2AndroidFolder = FileSystem::FolderPath(Global::filePathJava2Android);
	
	
	//// Process bool variables

	//if (Global::filePathFlags.size() > 0)
	//{
	//	processBoolFlagsFile(Global::filePathFlags);
	//}

	//if (Global::printFlags)
	//{
	//	printBoolFlags();
	//}


	//// Process java2android

	//ExpressionHandler::BoolTable(Global::boolFlags);

	//std::cout << "  Processing " << putInQuotes(Global::filePathJava2Android) << "." << std::endl;

	//XMLParser::Java2Android projectData;
	//// set new defaults
	//// ...

	//// get java2android.xml list .. in process args
	//// for each file ... process proj params
	//// for each file ... process libs params (maje sure not to have the same one twice.. maybe give a warning)
	//
	//XMLParser::ProcessJava2Android(Global::filePathJava2Android, projectData, errorOccurred, errorMsg);
	//if (errorOccurred)
	//{
	//	errorFunction(errorMsg);
	//}

	//std::cout << std::endl;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	//std::cout << "-------------------------------------------------------------------" << std::endl;
	//std::string varname = "APP_PACKAGE";
	//char* buffer = new char[1];
	//int size = 0;
	//std::cout << size << std::endl;
	//size = GetEnvironmentVariable(varname.c_str(), buffer, size);
	//std::cout << size << std::endl;
	//if (size == ERROR_ENVVAR_NOT_FOUND)
	//{
	//	std::cout << "var not found 1..." << std::endl;
	//}
	//else
	//{
	//	delete[] buffer;
	//	buffer = new char[size];
	//	std::cout << size << std::endl;
	//	size = GetEnvironmentVariable(varname.c_str(), buffer, size);
	//	std::cout << size << std::endl;
	//	if (size == ERROR_ENVVAR_NOT_FOUND)
	//	{
	//		std::cout << "var not found 2..." << std::endl;
	//	}
	//	else
	//	{
	//		std::string varvalue(buffer);
	//		std::cout << putInQuotes(varvalue) << std::endl;
	//	}
	//}
	//delete[] buffer;
	//std::cout << "-------------------------------------------------------------------" << std::endl;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Process java2android

	//XMLParser::Java2Android projectData;
	// set new defaults
	// ...

	if (Global::hideWarnings)
	{
		disableWarnings();
	}

	
	std::cout << "    Acquiring project parameters." << std::endl;
	std::cout << std::endl;

	for (int i = 0; i < Global::java2androidPathList.size(); i++)
	{
		std::cout << "      Looking in " << putInQuotes(Global::java2androidPathList[i]) << "." << std::endl;
		
		XMLParser::ProcessJava2AndroidProject(Global::java2androidPathList[i], projectData, errorOccurred, errorMsg);
		if (errorOccurred)
		{
			errorFunction(errorMsg);
		}

		//std::cout << "projectData.minSDK" << "::" << projectData.minSDK << std::endl;
		//std::cout << "projectData. targetSDK" << "::" << projectData.targetSDK << std::endl;
		//std::cout << "projectData.compileSDK" << "::" << projectData.compileSDK << std::endl;
		//std::cout << "projectData.buildTools[3]" << "::" << projectData.buildTools[0] << " " << projectData.buildTools[1] << " " << projectData.buildTools[2] << std::endl;
		//std::cout << "projectData.useMinSDK" << "::" << (projectData.useMinSDK ? "true" : "false") << std::endl;
		//std::cout << "projectData.useTargetSDK" << "::" << (projectData.useTargetSDK ? "true" : "false") << std::endl;
		//std::cout << "projectData.applicationID" << "::" << projectData.applicationID << std::endl;
		//std::cout << "projectData.signatureKeystorePath" << "::" << projectData.signatureKeystorePath << std::endl;
		//std::cout << "projectData.signatureKeystorePass" << "::" << projectData.signatureKeystorePass << std::endl;
		//std::cout << "projectData.signatureKeyName" << "::" << projectData.signatureKeyName << std::endl;
		//std::cout << "projectData.signatureKeyPass" << "::" << projectData.signatureKeyPass << std::endl;
		//std::cout << "projectData.defaultJniType" << "::" << projectData.defaultJniType << std::endl;
		//std::cout << "projectData.incrementalBuild" << "::" << (projectData.incrementalBuild ? "true" : "false") << std::endl;
		//std::cout << "projectData.packageOptions" << "::" << projectData.packageOptions << std::endl;
		//std::cout << "projectData.workspaceFolder" << "::" << projectData.workspaceFolder << std::endl;
		//std::cout << "projectData.workspaceManifest" << "::" << projectData.workspaceLocalManifest << std::endl;
		//std::cout << "projectData.workspaceProguard" << "::" << projectData.workspaceLocalProguard << std::endl;
		//std::cout << "projectData.workspaceJava" << "::" << projectData.workspaceLocalJava << std::endl;
		//std::cout << "projectData.workspaceAIDL" << "::" << projectData.workspaceLocalAIDL << std::endl;
		//std::cout << "projectData.workspaceRes" << "::" << projectData.workspaceLocalRes << std::endl;
		//std::cout << "projectData.workspaceAssets" << "::" << projectData.workspaceLocalAssets << std::endl;
		//std::cout << "projectData.workspaceLibs" << "::" << projectData.workspaceLocalLibs << std::endl;
		//std::cout << "projectData.workspaceJniLibs" << "::" << projectData.workspaceLocalJniLibs << std::endl;
		//for (int i = 0; i < projectData.includesList.size(); i++)
		//std::cout << "projectData.flagsPath" << "::" << projectData.flagsPathList[i] << std::endl;
		//for (int i = 0; i < projectData.includesList.size(); i++)
		//std::cout << "projectData.includesList" << "::" << projectData.includesList[i] << std::endl;
		//for (int i = 0; i < projectData.macrosList.size(); i++)
		//std::cout << "projectData.macrosList" << "::" << projectData.macrosList[i] << std::endl;
		//for (int i = 0; i < projectData.j2aPathsList.size(); i++)
		//for (int j = 0; j < projectData.j2aPathsList[i].size(); j++)
		//std::cout << "projectData.j2aPathsList" << "::" << projectData.j2aPathsList[i][j] << std::endl;

		std::cout << "      End." << std::endl;
		std::cout << std::endl;
	}

	std::cout << "    Done." << std::endl << std::endl;
	std::cout << std::endl;

	// Command line params have priority ///////////////////////////

	if (!Global::architectureIndexList.empty())
	{
		projectData.architectureTypeIndices.clear();
		for (std::vector<std::size_t>::const_iterator i = Global::architectureIndexList.begin(); i != Global::architectureIndexList.end(); i++)
		{
			projectData.architectureTypeIndices.push_back(*i);
		}
		projectData.useArchitectureTypeIndices = true;
	}

	////////////////////////////////////////////////////////////////

	//if not used
	//bool useMinSDK;					- don't use or include in build script
	//bool useTargetSDK;				- don't use or include in build script
	//bool useCompileSDK;				- must have... warning default is...
	//bool useBuildTools;				- must have... warning default is...
	//bool useApplicationID;			- teoretic o ia si din manifest... cred... dar poate e mai bine as existe.... idk ???????? /// nothing.. if not used don't use it and it will get it from xml... probably....
	//bool useSignatureKeystorePath;	- must have... warning default is...
	//bool useSignatureKeystorePass;	- must have... warning default is...
	//bool useSignatureKeyName;			- must have... warning default is...
	//bool useSignatureKeyPass;			- must have... warning default is...
	//bool useDefaultJniType;			- must have... warning default is...
	//bool useIncrementalBuild;			- not that big of a deal really... default is true... don't say anything.... ..... but actually use the value at some time... with force rebuild and stuff...
	//bool usePackageOptions;			- if empty is empty so it does not matter... say nothing
	//bool useWorkspaceFolder;			- must have... warning default is...
	//bool useWorkspaceLocalManifest;	- must have... warning default is...
	//bool useWorkspaceLocalProguard;	- optional... do not use or include in build scritp... warning default is... no warning
	//bool useWorkspaceLocalJava;		- optional... do not use or include in build scritp... warning default is... no warning
	//bool useWorkspaceLocalAIDL;		- optional... do not use or include in build scritp... warning default is... no warning
	//bool useWorkspaceLocalRes;		- optional... do not use or include in build scritp... warning default is... no warning
	//bool useWorkspaceLocalAssets;		- optional... do not use or include in build scritp... warning default is... no warning
	//bool useWorkspaceLocalLibs;		- optional... do not use or include in build scritp... warning default is... no warning
	//bool useWorkspaceLocalJniLibs;	- optional... do not use or include in build scritp... warning default is... no warning
	//bool useFlagsPathList;			- nothing
	//bool useIncludesList;				- nothing
	//bool useMacrosList;				- nothing
	//bool useJ2aPathsList;				- nothing

	bool anyDefaulstUsed = false;

//	if (!projectData.useCompileSDK)					{ anyDefaulstUsed = true;	std::cout << "  Warning: \"compileSdkVersion\" not set. The default is " << projectData.compileSDK << std::endl; }
//	if (!projectData.useBuildTools)					{ anyDefaulstUsed = true;	std::cout << "  Warning: \"buildToolsVersion\" not set. The default is \"" << projectData.buildTools[0] << "." << projectData.buildTools[1] << "." << projectData.buildTools[2] << "\"" << std::endl; }
////	if (!projectData.useApplicationID)				{ anyDefaulstUsed = true;	std::cout << "  Warning: \"applicationId\" not set. The default is " << projectData.applicationID << std::endl; }
//	if (!projectData.useSignatureKeystorePath)		{ anyDefaulstUsed = true;	std::cout << "  Warning: \"keystorepath\" not set. The default is " << putInQuotes(projectData.signatureKeystorePath) << std::endl; }
//	if (!projectData.useSignatureKeystorePass)		{ anyDefaulstUsed = true;	std::cout << "  Warning: \"keystorepass\" not set. The default is " << putInQuotes(projectData.signatureKeystorePass) << std::endl; }
//	if (!projectData.useSignatureKeyName)			{ anyDefaulstUsed = true;	std::cout << "  Warning: \"keyname\" not set. The default is " << putInQuotes(projectData.signatureKeyName) << std::endl; }
//	if (!projectData.useSignatureKeyPass)			{ anyDefaulstUsed = true;	std::cout << "  Warning: \"keypass\" not set. The default is " << putInQuotes(projectData.signatureKeyPass) << std::endl; }
////	if (!projectData.useDefaultJniType)				{ anyDefaulstUsed = true;	std::cout << "  Warning: \"defaultJniType\" not set. The default is " << putInQuotes(projectData.defaultJniType) << std::endl; }
//	if (!projectData.useWorkspaceFolder)			{ anyDefaulstUsed = true;	std::cout << "  Warning: \"workspace\" not set. The default is " << putInQuotes(projectData.workspaceFolder) << std::endl; }
//	if (!projectData.useWorkspaceLocalManifest)		{ anyDefaulstUsed = true;	std::cout << "  Warning: \"manifest\" not set. The default is " << putInQuotes(projectData.workspaceManifest) << std::endl; }
//	if (!projectData.useArchitectureTypeIndices)	{ anyDefaulstUsed = true;	std::cout << "  Warning: \"architectures\" not set. The default is " << putInQuotes(Global::varJNITypes[projectData.architectureTypeIndices[0]]) << std::endl; }

	std::stringstream ss("");
	ss << "\"buildToolsVersion\" not set. The default is \"" << projectData.buildTools[0] << "." << projectData.buildTools[1] << "." << projectData.buildTools[2] << "\"";
	std::string tmpstr = ss.str();

	if (!projectData.useCompileSDK)					{ anyDefaulstUsed = true;	warningFunction("\"compileSdkVersion\" not set. The default is " + projectData.compileSDK); }
	if (!projectData.useBuildTools)					{ anyDefaulstUsed = true;	warningFunction(tmpstr); }
//	if (!projectData.useApplicationID)				{ anyDefaulstUsed = true;	warningFunction("\"applicationId\" not set. The default is " + projectData.applicationID); }
	if (!projectData.useSignatureKeystorePath)		{ anyDefaulstUsed = true;	warningFunction("\"keystorepath\" not set. The default is " + putInQuotes(projectData.signatureKeystorePath)); }
	if (!projectData.useSignatureKeystorePass)		{ anyDefaulstUsed = true;	warningFunction("\"keystorepass\" not set. The default is " + putInQuotes(projectData.signatureKeystorePass)); }
	if (!projectData.useSignatureKeyName)			{ anyDefaulstUsed = true;	warningFunction("\"keyname\" not set. The default is " + putInQuotes(projectData.signatureKeyName)); }
	if (!projectData.useSignatureKeyPass)			{ anyDefaulstUsed = true;	warningFunction("\"keypass\" not set. The default is " + putInQuotes(projectData.signatureKeyPass)); }
//	if (!projectData.useDefaultJniType)				{ anyDefaulstUsed = true;	warningFunction("\"defaultJniType\" not set. The default is " + putInQuotes(projectData.defaultJniType)); }
	if (!projectData.useWorkspaceFolder)			{ anyDefaulstUsed = true;	warningFunction("\"workspace\" not set. The default is " + putInQuotes(projectData.workspaceFolder)); }
	if (!projectData.useWorkspaceLocalManifest)		{ anyDefaulstUsed = true;	warningFunction("\"manifest\" not set. The default is " + putInQuotes(projectData.workspaceManifest)); }
	if (!projectData.useArchitectureTypeIndices)	{ anyDefaulstUsed = true;	warningFunction("\"architectures\" not set. The default is " + putInQuotes(Global::varJNITypes[projectData.architectureTypeIndices[0]])); }
	
	if (anyDefaulstUsed)
	{
		std::cout << std::endl;
		std::cout << std::endl;
	}

	projectData.workspaceManifest = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalManifest);
	projectData.workspaceProguard = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalProguard);
	projectData.workspaceJava = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalJava);
	projectData.workspaceAIDL = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalAIDL);
	projectData.workspaceRes = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalRes);
	projectData.workspaceAssets = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalAssets);
	projectData.workspaceLibs = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalLibs);
	projectData.workspaceJniLibs = FileSystem::CombinePaths(projectData.workspaceFolder, projectData.workspaceLocalJniLibs);
	
	//std::cout << std::endl;
	//std::cout << putInQuotes(projectData.workspaceFolder) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalManifest) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalProguard) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalJava) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalAIDL) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalRes) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalAssets) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalLibs) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLocalJniLibs) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceManifest) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceProguard) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceJava) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceAIDL) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceRes) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceAssets) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceLibs) << std::endl;
	//std::cout << putInQuotes(projectData.workspaceJniLibs) << std::endl;
	//std::cout << std::endl;

	//system("pause");

	// Process bool variables

	std::cout << "    Filling up boolean flags table." << std::endl;
	std::cout << std::endl;

	//for (int i = 0; i < projectData.architectureTypeIndices.size(); i++)
	//{
	//	std::cout << Global::varJNITypes[projectData.architectureTypeIndices[i]] << std::endl;
	//}
	//std::cout << "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" << std::endl;

	for (int i = 0; i < Global::varJNITypesCount; i++)
	{
		Global::boolFlags[Global::varJNITypesBoolNames[i]] = false;
	}
	for (int i = 0; i < projectData.architectureTypeIndices.size(); i++)
	{
		Global::boolFlags[Global::varJNITypesBoolNames[projectData.architectureTypeIndices[i]]] = true;
	}

	for (int i = 0; i < projectData.flagsPathList.size(); i++)
	{
		std::cout << "      Looking in " << putInQuotes(projectData.flagsPathList[i]) << "." << std::endl;

		processBoolFlagsFile(projectData.flagsPathList[i]);

		std::cout << "      End." << std::endl;
		std::cout << std::endl;
	}

	std::cout << "    Done." << std::endl << std::endl;
	std::cout << std::endl;

	//system("pause");


	if (Global::printFlags)
	{
		printBoolFlags();
	}
	//system("pause");
	ExpressionHandler::BoolTable(Global::boolFlags);

	std::cout << "    Constructing additional libraries list." << std::endl;
	std::cout << std::endl;

	for (int i = 0; i < Global::java2androidPathList.size(); i++)
	{
		std::cout << "      Looking in " << putInQuotes(Global::java2androidPathList[i]) << "." << std::endl;

		XMLParser::ProcessJava2AndroidLibs(Global::java2androidPathList[i], projectData, errorOccurred, errorMsg);
		if (errorOccurred)
		{
			errorFunction(errorMsg);
		}

		std::cout << "      End." << std::endl;
		std::cout << std::endl;
	}

	std::cout << "    Done." << std::endl << std::endl;
	std::cout << std::endl;

	//std::cout << "projectData.minSDK" << "::" << projectData.minSDK << std::endl;
	//std::cout << "projectData. targetSDK" << "::" << projectData.targetSDK << std::endl;
	//std::cout << "projectData.compileSDK" << "::" << projectData.compileSDK << std::endl;
	//std::cout << "projectData.buildTools[3]" << "::" << projectData.buildTools[0] << " " << projectData.buildTools[1] << " " << projectData.buildTools[2] << std::endl;
	//std::cout << "projectData.useMinSDK" << "::" << (projectData.useMinSDK ? "true" : "false") << std::endl;
	//std::cout << "projectData.useTargetSDK" << "::" << (projectData.useTargetSDK ? "true" : "false") << std::endl;
	//std::cout << "projectData.applicationID" << "::" << projectData.applicationID << std::endl;
	//std::cout << "projectData.signatureKeystorePath" << "::" << projectData.signatureKeystorePath << std::endl;
	//std::cout << "projectData.signatureKeystorePass" << "::" << projectData.signatureKeystorePass << std::endl;
	//std::cout << "projectData.signatureKeyName" << "::" << projectData.signatureKeyName << std::endl;
	//std::cout << "projectData.signatureKeyPass" << "::" << projectData.signatureKeyPass << std::endl;
	//std::cout << "projectData.defaultJniType" << "::" << projectData.defaultJniType << std::endl;
	//std::cout << "projectData.incrementalBuild" << "::" << (projectData.incrementalBuild ? "true" : "false") << std::endl;
	//std::cout << "projectData.packageOptions" << "::" << projectData.packageOptions << std::endl;
	//std::cout << "projectData.workspaceFolder" << "::" << projectData.workspaceFolder << std::endl;
	//std::cout << "projectData.workspaceManifest" << "::" << projectData.workspaceLocalManifest << std::endl;
	//std::cout << "projectData.workspaceProguard" << "::" << projectData.workspaceLocalProguard << std::endl;
	//std::cout << "projectData.workspaceJava" << "::" << projectData.workspaceLocalJava << std::endl;
	//std::cout << "projectData.workspaceAIDL" << "::" << projectData.workspaceLocalAIDL << std::endl;
	//std::cout << "projectData.workspaceRes" << "::" << projectData.workspaceLocalRes << std::endl;
	//std::cout << "projectData.workspaceAssets" << "::" << projectData.workspaceLocalAssets << std::endl;
	//std::cout << "projectData.workspaceLibs" << "::" << projectData.workspaceLocalLibs << std::endl;
	//std::cout << "projectData.workspaceJniLibs" << "::" << projectData.workspaceLocalJniLibs << std::endl;
	//for (int i = 0; i < projectData.includesList.size(); i++)
	//std::cout << "projectData.flagsPath" << "::" << projectData.flagsPathList[i] << std::endl;
	//for (int i = 0; i < projectData.includesList.size(); i++)
	//std::cout << "projectData.includesList" << "::" << projectData.includesList[i] << std::endl;
	//for (int i = 0; i < projectData.macrosList.size(); i++)
	//std::cout << "projectData.macrosList" << "::" << projectData.macrosList[i] << std::endl;
	//for (int i = 0; i < projectData.j2aPathsList.size(); i++)
	//for (int j = 0; j < projectData.j2aPathsList[i].size(); j++)
	//std::cout << "projectData.j2aPathsList" << "::" << projectData.j2aPathsList[i][j] << std::endl;

	//system("pause");


	// Copy android studio template

	bool success = false;

	std::string globalFolderPathAndroidStudioTemplate = FileSystem::CombinePaths(Global::varRootDirectory, Global::constLocalFolderPathAndroidStudioTemplate);
	if (Global::varLocalFolderPathAndroidStudioProject == "")
	{
		Global::varLocalFolderPathAndroidStudioProject = Global::constLocalFolderPathAndroidStudioProject;
	}
	std::string globalFolderPathAndroidStudio = FileSystem::FullPath(FileSystem::CanonicalPath(Global::varLocalFolderPathAndroidStudioProject), success);
	if (!success) errorFunction("Invalid path \"" + globalFolderPathAndroidStudio + "\"");
	//std::cout << putInQuotes(globalFolderPathAndroidStudioTemplate) << std::endl;
	//std::cout << putInQuotes(globalFolderPathAndroidStudio) << std::endl;
	Global::varCopyDestinationFolder = globalFolderPathAndroidStudio;
	if (!FileSystem::FileExists(globalFolderPathAndroidStudio)) { FileSystem::CreateFolder(globalFolderPathAndroidStudio, success); if (!success) errorFunction("Failed to create folder \"" + globalFolderPathAndroidStudio + "\""); }
	Global::pathOffset = "";
	Global::varCheckedExtension = "";
	applyRercursively(globalFolderPathAndroidStudioTemplate, copyFile, createFolderIfNotExists);

	std::string globalFilePathAndroidStudioBuild = FileSystem::CombinePaths(globalFolderPathAndroidStudio, "app\\build.gradle");


	// prepare temp src folder

	Global::globalFolderPathSrcTemp = FileSystem::CombinePaths(Global::varRootDirectory, Global::constLocalFolderPathSrcTemp);
	if (!FileSystem::FileExists(Global::globalFolderPathSrcTemp)) { FileSystem::CreateFolder(Global::globalFolderPathSrcTemp, success); if (!success) errorFunction("Failed to create folder \"" + Global::globalFolderPathSrcTemp + "\""); }

	
	// Process j2a files
	
	std::cout << "    Processing additional libraries." << std::endl;
	std::cout << std::endl;

	Global::externalsAAR.clear();
	//std::string externalNames = "";
	for (int k = 0; k < projectData.j2aPathsList.size(); k++)
	for (std::vector<std::string>::iterator i = projectData.j2aPathsList[k].begin(); i != projectData.j2aPathsList[k].end(); i++)
	{
		bool success = false;
		Global::libs.clear();
		Global::foldersToDelete.clear();


		// parse

		//std::string j2aPath = FileSystem::CombinePaths(Global::varJava2AndroidFolder, *i);
		std::string j2aPath = FileSystem::CombinePaths(Global::java2androidFolderList[k], *i);
		Global::varJ2ARoot = FileSystem::FolderPath(j2aPath);
		std::cout << "      Processing " << putInQuotes(j2aPath) << "." << std::endl;
		
		XMLParser::J2AData data;
		//XMLParser::ProcessJ2A(j2aPath, projectData.defaultJniType, data, errorOccurred, errorMsg);
		XMLParser::ProcessJ2A(j2aPath, XMLParser::Java2Android::unspecifiedJniType, data, errorOccurred, errorMsg);
		if (errorOccurred)
		{
			errorFunction(errorMsg, EXIT_FAILURE);
		}

		// append to externals

		if (data.buildType == XMLParser::J2AData::BuildType::AndroidLibrary)
		{
			//externalNames += data.buildArtefact + "\n";
			Global::externalsAAR.push_back(data.buildArtefact);
		}

		// process
		
		if (data.buildType == XMLParser::J2AData::BuildType::Copy)
		{
			//Global::varTmpFolderPathWorkspace = Global::folderPathWorkspace;
			//Global::varTmpLocalFilePathManifest = Global::localFilePathManifest;
			//Global::varTmpLocalFilePathProguard = Global::localFilePathProguard;
			//Global::varTmpLocalFolderPathJava = Global::localFolderPathJava;
			//Global::varTmpLocalFolderPathAIDL = Global::localFolderPathAIDL;
			//Global::varTmpLocalFolderPathRes = Global::localFolderPathRes;
			//Global::varTmpLocalFolderPathAssets = Global::localFolderPathAssets;
			//Global::varTmpLocalFolderPathLibs = Global::localFolderPathLibs;
			//Global::varTmpLocalFolderPathJniLibs = Global::localFolderPathJniLibs;
			//Global::varTmpLocalFolderPathNative = Global::localFolderPathNative;

			Global::varTmpFolderPathWorkspace = projectData.workspaceFolder;
			Global::varTmpLocalFilePathManifest = projectData.workspaceLocalManifest;
			Global::varTmpLocalFilePathProguard = projectData.workspaceLocalProguard;
			Global::varTmpLocalFolderPathJava = projectData.workspaceLocalJava;
			Global::varTmpLocalFolderPathAIDL = projectData.workspaceLocalAIDL;
			Global::varTmpLocalFolderPathRes = projectData.workspaceLocalRes;
			Global::varTmpLocalFolderPathAssets = projectData.workspaceLocalAssets;
			Global::varTmpLocalFolderPathLibs = projectData.workspaceLocalLibs;
			Global::varTmpLocalFolderPathJniLibs = projectData.workspaceLocalJniLibs;
			//Global::varTmpLocalFolderPathNative = Global::localFolderPathNative;
		}
		else
		{
			Global::varTmpFolderPathWorkspace = "TempWorkspace";
			Global::varTmpFolderPathWorkspace = FileSystem::CombinePaths(Global::varRootDirectory, Global::varTmpFolderPathWorkspace);
			Global::varTmpLocalFilePathManifest = "AndroidManifest.xml";
			Global::varTmpLocalFilePathProguard = "proguard.cfg";
			Global::varTmpLocalFolderPathJava = "java";
			Global::varTmpLocalFolderPathAIDL = "aidl";
			Global::varTmpLocalFolderPathRes = "res";
			Global::varTmpLocalFolderPathAssets = "assets";
			Global::varTmpLocalFolderPathLibs = "libs";
			Global::varTmpLocalFolderPathJniLibs = "jniLibs";
			//Global::varTmpLocalFolderPathNative = "native";
		}

		Global::varTmpFilePathManifest = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFilePathManifest);
		Global::varTmpFilePathProguard = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFilePathProguard);
		Global::varTmpFolderPathJava = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathJava);
		Global::varTmpFolderPathAIDL = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathAIDL);
		Global::varTmpFolderPathRes = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathRes);
		Global::varTmpFolderPathAssets = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathAssets);
		Global::varTmpFolderPathLibs = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathLibs);
		Global::varTmpFolderPathJniLibs = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathJniLibs);
		//Global::varTmpFolderPathNative = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, Global::varTmpLocalFolderPathNative);

		
		
		if (!FileSystem::FileExists(Global::varTmpFolderPathWorkspace) && true) { FileSystem::CreateFolder(Global::varTmpFolderPathWorkspace, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathWorkspace + "\""); }
		if (!FileSystem::FileExists(Global::varTmpFolderPathJava) && data.useDirs[XMLParser::J2AData::Directories::Java]) { FileSystem::CreateFolder(Global::varTmpFolderPathJava, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathJava + "\""); }
		if (!FileSystem::FileExists(Global::varTmpFolderPathAIDL) && data.useDirs[XMLParser::J2AData::Directories::AIDL]) { FileSystem::CreateFolder(Global::varTmpFolderPathAIDL, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathAIDL + "\""); }
		if (!FileSystem::FileExists(Global::varTmpFolderPathRes) && data.useDirs[XMLParser::J2AData::Directories::Res]) { FileSystem::CreateFolder(Global::varTmpFolderPathRes, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathRes + "\""); }
		if (!FileSystem::FileExists(Global::varTmpFolderPathAssets) && data.useDirs[XMLParser::J2AData::Directories::Assets]) { FileSystem::CreateFolder(Global::varTmpFolderPathAssets, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathAssets + "\""); }
		if (!FileSystem::FileExists(Global::varTmpFolderPathLibs) && data.useDirs[XMLParser::J2AData::Directories::Libs]) { FileSystem::CreateFolder(Global::varTmpFolderPathLibs, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathLibs + "\""); }
		if (!FileSystem::FileExists(Global::varTmpFolderPathJniLibs) && data.useDirs[XMLParser::J2AData::Directories::JniLibs]) { FileSystem::CreateFolder(Global::varTmpFolderPathJniLibs, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathJniLibs + "\""); }
		//if (!FileSystem::FileExists(Global::varTmpFolderPathNative) && data.useDirs[XMLParser::J2AData::Directories::Native]) { FileSystem::CreateFolder(Global::varTmpFolderPathNative, success); if (!success) errorFunction("Failed to create folder \"" + Global::varTmpFolderPathNative + "\""); }

		if (data.buildType != XMLParser::J2AData::BuildType::Copy)
		{
			if (!FileSystem::FileExists(projectData.workspaceFolder) && true) { FileSystem::CreateFolder(projectData.workspaceFolder, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceFolder + "\""); }
			if (!FileSystem::FileExists(projectData.workspaceJava) && data.useDirs[XMLParser::J2AData::Directories::Java]) { FileSystem::CreateFolder(projectData.workspaceJava, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceJava + "\""); }
			if (!FileSystem::FileExists(projectData.workspaceAIDL) && data.useDirs[XMLParser::J2AData::Directories::AIDL]) { FileSystem::CreateFolder(projectData.workspaceAIDL, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceAIDL + "\""); }
			if (!FileSystem::FileExists(projectData.workspaceRes) && data.useDirs[XMLParser::J2AData::Directories::Res]) { FileSystem::CreateFolder(projectData.workspaceRes, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceRes + "\""); }
			if (!FileSystem::FileExists(projectData.workspaceAssets) && data.useDirs[XMLParser::J2AData::Directories::Assets]) { FileSystem::CreateFolder(projectData.workspaceAssets, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceAssets + "\""); }
			if (!FileSystem::FileExists(projectData.workspaceLibs) && data.useDirs[XMLParser::J2AData::Directories::Libs]) { FileSystem::CreateFolder(projectData.workspaceLibs, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceLibs + "\""); }
			if (!FileSystem::FileExists(projectData.workspaceJniLibs) && data.useDirs[XMLParser::J2AData::Directories::JniLibs]) { FileSystem::CreateFolder(projectData.workspaceJniLibs, success); if (!success) errorFunction("Failed to create folder \"" + projectData.workspaceJniLibs + "\""); }

			//if (!FileSystem::FileExists(Global::folderPathWorkspace) && true) { FileSystem::CreateFolder(Global::folderPathWorkspace, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathWorkspace + "\""); }
			//if (!FileSystem::FileExists(Global::folderPathJava) && data.useDirs[XMLParser::J2AData::Directories::Java]) { FileSystem::CreateFolder(Global::folderPathJava, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathJava + "\""); }
			//if (!FileSystem::FileExists(Global::folderPathAIDL) && data.useDirs[XMLParser::J2AData::Directories::AIDL]) { FileSystem::CreateFolder(Global::folderPathAIDL, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathAIDL + "\""); }
			//if (!FileSystem::FileExists(Global::folderPathRes) && data.useDirs[XMLParser::J2AData::Directories::Res]) { FileSystem::CreateFolder(Global::folderPathRes, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathRes + "\""); }
			//if (!FileSystem::FileExists(Global::folderPathAssets) && data.useDirs[XMLParser::J2AData::Directories::Assets]) { FileSystem::CreateFolder(Global::folderPathAssets, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathAssets + "\""); }
			//if (!FileSystem::FileExists(Global::folderPathLibs) && data.useDirs[XMLParser::J2AData::Directories::Libs]) { FileSystem::CreateFolder(Global::folderPathLibs, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathLibs + "\""); }
			//if (!FileSystem::FileExists(Global::folderPathJniLibs) && data.useDirs[XMLParser::J2AData::Directories::JniLibs]) { FileSystem::CreateFolder(Global::folderPathJniLibs, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathJniLibs + "\""); }
			////if (!FileSystem::FileExists(Global::folderPathNative) && data.useDirs[XMLParser::J2AData::Directories::Native]) { FileSystem::CreateFolder(Global::folderPathNative, success); if (!success) errorFunction("Failed to create folder \"" + Global::folderPathNative + "\""); }
		}

		// copy files over and preprocess java files
		
		CopyFilesToWorkspace(data);
		
		if (data.buildType != XMLParser::J2AData::BuildType::Copy)
		{
			// check if artefact already exists
			
			std::string artefact = "";
			std::string artefactDebug = "";
			std::string artefactRelease = "";
			std::string extension = "";
			switch (data.buildType)
			{
			case XMLParser::J2AData::BuildType::JavaLibrary:
			{
				;
				artefact = "build\\libs\\gradle.jar";
				extension = "jar";
			}
			break;
			case XMLParser::J2AData::BuildType::AndroidLibrary:
			{
				;
				//artefact = "build\\outputs\\aar\\gradle.aar";
				artefactDebug = "build\\outputs\\aar\\gradle-debug.aar";
				artefactRelease = "build\\outputs\\aar\\gradle-release.aar";
				extension = "aar";
			}
			break;
			//case XMLParser::J2AData::BuildType::AndroidApplication:
			//{
			//	;
			//	artefact = "build\\outputs\\apk\\gradle.apk";
			//	//artefact = "build\\outputs\\apk\\gradle-release.apk"; //???
			//	//one more reason to not support APK
			//	//extension = "apk";
			//}
			break;
			}
			std::string artefactDestination = FileSystem::CombinePaths(projectData.workspaceLibs, data.buildArtefact + "." + extension);
			std::string artefactDestinationDebug = FileSystem::CombinePaths(projectData.workspaceLibs, data.buildArtefact + "-debug." + extension);
			std::string artefactDestinationRelease = FileSystem::CombinePaths(projectData.workspaceLibs, data.buildArtefact + "-release." + extension);
			std::string artefactExternalsFolder = FileSystem::CombinePaths(globalFolderPathAndroidStudio, "app\\build\\java2androidExternals");
			std::string artefactBackup = FileSystem::CombinePaths(artefactExternalsFolder, data.buildArtefact + "." + extension);
			std::string artefactBackupDebug = FileSystem::CombinePaths(artefactExternalsFolder, data.buildArtefact + "-debug." + extension);
			std::string artefactBackupRelease = FileSystem::CombinePaths(artefactExternalsFolder, data.buildArtefact + "-release." + extension);
			
			//e b r a
			//0 0 0 1
			//1 0 0 0
			//0 1 0 0
			//1 1 0 0
			//0 0 1 1
			//1 0 1 1
			//0 1 1 1
			//1 1 1 1
			//
			//if (!exists and backup)
			//	copy8
			//else if (!exists || rebuild)
			//	rebuild

			if (artefact != "" && !FileSystem::FileExists(artefactDestination) && FileSystem::FileExists(artefactBackup))
			{
				FileSystem::BinaryFileCopy(artefactBackup, artefactDestination, success);
				if (!success) errorFunction("Failed to copy file from \"" + artefactBackup + "\" to \"" + artefactDestination + "\"");
			}
			if (artefactDebug != "" && !FileSystem::FileExists(artefactDestinationDebug) && FileSystem::FileExists(artefactBackupDebug))
			{
				FileSystem::BinaryFileCopy(artefactBackupDebug, artefactDestinationDebug, success);
				if (!success) errorFunction("Failed to copy file from \"" + artefactBackupDebug + "\" to \"" + artefactDestinationDebug + "\"");
			}
			if (artefactRelease != "" && !FileSystem::FileExists(artefactDestinationRelease) && FileSystem::FileExists(artefactBackupRelease))
			{
				FileSystem::BinaryFileCopy(artefactBackupRelease, artefactDestinationRelease, success);
				if (!success) errorFunction("Failed to copy file from \"" + artefactBackupRelease + "\" to \"" + artefactDestinationRelease + "\"");
			}

			//if (!FileSystem::FileExists(artefactDestinationDebug) || !FileSystem::FileExists(artefactDestinationRelease) || Global::alwaysRebuild)
			if(artefact != "" && !FileSystem::FileExists(artefactDestination)
			|| artefactDebug != "" && !FileSystem::FileExists(artefactDestinationDebug)
			|| artefactRelease != "" && !FileSystem::FileExists(artefactDestinationRelease)
			|| Global::alwaysRebuild)
			{
				//copy gradleTemplate

				std::string globalFolderPathGradleTemplate = FileSystem::CombinePaths(Global::varRootDirectory, Global::constLocalFolderPathGradleTemplate);
				std::string globalFolderPathGradle = FileSystem::CombinePaths(Global::varTmpFolderPathWorkspace, "gradle");
				Global::varCopyDestinationFolder = globalFolderPathGradle;
				if (!FileSystem::FileExists(globalFolderPathGradle) && true) { FileSystem::CreateFolder(globalFolderPathGradle, success); if (!success) errorFunction("Failed to create folder \"" + globalFolderPathGradle + "\""); }
				Global::varCheckedExtension = "";
				applyRercursively(globalFolderPathGradleTemplate, copyFile, createFolderIfNotExists);

				std::string globalFilePathGradleBuild = FileSystem::CombinePaths(globalFolderPathGradle, "build.gradle");


				// generate build.gradle

				std::stringstream stream;
				generateGradleBuildScript(data, stream);
				FileSystem::WriteToFile(stream, globalFilePathGradleBuild, success);
				if (!success) errorFunction("Failed to write to \"" + globalFilePathGradleBuild + "\"");


				// build 

				std::string globalFilePathGradleWrapper = FileSystem::CombinePaths(globalFolderPathGradle, "assemble.bat");
				system(putInQuotes(globalFilePathGradleWrapper).c_str());

				
				// copy artefact

				artefact = FileSystem::CombinePaths(globalFolderPathGradle, artefact);
				artefactDebug = FileSystem::CombinePaths(globalFolderPathGradle, artefactDebug);
				artefactRelease = FileSystem::CombinePaths(globalFolderPathGradle, artefactRelease);

				switch (data.buildType)
				{
				case XMLParser::J2AData::BuildType::JavaLibrary:
				{
					if (FileSystem::FileExists(artefact))
					{
						bool success = false;
						if (!FileSystem::FileExists(artefactExternalsFolder) && true) { FileSystem::CreateFolder(artefactExternalsFolder, success); if (!success) errorFunction("Failed to create folder \"" + artefactExternalsFolder + "\""); }
						FileSystem::BinaryFileCopy(artefact, artefactBackup, success);
						if (!success)
						{
							errorFunction("Failed to copy build artefact");
						}
						FileSystem::BinaryFileCopy(artefact, artefactDestination, success);
						if (!success)
						{
							errorFunction("Failed to copy build artefact");
						}
					}
					else
					{
						errorFunction("Could not find build artefact");
					}
				}
				break;
				case XMLParser::J2AData::BuildType::AndroidLibrary:
				{
					if (FileSystem::FileExists(artefactDebug))
					{
						bool success = false;
						if (!FileSystem::FileExists(artefactExternalsFolder) && true) { FileSystem::CreateFolder(artefactExternalsFolder, success); if (!success) errorFunction("Failed to create folder \"" + artefactExternalsFolder + "\""); }
						FileSystem::BinaryFileCopy(artefactDebug, artefactBackupDebug, success);
						if (!success)
						{
							errorFunction("Failed to copy build artefact");
						}
						FileSystem::BinaryFileCopy(artefactDebug, artefactDestinationDebug, success);
						if (!success)
						{
							errorFunction("Failed to copy build artefact");
						}
					}
					else
					{
						errorFunction("Could not find build artefact");
					}
				
					if (FileSystem::FileExists(artefactRelease))
					{
						bool success = false;
						if (!FileSystem::FileExists(artefactExternalsFolder) && true) { FileSystem::CreateFolder(artefactExternalsFolder, success); if (!success) errorFunction("Failed to create folder \"" + artefactExternalsFolder + "\""); }
						FileSystem::BinaryFileCopy(artefactRelease, artefactBackupRelease, success);
						if (!success)
						{
							errorFunction("Failed to copy build artefact");
						}
						FileSystem::BinaryFileCopy(artefactRelease, artefactDestinationRelease, success);
						if (!success)
						{
							errorFunction("Failed to copy build artefact");
						}
					}
					else
					{
						errorFunction("Could not find build artefact");
					}
				}
				break;
				}

			}


			// copy libs

			if (data.useDirs[XMLParser::J2AData::Directories::Libs])
			{
				Global::varCopyDestinationFolder = projectData.workspaceLibs;
				Global::varCheckedExtension = "jar";
				applyRercursively(Global::varTmpFolderPathLibs, copyFile, createFolderIfNotExists);
			}


			// append proguard

			if (data.useProguard)
			{
				bool success = false;
				FileSystem::AppendCopyFile(Global::varTmpFilePathProguard, projectData.workspaceProguard, success);
				if (!success)
				{
					errorFunction("Failed to append proguard file");
				}
			}


			// cleanup tmp work

			Global::varCopyDestinationFolder = "";
			Global::varCheckedExtension = "";
			Global::foldersToDelete.clear();
			applyRercursively(Global::varTmpFolderPathWorkspace, deleteFile, saveFolderForRemoval);

			for (int i = Global::foldersToDelete.size() - 1; i >= 0; i--)
			{
				FileSystem::RemoveFolder(Global::foldersToDelete[i], success);
				if (!success) errorFunction("Failed to delete folder \"" + Global::foldersToDelete[i]);
			}
			FileSystem::RemoveFolder(Global::varTmpFolderPathWorkspace, success);
			if (!success) errorFunction("Failed to delete folder \"" + Global::varTmpFolderPathWorkspace);
		}

		std::cout << "      End." << std::endl << std::endl;
	}

	Global::varCopyDestinationFolder = "";
	Global::varCheckedExtension = "";
	Global::foldersToDelete.clear();
	applyRercursively(Global::globalFolderPathSrcTemp, deleteFile, saveFolderForRemoval);
	for (int i = Global::foldersToDelete.size() - 1; i >= 0; i--)
	{
		FileSystem::RemoveFolder(Global::foldersToDelete[i], success);
		if (!success) errorFunction("Failed to delete folder \"" + Global::foldersToDelete[i]);
	}
	FileSystem::RemoveFolder(Global::globalFolderPathSrcTemp, success);
	if (!success) errorFunction("Failed to delete folder \"" + Global::globalFolderPathSrcTemp);

	std::cout << "    Done." << std::endl;
	std::cout << std::endl;
	std::cout << std::endl;


	// extract classes from jar files

	std::cout << "    Removing unwanted files from workspace." << std::endl;
	std::cout << "    " << std::endl;

	std::string varExtractScript = "@echo off\n";
	for (std::map<std::string, std::set<std::string>>::iterator i = projectData.excludedJarClassPaths.begin(); i != projectData.excludedJarClassPaths.end(); i++)
	{
		std::string jarPath = FileSystem::CombinePaths(projectData.workspaceLibs, i->first);
		
		if (FileSystem::FileExists(jarPath))
		{
			varExtractScript += "echo       Extracting files from " + putInQuotes(jarPath) + ".\n";
			
			varExtractScript += putInQuotes(Global::varZipApp) + " d " + putInQuotes(jarPath);

			//7za.exe d google-play-services.jar com/google/ads/mediation/NetworkExtras.class com/google/ads/mediation/MediationServerParameters.class
			//7za.exe d "google-play-services.jar" "com/google/ads/mediation/NetworkExtras.class" "com/google/ads/mediation/MediationServerParameters.class"

			std::set<std::string>& classPaths = i->second;
			for (std::set<std::string>::iterator classpath = classPaths.begin(); classpath != classPaths.end(); classpath++)
			{
				varExtractScript += " " + putInQuotes(*classpath);
			}
			//command && echo success || echo fail

			varExtractScript += ">NUL || echo       Error: Operation failed.\n";
			varExtractScript += "echo       End.\n";
			varExtractScript += "echo.\n";
		}
		else
		{
			warningFunction(putInQuotes(jarPath) + " does not exist.");
		}
	}

	std::string extractbatname = FileSystem::CombinePaths(Global::varRootDirectory, "extractClasses.bat");
	std::ofstream out(extractbatname);
	out << varExtractScript;
	out.close();
	system(("call " + putInQuotes(extractbatname)).c_str());
	FileSystem::FileDelete(extractbatname.c_str(), success);
	if (!success) errorFunction("Failed to delete file \"" + extractbatname + "\"");

	std::cout << "    Done." << std::endl;
	std::cout << "    " << std::endl;

	
	
	//bool success = false;
	//FileSystem::WriteToFile(externalNames, Global::filePathExternals, success);
	//if (!success)
	//{
	//	errorFunction("Could not write to \"" + Global::filePathExternals + "\"");
	//}

	//bool success = false;
	//std::stringstream stream;
	//generateMainGradleBuildScript(projectData, stream);
	//FileSystem::WriteToFile(stream, "buildscripttest.ceva", success);
	//if (!success) errorFunction("Failed to write to \"buildscripttest.ceva\"");

	// generate build.gradle

	std::stringstream stream;
	generateMainGradleBuildScript(projectData, stream);
	FileSystem::WriteToFile(stream, globalFilePathAndroidStudioBuild, success);
	if (!success) errorFunction("Failed to write to \"" + globalFilePathAndroidStudioBuild + "\"");

	
	std::cout << "  FINISHED the Java to Android process." << std::endl;
	std::cout << std::endl;

	return EXIT_SUCCESS;
}
