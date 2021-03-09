#include "Helper.h"

void(*warningFunction)(std::string msg) = giveWarningFunction;

std::string toLower(std::string str)
{
	std::string res;
	for (int i = 0; i < str.size(); i++)
	{
		res += " ";
	}
	std::string::iterator i = str.begin();
	std::string::iterator j = res.begin();
	for (; i != str.end(); i++, j++)
	{

		if (*i >= 'A' && *i <= 'Z')
		{
			*j = *i - 'A' + 'a';
		}
		else
		{
			*j = *i;
		}
	}
	return res;
}

void errorFunction(std::string msg, int val)
{
	std::cout << "" << std::endl;
	std::cout << "  Error: " + msg << std::endl;
	std::cout << "" << std::endl;
	//std::cerr << "Error: " + msg << std::endl;

	//fflush(stdout);
	//fflush(stderr);

	exit(val);
}

void errorContinueFunction(std::string msg)
{
	std::cout << "" << std::endl;
	std::cout << "  Error: " + msg << std::endl;
	std::cout << "" << std::endl;
	//std::cerr << "Error: " + msg << std::endl;
}

void giveWarningFunction(std::string msg)
{
	//std::cout << "" << std::endl;
	std::cout << "  Warning: " + msg << std::endl;
	std::cout << "" << std::endl;
	//std::cerr << "Error: " + msg << std::endl;

	//fflush(stdout);
	//fflush(stderr);
}

void enableWarnings()
{
	warningFunction = giveWarningFunction;
}
void disableWarnings()
{
	warningFunction = noWarningFunction;
}

inline void noWarningFunction(std::string msg)
{
	
}

std::string putInQuotes(std::string val)
{
	return "\"" + val + "\"";
}

void printInQuotes(std::string val)
{
	std::cout << putInQuotes(val) << std::endl;
}

void printDBG(std::string msg)
{
	std::cout << "    DBG - [" << msg << "]" << std::endl;
}

void extractPackage(const std::string &content, std::vector<std::string> &package, bool &errorOccurred, std::string &errorMsg)
{
	errorOccurred = false;
	errorMsg = "";

	size_t crtpos = 0;
	size_t begpos = 0;
	size_t endpos = 0;
	package.clear();
	
	std::string text = content;

	std::vector<std::pair<size_t, size_t>> indices;
	while (crtpos < text.size() && (begpos = text.find("//", crtpos)) != std::string::npos)
	{
		if ((endpos = text.find("\n", begpos)) == std::string::npos)
		{
			errorOccurred = true;
			errorMsg = "Single line comment oppened but not closed";
			return;
		}
		indices.push_back(std::make_pair(begpos, endpos + 1));
		crtpos = endpos + 1;
	}
	for (int i = indices.size() - 1; i >= 0; i--)
	{
		text = text.replace(indices[i].first, indices[i].second - indices[i].first, "");
	}

	indices.clear();
	while (crtpos < text.size() && (begpos = text.find("/*", crtpos)) != std::string::npos)
	{
		if ((endpos = text.find("*/", begpos)) == std::string::npos)
		{
			errorOccurred = true;
			errorMsg = "Muly-line comment oppened but not closed";
			return;
		}
		indices.push_back(std::make_pair(begpos, endpos + 2));
		crtpos = endpos + 1;
	}
	for (int i = indices.size() - 1; i >= 0; i--)
	{
		if (text.find("\n", indices[i].first, indices[i].second - indices[i].first) == std::string::npos)
		{
			text = text.replace(indices[i].first, indices[i].second - indices[i].first, "");
		}
		else
		{
			text = text.replace(indices[i].first, indices[i].second - indices[i].first, "\n");
		}
	}

	crtpos = 0;
	begpos = 0;
	endpos = 0;
	if ((crtpos = text.find("package", crtpos)) == std::string::npos)
	{
		return;
	}

	begpos = crtpos + std::string("package").size();
	if ((endpos = text.find(";", begpos)) == std::string::npos)
	{
		//errorFunction("Incorrect package definition");
		errorOccurred = true;
		errorMsg = "Package definition must end with a semicolon";
		return;
	}

	std::string pkg0 = text.substr(begpos, endpos - begpos);

	//crtpos = 0;
	//std::string pkg1 = "";
	//while (crtpos < pkg0.size() && (begpos = pkg0.find("//", crtpos)) != std::string::npos)
	//{
	//	if ((endpos = pkg0.find("\n", begpos)) == std::string::npos)
	//	{
	//		//errorFunction("Incorrect package definition");
	//		errorOccurred = true;
	//		errorMsg = "Incorrect package definition2";
	//		return;
	//	}
	//	pkg1 += pkg0.substr(crtpos, begpos - crtpos);
	//	crtpos = endpos + 1;
	//}
	//if (crtpos < pkg0.size())
	//{
	//	pkg1 += pkg0.substr(crtpos, pkg0.size() - crtpos);
	//}

	//crtpos = 0;
	//std::string pkg2 = "";
	//while (crtpos < pkg1.size() && (begpos = pkg1.find("/*", crtpos)) != std::string::npos)
	//{
	//	if ((endpos = pkg1.find("*/", begpos)) == std::string::npos)
	//	{
	//		//errorFunction("Incorrect package definition");
	//		errorOccurred = true;
	//		errorMsg = "Incorrect package definition3";
	//		return;
	//	}
	//	pkg2 += pkg1.substr(crtpos, begpos - crtpos);
	//	crtpos = endpos + 2;
	//}
	//if (crtpos < pkg1.size())
	//{
	//	pkg2 += pkg1.substr(crtpos, pkg1.size() - crtpos);
	//}

	std::string pkg2 = pkg0;

	std::string pkg3 = "";
	for (size_t i = 0; i < pkg2.size(); i++)
	{
		switch (pkg2[i])
		{
		case ' ':
		case '\t':
		case '\n':
			break;
		default:
			if (pkg2[i] >= 'a' && pkg2[i] <= 'z' || pkg2[i] >= 'A' && pkg2[i] <= 'Z' || pkg2[i] >= '0' && pkg2[i] <= '9' || pkg2[i] == '.')
			{
				pkg3 += pkg2[i];
			}
			else
			{
				//errorFunction("Incorrect package definition");
				errorOccurred = true;
				errorMsg = "Package name contains invalid characters.";
				return;
			}
			break;
		}
	}

	std::stringstream ss(pkg3);
	std::string token;
	while (std::getline(ss, token, '.'))
	{
		package.push_back(token);
	}

	//while ((begpos = content.find("/*", crtpos)) != std::string::npos)
	//{
	//	size_t tmppos = content.find("*/", crtpos);
	//	if (tmppos < begpos)
	//only one package(or none)
	//	"package" -> ";"
	//	"//" -> "\n"
	//	"/*" -> "*/"
	//	getline(".")
	//	remove all begining and trailing \s\t\n\r
}

