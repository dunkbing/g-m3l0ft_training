#include "FileSystem.h"

bool FileSystem::NotUsed = false;

FileSystem::FileFinder::FileFinder(std::string path)
{
	hFind = FindFirstFile(path.c_str(), &FindFileData);
}
bool FileSystem::FileFinder::isValid()
{
	return hFind != INVALID_HANDLE_VALUE;
}
std::string FileSystem::FileFinder::getCurrentFile()
{
	if (hFind != INVALID_HANDLE_VALUE)
	{
		return std::string(FindFileData.cFileName);
	}
	return "";
}
void FileSystem::FileFinder::findNextFile()
{
	if (hFind != INVALID_HANDLE_VALUE)
	{
		if (FindNextFile(hFind, &FindFileData) == 0)
		{
			FindClose(hFind);
			hFind = INVALID_HANDLE_VALUE;
		}
	}
}

void FileSystem::BinaryFileCopy(const std::string &in, const std::string &out, bool &success)
{
	//if (FileExists(out)) std::cout << "  Warning: \"" << out << "\" rewritten by \"" << in << "\"." << std::endl;

	std::ifstream fin(in, std::ifstream::binary);
	std::ofstream fout(out, std::ofstream::binary);

	if (fin && fout)
	{
		fin.seekg(0, fin.end);
		int length = fin.tellg();
		fin.seekg(0, fin.beg);

		char* buffer = new char[length];

		fin.read(buffer, length);

		if (!fin)
		{
			success = false;
		}

		fin.close();

		fout.write(buffer, length);

		delete[] buffer;
		success = true;
	}
	else
	{
		success = false;
	}
}

void FileSystem::AppendCopyFile(const std::string &in, const std::string &out, bool &success)
{
	std::ifstream fin(in, std::ios::in);
	std::ofstream fout(out, std::ios::app);

	if (fin && fout)
	{
		fout << fin.rdbuf();
		success = true;
	}
	else
	{
		success = false;
	}
}

void FileSystem::WriteToFile(const std::string &in, const std::string &out, bool &success)
{
	//if (FileExists(out)) std::cout << "  Warning: \"" << out << "\" rewritten by \"" << in << "\"." << std::endl;

	std::ofstream fout(out, std::ios::out);

	if (fout)
	{
		fout << in;
		success = true;
	}
	else
	{
		success = false;
	}
}

void FileSystem::WriteToFile(const std::stringstream &in, const std::string &out, bool &success)
{
	//if (FileExists(out)) std::cout << "  Warning: \"" << out << "\" rewritten by stream." << std::endl;

	std::ofstream fout(out, std::ios::out);

	if (fout)
	{
		fout << in.rdbuf();
		success = true;
	}
	else
	{
		success = false;
	}
}

void FileSystem::AppendToFile(const std::string &in, const std::string &out, bool &success)
{
	std::ofstream fout(out, std::ios::app);

	if (fout)
	{
		fout << in;
		success = true;
	}
	else
	{
		success = false;
	}
}

void FileSystem::AppendToFile(const std::stringstream &in, const std::string &out, bool &success)
{
	std::ofstream fout(out, std::ios::app);

	if (fout)
	{
		fout << in.rdbuf();
		success = true;
	}
	else
	{
		success = false;
	}
}

