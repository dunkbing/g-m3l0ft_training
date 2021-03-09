#pragma once

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

#include <Windows.h>
#include <Shlwapi.h>
#pragma comment (lib, "Shlwapi.lib")

class FileSystem
{
	FileSystem();
	FileSystem(const FileSystem&);
	FileSystem operator=(const FileSystem&);

	static bool NotUsed;
public:
	class FileFinder
	{
		WIN32_FIND_DATA FindFileData;
		HANDLE hFind;
	public:
		FileFinder(std::string path);
		bool isValid();
		std::string getCurrentFile();
		void findNextFile();
	};
	
	static void BinaryFileCopy(const std::string &in, const std::string &out, bool &success = NotUsed);
	static void AppendCopyFile(const std::string &in, const std::string &out, bool &success = NotUsed);
	static void WriteToFile(const std::string &in, const std::string &out, bool &success = NotUsed);
	static void WriteToFile(const std::stringstream &in, const std::string &out, bool &success = NotUsed);
	static void AppendToFile(const std::string &in, const std::string &out, bool &success = NotUsed);
	static void AppendToFile(const std::stringstream &in, const std::string &out, bool &success = NotUsed);
	static std::string CanonicalPath(const std::string &path);
	static std::string CanonicalToForwardSlashesPath(const std::string &path);
	static std::string CanonicalToBackslashesPath(const std::string &path);
	static std::string FullPath(const std::string &path, bool &success = NotUsed);
	static std::string FolderPath(const std::string &path, bool &success = NotUsed);
	static std::string FileName(const std::string &path, bool &success = NotUsed);
	static std::string FileRemoveExtension(const std::string &path, bool &success = NotUsed);
	static std::string FileExtension(const std::string &path, bool &success = NotUsed);
	static std::string CombinePaths(const std::string &first, const std::string &second);
	static bool FileExists(const std::string &path);
	static bool IsFolder(const std::string &path);
	static void CreateFolder(const std::string &path, bool &success = NotUsed);
	static void RemoveFolder(const std::string &path, bool &success = NotUsed);
	static void FileDelete(const std::string &path, bool &success = NotUsed);
	//technically nof "file" system related... should be moved
	static std::string GetEnvVar(const std::string &name, bool &exists = NotUsed);
	static std::string ReplaceEnvVars(const std::string &str, bool &success = NotUsed);
	static std::string ReplaceEnvVarsAndCanonicalize(const std::string &path, bool &success = NotUsed);
//	\ / : *? " < > | 
//	${}
//	:? ?:
//	:?: :?:
//	:<: :>:
//	:< >:
//	<: :>
//	<? ?>
//	:? *:
//	<* *>
//	-< >-
//	% %
};