//void extractPackage(const std::string &content, std::vector<std::string> &package, bool &errorOccurred, std::string &errorMsg)
//{
//	errorOccurred = false;
//	errorMsg = "";
//
//	size_t crtpos = 0;
//	size_t begpos = 0;
//	size_t endpos = 0;
//	package.clear();
//
//	if ((crtpos = content.find("package", crtpos)) == std::string::npos)
//	{
//		return;
//	}
//
//	begpos = crtpos + std::string("package").size();
//	if ((endpos = content.find(";", begpos)) == std::string::npos)
//	{
//		//errorFunction("Incorrect package definition");
//		errorOccurred = true;
//		errorMsg = "Incorrect package definition1";
//		return;
//	}
//
//	std::string pkg0 = content.substr(begpos, endpos - begpos);
//
//	crtpos = 0;
//	std::string pkg1 = "";
//	while (crtpos < pkg0.size() && (begpos = pkg0.find("//", crtpos)) != std::string::npos)
//	{
//		if ((endpos = pkg0.find("\n", begpos)) == std::string::npos)
//		{
//			//errorFunction("Incorrect package definition");
//			errorOccurred = true;
//			errorMsg = "Incorrect package definition2";
//			return;
//		}
//		pkg1 += pkg0.substr(crtpos, begpos - crtpos);
//		crtpos = endpos + 1;
//	}
//	if (crtpos < pkg0.size())
//	{
//		pkg1 += pkg0.substr(crtpos, pkg0.size() - crtpos);
//	}
//
//	crtpos = 0;
//	std::string pkg2 = "";
//	while (crtpos < pkg1.size() && (begpos = pkg1.find("/*", crtpos)) != std::string::npos)
//	{
//		if ((endpos = pkg1.find("*/", begpos)) == std::string::npos)
//		{
//			//errorFunction("Incorrect package definition");
//			errorOccurred = true;
//			errorMsg = "Incorrect package definition3";
//			return;
//		}
//		pkg2 += pkg1.substr(crtpos, begpos - crtpos);
//		crtpos = endpos + 2;
//	}
//	if (crtpos < pkg1.size())
//	{
//		pkg2 += pkg1.substr(crtpos, pkg1.size() - crtpos);
//	}
//
//	std::string pkg3 = "";
//	for (size_t i = 0; i < pkg2.size(); i++)
//	{
//		switch (pkg2[i])
//		{
//		case ' ':
//		case '\t':
//		case '\n':
//			break;
//		default:
//			if (pkg2[i] >= 'a' && pkg2[i] <= 'z' || pkg2[i] >= 'A' && pkg2[i] <= 'Z' || pkg2[i] >= '0' && pkg2[i] <= '9' || pkg2[i] == '.')
//			{
//				pkg3 += pkg2[i];
//			}
//			else
//			{
//				//errorFunction("Incorrect package definition");
//				errorOccurred = true;
//				errorMsg = "Incorrect package definition4";
//				return;
//			}
//			break;
//		}
//	}
//
//	std::stringstream ss(pkg3);
//	std::string token;
//	while (std::getline(ss, token, '.'))
//	{
//		package.push_back(token);
//	}
//
//	//while ((begpos = content.find("/*", crtpos)) != std::string::npos)
//	//{
//	//	size_t tmppos = content.find("*/", crtpos);
//	//	if (tmppos < begpos)
//	//only one package(or none)
//	//	"package" -> ";"
//	//	"//" -> "\n"
//	//	"/*" -> "*/"
//	//	getline(".")
//	//	remove all begining and trailing \s\t\n\r
//}

void extractPackageFromFile(const std::string &filename, std::vector<std::string> &package, bool &errorOccurred, std::string &errorMsg)
{
	errorOccurred = false;
	errorMsg = "";
	std::ifstream in;
	std::stringstream stream;

	in.open(filename);
	if (!in.is_open())
	{
		//errorFunction("Could not open file \"" + filename + "\"");
		errorOccurred = true;
		errorMsg = "Could not open file \"" + filename + "\"";
		return;
	}
	stream << in.rdbuf();
	std::string content = stream.str();
	in.close();
	stream.str("");

	extractPackage(content, package, errorOccurred, errorMsg);
	if (errorOccurred)
	{
		errorMsg += "\n";
		errorMsg += "  In file \"" + filename + "\"";
		return;
	}
}