std::string FileSystem::CanonicalPath(const std::string &path)
{
	std::vector<std::string> names;
	std::stringstream ss(path);
	std::string token;
	while (std::getline(ss, token, '/'))
	{
		if (token == "")
		{
			names.push_back(token);
			continue;
		}
		std::string name;
		std::stringstream strs(token);
		while (std::getline(strs, name, '\\'))
		{
			names.push_back(name);
		}
	}
	std::vector<std::vector<std::string>::iterator> indices;
	for (std::vector<std::string>::iterator i = names.begin(); i != names.end(); i++)
	{
		if (*i == "" || *i == ".")
		{
			indices.push_back(i);
		}
		else if (*i == ".." && i != names.begin())
		{
			std::vector<std::string>::iterator j = i;
			int offset = 0;
			do
			{
				j--;
				bool marked = false;
				for (std::vector<std::vector<std::string>::iterator>::iterator k = indices.begin(); k != indices.end(); k++)
				{
					if (j == *k)
					{
						marked = true;
						offset++;
						break;
					}
				}
				if (!marked)
				{
					if (*j != "..")
					{
						indices.push_back(j);
						for (int o = 0; o < offset; o++)
						{
							std::vector<std::string>::iterator tmp = indices[indices.size() - o - 1];
							indices[indices.size() - o - 1] = indices[indices.size() - o - 2];
							indices[indices.size() - o - 2] = tmp;
						}
						indices.push_back(i);
					}
					break;
				}
			} while (j != names.begin());
		}
	}
	std::vector<std::vector<std::string>::iterator>::iterator k = indices.end();
	if (k != indices.begin())
	{
		do
		{
			k--;
			names.erase(*k);
		} while (k != indices.begin());
	}
	std::string result = "";
	for (std::vector<std::string>::iterator i = names.begin(); i != names.end(); i++)
	{
		result += *i + "\\";
	}
	result = result.substr(0, result.size() - 1);
	return result;
}

std::string FileSystem::CanonicalToForwardSlashesPath(const std::string &path)
{
	std::stringstream ss(path);
	std::string token;
	std::string result = "";
	while (std::getline(ss, token, '\\'))
	{
		if (token != "")
		{
			result = token;
			break;
		}
	}
	if (result == "")
	{
		return path;
	}
	while (std::getline(ss, token, '\\'))
	{
		if (token != "")
		{
			result += "/" + token;
		}
	}
	return result;
}
std::string FileSystem::CanonicalToBackslashesPath(const std::string &path)
{
	std::stringstream ss(path);
	std::string token;
	std::string result = "";
	while (std::getline(ss, token, '\\'))
	{
		if (token != "")
		{
			result = token;
			break;
		}
	}
	if (result == "")
	{
		return path;
	}
	while (std::getline(ss, token, '\\'))
	{
		if (token != "")
		{
			result += "\\" + token;
		}
	}
	return result;
}


std::string FileSystem::FullPath(const std::string &path, bool &success)
{
	DWORD buffsize = 1;

	DWORD  retval = 0;
	TCHAR  *buffer = new TCHAR[buffsize];
	buffer[0] = '\0';
	TCHAR** lppPart = { NULL };

	retval = GetFullPathName(path.c_str(), buffsize, buffer, lppPart);
	delete[] buffer;
	buffsize = retval;
	buffer = new TCHAR[buffsize];
	buffer[retval - 1] = '\0';
	retval = GetFullPathName(path.c_str(), buffsize, buffer, lppPart);
	DWORD WINAPI GetFullPathName(
		_In_   LPCTSTR lpFileName,
		_In_   DWORD nBufferLength,
		_Out_  LPTSTR lpBuffer,
		_Out_  LPTSTR *lpFilePart
		);
	if (retval == 0)
	{
		delete[] buffer;
		
		success = false;
		return "";
	}
	else
	{
		if (buffer[retval - 1] == '\\')
		{
			buffer[retval - 1] = '\0';
		}
		std::string result(buffer);

		delete[] buffer;

		success = true;
		return result;
	}
}

std::string FileSystem::FolderPath(const std::string &path, bool &success)
{
	char* buffer = new char[path.size() + 1];
	char* b = buffer;
	for (std::string::const_iterator c = path.begin(); c != path.end(); c++)
	{
		*b++ = *c;
	}
	*b++ = '\0';

	BOOL wasRemoved = PathRemoveFileSpec(buffer);

	std::string result(buffer);

	delete[] buffer;

	success = wasRemoved == TRUE;
	return result;
}

std::string FileSystem::FileName(const std::string &path, bool &success)
{
	//PathFindFileName
	size_t posbackslash = path.rfind("\\", path.size());
	if (posbackslash == path.npos)
	{
		success = true;
		return path;
	}
	success = true;
	return path.substr(posbackslash + 1, path.size() - posbackslash - 1);
}

std::string FileSystem::FileRemoveExtension(const std::string &path, bool &success)
{
	//PathFindExtension 
	std::string name = FileName(path);
	size_t posdot = path.rfind(".", path.size());
	if (posdot == path.npos)
	{
		success = false;
		return name;
	}
	name = path.substr(0, posdot);
	return name;
}

std::string FileSystem::FileExtension(const std::string &path, bool &success)
{
	//PathFindExtension 
	std::string name = FileName(path);
	size_t posdot = path.rfind(".", path.size());
	if (posdot == path.npos)
	{
		success = false;
		return "";
	}
	std::string extension = path.substr(posdot + 1, path.size() - posdot - 1);
	size_t posspace = extension.rfind(" ", extension.size());
	if (posspace != extension.npos)
	{
		success = false;
		return "";
	}
	success = true;
	return extension;
}

std::string FileSystem::CombinePaths(const std::string &first, const std::string &second)
{
	if (second == "")
	{
		return first;
	}
	return CanonicalPath(first + "\\" + second);
}

bool FileSystem::FileExists(const std::string &path)
{
	return PathFileExists(path.c_str()) == TRUE;
}

bool FileSystem::IsFolder(const std::string &path)
{
	return PathIsDirectory(path.c_str()) == FILE_ATTRIBUTE_DIRECTORY;
}

void FileSystem::CreateFolder(const std::string &path, bool &success)
{
	success = CreateDirectory(path.c_str(), nullptr) != 0;
}

void FileSystem::RemoveFolder(const std::string &path, bool &success)
{
	success = RemoveDirectory(path.c_str()) != 0;
}

void FileSystem::FileDelete(const std::string &path, bool &success)
{
	success = DeleteFile(path.c_str()) != 0;
}

std::string FileSystem::GetEnvVar(const std::string &name, bool &exists)
{
	const DWORD init_size = 32;
	exists = true;
	std::string value = "";
	char* buffer = new char[init_size];
	DWORD size = init_size;
	DWORD newSize;
	newSize = GetEnvironmentVariable(name.c_str(), buffer, size);
	if (newSize == 0)
	{
		if (GetLastError() == ERROR_ENVVAR_NOT_FOUND)
		{
			exists = false;
			return value;
		}
		else
		{
			return value;
		}
	}
	else
	{
		if (newSize >= size)
		{
			delete[] buffer;
			buffer = new char[newSize];
			DWORD newNewSize = GetEnvironmentVariable(name.c_str(), buffer, newSize);
			if (newNewSize != newSize - 1)
			{
				return value;
			}
		}
		value = std::string(buffer);
	}
	return value;
}
std::string FileSystem::ReplaceEnvVars(const std::string &str, bool &success)
{
	const std::string tag_beg = "<:";
	const std::string tag_end = ":>";
	success = true;
	size_t crtpos = 0;
	std::vector<std::pair<size_t, size_t>> indices;
	while ((crtpos = str.find(tag_beg, crtpos)) != std::string::npos)
	{
		size_t begin = crtpos;
		size_t end = str.find(tag_end, begin);
		if (end == std::string::npos)
		{
			success = false;
			return str;
		}
		begin += tag_beg.size();
		indices.push_back(std::make_pair(begin, end - begin));
		crtpos = end;
	}
	std::string result = str;
	for (int i = indices.size() - 1; i >= 0; i--)
	{
		bool exists = false;
		std::string value = GetEnvVar(result.substr(indices[i].first, indices[i].second), exists);
		if (!exists)
		{
			success = false;
			return str;
		}
		result.replace(indices[i].first - tag_beg.size(), indices[i].second + tag_beg.size() + tag_end.size(), value);
	}
	return result;
}
std::string FileSystem::ReplaceEnvVarsAndCanonicalize(const std::string &path, bool &success)
{
	return CanonicalPath(ReplaceEnvVars(path, success));
}
