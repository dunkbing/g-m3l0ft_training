#include "XMLParser.h"

//      manifest,   proguard,       java,       aidl,        res,     assets,       libs,    jniLibs,     native,
const XMLParser::J2AData::Use::type XMLParser::J2AData::UseTag[BuildType::COUNT][Tag::COUNT] = {
	{ Use::   No, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::   No },
	{ Use::   No, Use::Maybe, Use::Maybe, Use::Maybe, Use::   No, Use::Maybe, Use::Maybe, Use::   No, Use::   No },
	{ Use::  Yes, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::   No },
	{ Use::  Yes, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::Maybe, Use::   No }
};
//      manifest,   proguard,       java,       aidl,        res,     assets,       libs,    jniLibs,     native,

const bool XMLParser::J2AData::IsSupported[BuildType::COUNT] = { true, true, true, false };

XMLParser::J2AData::J2AData() :
name(""),
buildType(BuildType::Copy),
buildArtefact(""),
manifestPath(""),
proguardPath(""),
useBuildArtefact(false),
useManifest(false),
useProguard(false),
useJavaPreprocess(false)
{
	minSdkVersion = 0;
	targetSdkVersion = 0;
	compileSdkVersion = 0;
	buildToolsVersion[0] = 0;
	buildToolsVersion[1] = 0;
	buildToolsVersion[2] = 0;
	for (size_t i = 0; i < Directories::COUNT; i++)
	{
		useDirs[i] = false;
	}
}

const std::string XMLParser::Java2Android::unspecifiedJniType = "[UNSPECIFIED]";

XMLParser::Java2Android::Java2Android()
{
	minSDK = 14;
	targetSDK = 19;
	compileSDK = 19;
	buildTools[0] = 19;
	buildTools[1] = 1;
	buildTools[2] = 0;
	applicationID = "com.java2android.default";
	signatureKeystorePath = "keystore";
	signatureKeystoreRelativePath = signatureKeystorePath;
	signatureKeystorePass = "000000";
	signatureKeyName = "key";
	signatureKeyPass = "000000";
	//defaultJniType = "aremeabi-v7a";
	incrementalBuild = false;
	lintOptions = "";
	packageOptions = "";
	workspaceFolder = "workspace";
	workspaceLocalManifest = "AndroidManifest.xml";
	workspaceLocalProguard = "proguard.cfg";
	workspaceLocalJava = "java";
	workspaceLocalAIDL = "aidl";
	workspaceLocalRes = "res";
	workspaceLocalAssets = "assets";
	workspaceLocalLibs = "libs";
	workspaceLocalJniLibs = "jniLibs";

	workspaceFolder = FileSystem::CanonicalPath(FileSystem::FullPath("workspace"));
	workspaceManifest = FileSystem::CombinePaths(workspaceFolder, workspaceLocalManifest);
	workspaceProguard = FileSystem::CombinePaths(workspaceFolder, workspaceLocalProguard);
	workspaceJava = FileSystem::CombinePaths(workspaceFolder, workspaceLocalJava);
	workspaceAIDL = FileSystem::CombinePaths(workspaceFolder, workspaceLocalAIDL);
	workspaceRes = FileSystem::CombinePaths(workspaceFolder, workspaceLocalRes);
	workspaceAssets = FileSystem::CombinePaths(workspaceFolder, workspaceLocalAssets);
	workspaceLibs = FileSystem::CombinePaths(workspaceFolder, workspaceLocalLibs);
	workspaceJniLibs = FileSystem::CombinePaths(workspaceFolder, workspaceLocalJniLibs);

	workspaceRelativeFolder = FileSystem::CanonicalPath("workspace");
	workspaceRelativeManifest = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalManifest);
	workspaceRelativeProguard = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalProguard);
	workspaceRelativeJava = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalJava);
	workspaceRelativeAIDL = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalAIDL);
	workspaceRelativeRes = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalRes);
	workspaceRelativeAssets = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalAssets);
	workspaceRelativeLibs = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalLibs);
	workspaceRelativeJniLibs = FileSystem::CombinePaths(workspaceRelativeFolder, workspaceLocalJniLibs);

	flagsPathList.clear();
	includesList.clear();
	macrosList.clear();
	j2aPathsList.clear();
	architectureTypeIndices.clear();
	architectureTypeIndices.push_back(0);
	excludedJarClassPaths.clear();

	useMinSDK = false;
	useTargetSDK = false;
	useCompileSDK = false;
	useBuildTools = false;
	useApplicationID = false;
	useSignatureKeystorePath = false;
	useSignatureKeystorePass = false;
	useSignatureKeyName = false;
	useSignatureKeyPass = false;
	//useDefaultJniType = false;
	useIncrementalBuild = false;
	useLintOptions = false;
	usePackageOptions = false;
	useWorkspaceFolder = false;
	useWorkspaceLocalManifest = false;
	useWorkspaceLocalProguard = false;
	useWorkspaceLocalJava = false;
	useWorkspaceLocalAIDL = false;
	useWorkspaceLocalRes = false;
	useWorkspaceLocalAssets = false;
	useWorkspaceLocalLibs = false;
	useWorkspaceLocalJniLibs = false;
	useFlagsPathList = false;
	useIncludesList = false;
	useMacrosList = false;
	useJ2aPathsList = false;
	useArchitectureTypeIndices = false;
	useExcludedJarClassPaths = false;
}

void XMLParser::toXMLDoc(const std::string &filename, std::string &content, rapidxml::xml_document<> &doc, bool &errorOccurred, std::string &errorMsg)
{
	std::ifstream in;
	std::stringstream stream;
	
	in.open(filename);
	if (!in.is_open())
	{
		errorOccurred = true;
		errorMsg = "Could not open file \"" + filename + "\"";
		return;
	}
	stream << in.rdbuf();
	content = stream.str();
	in.close();
	stream.str("");

	try
	{
		doc.parse<0>((char*)(content.c_str()));
	}
	catch (rapidxml::parse_error &e)
	{
		errorOccurred = true;
		errorMsg = "\"" + filename + "\" is an invalid xml file - \"" + e.what() + "\"";
		return;
	}
}

void XMLParser::processJava2AndroidProject(const rapidxml::xml_node<> *node, const std::string &folder, Java2Android &data, bool &errorOccurred, std::string &errorMsg)
{
	bool success = false;
	rapidxml::xml_node<> *buildNode = node->first_node("build");
	if (buildNode != nullptr)
	{
		std::stringstream ss;

		data.useMinSDK = false;
		rapidxml::xml_node<> *minSdkVersionNode = buildNode->first_node("minSdkVersion");
		if (minSdkVersionNode != nullptr)
		{
			data.useMinSDK = true;
			rapidxml::xml_attribute<> *minSdkVersionAttr = minSdkVersionNode->first_attribute("value");
			if (minSdkVersionAttr == nullptr || minSdkVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<minSdkVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVars(minSdkVersionAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <minSdkVersion> \"value\" attribute";
				return;
			}

			ss.str("");
			ss << cleanVal;
			ss >> data.minSDK;
			if (ss.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <minSdkVersion> node";
				return;
			}
			ss.clear();
		}

		data.useTargetSDK = false;
		rapidxml::xml_node<> *targetSdkVersionNode = buildNode->first_node("targetSdkVersion");
		if (targetSdkVersionNode != nullptr)
		{
			data.useTargetSDK = true;
			rapidxml::xml_attribute<> *targetSdkVersionAttr = targetSdkVersionNode->first_attribute("value");
			if (targetSdkVersionAttr == nullptr || targetSdkVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<targetSdkVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVars(targetSdkVersionAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <targetSdkVersion> \"value\" attribute";
				return;
			}

			ss.str("");
			ss << cleanVal;
			ss >> data.targetSDK;
			if (ss.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <targetSdkVersion> node";
				return;
			}
			ss.clear();
		}

		rapidxml::xml_node<> *compileSdkVersionNode = buildNode->first_node("compileSdkVersion");
		if (compileSdkVersionNode != nullptr)
		{
			data.useCompileSDK = true;
			rapidxml::xml_attribute<> *compileSdkVersionAttr = compileSdkVersionNode->first_attribute("value");
			if (compileSdkVersionAttr == nullptr || compileSdkVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<compileSdkVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVars(compileSdkVersionAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <compileSdkVersion> \"value\" attribute";
				return;
			}

			ss.str("");
			ss << cleanVal;
			ss >> data.compileSDK;
			if (ss.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <compileSdkVersion> node";
				return;
			}
			ss.clear();
		}

		rapidxml::xml_node<> *buildToolsVersionNode = buildNode->first_node("buildToolsVersion");
		if (buildToolsVersionNode != nullptr)
		{
			data.useBuildTools = true;
			rapidxml::xml_attribute<> *buildToolsVersionAttr = buildToolsVersionNode->first_attribute("value");
			if (buildToolsVersionAttr == nullptr || buildToolsVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<buildToolsVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVars(buildToolsVersionAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <buildToolsVersion> \"value\" attribute";
				return;
			}

			ss.str("");
			ss << cleanVal;
			std::string v0, v1, v2;
			std::getline(ss, v0, '.');
			std::getline(ss, v1, '.');
			std::getline(ss, v2, '.');
			if (v1 == "")
			{
				v1 = "0";
			}
			if (v2 == "")
			{
				v2 = "0";
			}
			std::stringstream ss1;
			ss1.str("");
			ss1 << v0;
			ss1 >> data.buildTools[0];
			if (ss1.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <buildToolsVersion> node";
				return;
			}
			ss1.clear();
			ss1.str("");
			ss1 << v1;
			ss1 >> data.buildTools[1];
			if (ss1.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <buildToolsVersion> node";
				return;
			}
			ss1.clear();
			ss1.str("");
			ss1 << v2;
			ss1 >> data.buildTools[2];
			if (ss1.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <buildToolsVersion> node";
				return;
			}
			ss1.clear();
		}

		if (data.useMinSDK && data.minSDK == 0)
		{
			///????????????????????????????????????
			errorOccurred = true;
			errorMsg = "\"minSdkVersion\" can not be 0";
			return;
		}
		if (data.useMinSDK  && data.useTargetSDK && data.useMinSDK > data.targetSDK)
		{
			errorOccurred = true;
			errorMsg = "\"minSdkVersion\" must be lower or equal to \"targetSdkVersion\"";
			return;
		}
		if (data.useTargetSDK && data.targetSDK > data.compileSDK)
		{
			errorOccurred = true;
			errorMsg = "\"targetSdkVersion\" must be lower or equal to \"compileSdkVersion\"";
			return;
		}
		if (data.targetSDK > data.buildTools[0])
		{
			errorOccurred = true;
			errorMsg = "\"targetSdkVersion\" must be lower or equal to \"buildToolsVersion\"";
			return;
		}
		if (!data.useMinSDK && data.minSDK > data.compileSDK)
		{
			data.minSDK = data.compileSDK;
		}
		if (!data.useTargetSDK && data.targetSDK > data.compileSDK)
		{
			data.targetSDK = data.compileSDK;
		}

		rapidxml::xml_node<> *applicationIdNode = buildNode->first_node("applicationId");
		if (applicationIdNode != nullptr)
		{
			data.useApplicationID = true;
			rapidxml::xml_attribute<> *applicationIdAttr = applicationIdNode->first_attribute("value");
			if (applicationIdAttr == nullptr || applicationIdAttr ->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<applicationId> \"value\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVars(applicationIdAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <applicationId> \"value\" attribute";
				return;
			}

			data.applicationID = cleanVal;
		}

		rapidxml::xml_node<> *signatureNode = buildNode->first_node("signature");
		if (signatureNode != nullptr)
		{
			rapidxml::xml_node<> *keystorepathNode = signatureNode->first_node("keystorepath");
			if (keystorepathNode == nullptr)
			{
				errorOccurred = true;
				errorMsg = "<keystorepath> node not found";
				return;
			}
			if (keystorepathNode != nullptr)
			{
				data.useSignatureKeystorePath = true;
				rapidxml::xml_attribute<> *keystorepathAttr = keystorepathNode->first_attribute("value");
				if (keystorepathAttr == nullptr || keystorepathAttr->value_size() == 0)
				{
					errorOccurred = true;
					errorMsg = "<keystorepath> \"value\" attribute does not exist or is empty";
					return;
				}

				std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(keystorepathAttr->value(), success);
				if (!success)
				{
					errorOccurred = true;
					errorMsg = "Could not replace environment variables in <keystorepath> \"value\" attribute";
					return;
				}

				//std::cout << putInQuotes(folder) << std::endl;
				//std::cout << putInQuotes(keystorepathAttr->value()) << std::endl;
				//std::cout << putInQuotes(FileSystem::CanonicalPath(keystorepathAttr->value())) << std::endl;
				//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(keystorepathAttr->value()))) << std::endl;
				//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(keystorepathAttr->value())))) << std::endl;
				data.signatureKeystorePath = FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(cleanVal)));
				data.signatureKeystoreRelativePath = FileSystem::CanonicalPath(cleanVal);
				std::ifstream in(data.signatureKeystorePath);
				if (!in.is_open())
				{
					errorOccurred = true;
					errorMsg = putInQuotes(data.signatureKeystorePath) + " does not exist";
					return;
				}
			}

			rapidxml::xml_node<> *keystorepassNode = signatureNode->first_node("keystorepass");
			if (keystorepassNode == nullptr)
			{
				errorOccurred = true;
				errorMsg = "<keystorepass> node not found";
				return;
			}
			if (keystorepassNode != nullptr)
			{
				data.useSignatureKeystorePass = true;
				rapidxml::xml_attribute<> *keystorepassAttr = keystorepassNode->first_attribute("value");
				if (keystorepassAttr == nullptr || keystorepassAttr->value_size() == 0)
				{
					errorOccurred = true;
					errorMsg = "<keystorepass> \"value\" attribute does not exist or is empty";
					return;
				}

				std::string cleanVal = FileSystem::ReplaceEnvVars(keystorepassAttr->value(), success);
				if (!success)
				{
					errorOccurred = true;
					errorMsg = "Could not replace environment variables in <keystorepass> \"value\" attribute";
					return;
				}

				data.signatureKeystorePass = cleanVal;
			}

			rapidxml::xml_node<> *keynameNode = signatureNode->first_node("keyname");
			if (keynameNode == nullptr)
			{
				errorOccurred = true;
				errorMsg = "<keyname> node not found";
				return;
			}
			if (keynameNode != nullptr)
			{
				data.useSignatureKeyName = true;
				rapidxml::xml_attribute<> *keynameAttr = keynameNode->first_attribute("value");
				if (keynameAttr == nullptr || keynameAttr->value_size() == 0)
				{
					errorOccurred = true;
					errorMsg = "<keyname> \"value\" attribute does not exist or is empty";
					return;
				}

				std::string cleanVal = FileSystem::ReplaceEnvVars(keynameAttr->value(), success);
				if (!success)
				{
					errorOccurred = true;
					errorMsg = "Could not replace environment variables in <keyname> \"value\" attribute";
					return;
				}

				data.signatureKeyName = cleanVal;
			}

			rapidxml::xml_node<> *keypassNode = signatureNode->first_node("keypass");
			if (keypassNode == nullptr)
			{
				errorOccurred = true;
				errorMsg = "<keypass> node not found";
				return;
			}
			if (keypassNode != nullptr)
			{
				data.useSignatureKeyPass = true;
				rapidxml::xml_attribute<> *keypassAttr = keypassNode->first_attribute("value");
				if (keypassAttr == nullptr || keypassAttr->value_size() == 0)
				{
					errorOccurred = true;
					errorMsg = "<keypass> \"value\" attribute does not exist or is empty";
					return;
				}

				std::string cleanVal = FileSystem::ReplaceEnvVars(keypassAttr->value(), success);
				if (!success)
				{
					errorOccurred = true;
					errorMsg = "Could not replace environment variables in <keypass> \"value\" attribute";
					return;
				}

				data.signatureKeyPass = cleanVal;
			}
		}

		//rapidxml::xml_node<> *defaultJniTypeNode = buildNode->first_node("defaultJniType");
		//if (defaultJniTypeNode != nullptr)
		//{
		//	data.useDefaultJniType = true;
		//	rapidxml::xml_attribute<> *typeAttr = defaultJniTypeNode->first_attribute("value");
		//	if (typeAttr == nullptr || typeAttr->value_size() == 0)
		//	{
		//		errorOccurred = true;
		//		errorMsg = "Invalid value for \"value\" attribute in <defaultJniType> node";
		//		return;
		//	}

		//	std::string cleanVal = FileSystem::ReplaceEnvVars(typeAttr->value(), success);
		//	if (!success)
		//	{
		//		errorOccurred = true;
		//		errorMsg = "Could not replace environment variables in <defaultJniType> \"value\" attribute";
		//		return;
		//	}

		//	std::string val = cleanVal;

		//	bool found = false;
		//	for (size_t i = 0; i < Global::varJNITypesCount; i++)
		//	{
		//		if (val == Global::varJNITypes[i])
		//		{
		//			found = true;
		//			break;
		//		}
		//	}
		//	if (!found)
		//	{
		//		errorOccurred = true;
		//		errorMsg = "Invalid value for \"value\" attribute in <defaultJniType> node";
		//		return;
		//	}
		//	data.defaultJniType = val;
		//}

		rapidxml::xml_node<> *architectureNode = buildNode->first_node("architectures");
		//if (signatureNode == nullptr)
		//{
		//	if (data.architectureTypeIndices.size() == 0)
		//	{
		//		errorOccurred = true;
		//		errorMsg = "At least one architecture type must be defined";
		//		return;
		//	}
		//}
		if (architectureNode != nullptr)
		{
			data.useArchitectureTypeIndices = true;
			data.architectureTypeIndices.clear();

			bool atLeastOne = false;
			for (rapidxml::xml_node<> *typeNode = architectureNode->first_node("type"); typeNode; typeNode = typeNode->next_sibling("type"))
			{
				atLeastOne = true;

				rapidxml::xml_attribute<> *nameAttr = typeNode->first_attribute("name");
				if (nameAttr == nullptr || nameAttr->value_size() == 0)
				{
					errorOccurred = true;
					errorMsg = "<type> \"name\" attribute does not exist or is empty";
					return;
				}

				std::string cleanVal = FileSystem::ReplaceEnvVars(nameAttr->value(), success);
				if (!success)
				{
					errorOccurred = true;
					errorMsg = "Could not replace environment variables in <type> \"name\" attribute";
					return;
				}

				bool found = false;
				size_t index = 0;
				for (size_t i = 0; i < Global::varJNITypesCount; i++)
				{
					if (cleanVal == Global::varJNITypes[i])
					{
						found = true;
						index = i;
						break;
					}
				}
				if (!found)
				{
					errorOccurred = true;
					errorMsg = "Invalid value for \"name\" attribute in <type> node";
					return;
				}

				for (int i = 0; i < data.architectureTypeIndices.size(); i++)
				{
					if (index == data.architectureTypeIndices[i])
					{
						errorOccurred = true;
						errorMsg = "Duplicate architecture type definition";
						return;
					}
				}
				data.architectureTypeIndices.push_back(index);
			}

			if (!atLeastOne)
			{
				errorOccurred = true;
				errorMsg = "At least one architecture type must be defined";
				return;
			}
		}

		rapidxml::xml_node<> *incrementalbuildNode = buildNode->first_node("incrementalbuild");
		if (incrementalbuildNode != nullptr)
		{
			data.useIncrementalBuild = true;
			rapidxml::xml_attribute<> *incrementalbuilAttr = incrementalbuildNode->first_attribute("value");
			if (incrementalbuilAttr == nullptr || incrementalbuilAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <incrementalbuild> node";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVars(incrementalbuilAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <incrementalbuild> \"value\" attribute";
				return;
			}

			//std::cout << putInQuotes(incrementalbuilAttr->value()) << std::endl;

			std::string value = cleanVal;
			if (value == "true")
			{
				data.incrementalBuild = true;
			}
			else if (value == "false")
			{
				data.incrementalBuild = false;
			}
			else
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <incrementalbuild> node";
				return;
			}
		}

		rapidxml::xml_node<> *lintOptionsNode = buildNode->first_node("lintOptions");
		if (lintOptionsNode != nullptr)
		{
			data.useLintOptions = true;
			data.lintOptions = "";
			data.lintOptions += "\n";
			data.lintOptions += lintOptionsNode->value();
		}

		rapidxml::xml_node<> *packagingOptionsNode = buildNode->first_node("packagingOptions");
		if (packagingOptionsNode != nullptr)
		{
			data.usePackageOptions = true;
			data.packageOptions += "\n";
			data.packageOptions += packagingOptionsNode->value();
		}

		rapidxml::xml_node<> *excludeNode = buildNode->first_node("exclude");
		if (excludeNode != nullptr)
		{
			data.useExcludedJarClassPaths = true;
			
			for (rapidxml::xml_node<> *jarNode = excludeNode->first_node("jar"); jarNode; jarNode = jarNode->next_sibling("jar"))
			{
				rapidxml::xml_attribute<> *jarNameAttr = jarNode->first_attribute("name");

				if (jarNameAttr == nullptr || std::string(jarNameAttr->value()).empty())
				{
					errorOccurred = true;
					errorMsg = "<jar> \"name\" attribute does not exist or is empty";
					return;
				}

				std::string cleanVal = FileSystem::ReplaceEnvVars(jarNameAttr->value(), success);
				if (!success)
				{
					errorOccurred = true;
					errorMsg = "Could not replace environment variables in <jar> \"name\" attribute";
					return;
				}

				std::string jarName = cleanVal;

				std::pair<std::map<std::string, std::set<std::string>>::iterator, bool> result = data.excludedJarClassPaths.insert(std::make_pair(FileSystem::CanonicalPath(jarName), std::set<std::string>()));
				std::set<std::string>& excludedPaths = result.first->second;
					
				for (rapidxml::xml_node<> *classNode = jarNode->first_node("class"); classNode; classNode = classNode->next_sibling("class"))
				{
					rapidxml::xml_attribute<> *classPathAttr = classNode->first_attribute("path");
						
					if (classPathAttr == nullptr || std::string(classPathAttr->value()).empty())
					{
						errorOccurred = true;
						errorMsg = "<class> \"path\" attribute does not exist or is empty";
						return;
					}

					std::string cleanVal = FileSystem::ReplaceEnvVars(classPathAttr->value(), success);
					if (!success)
					{
						errorOccurred = true;
						errorMsg = "Could not replace environment variables in <class> \"path\" attribute";
						return;
					}

					std::string classPath = cleanVal;

					std::pair<std::set<std::string>::iterator, bool> result = excludedPaths.insert(FileSystem::CanonicalPath(classPath));
				}
			}
		}
	}
	rapidxml::xml_node<> *workspaceNode = node->first_node("workspace");
	if (workspaceNode != nullptr)
	{
		data.useWorkspaceFolder = true;
		rapidxml::xml_attribute<> *pathAttr = workspaceNode->first_attribute("path");
		if (pathAttr == nullptr || pathAttr->value_size() == 0)
		{
			errorOccurred = true;
			errorMsg = "<workspace> \"path\" attribute does not exist or is empty";
			return;
		}

		std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
		if (!success)
		{
			errorOccurred = true;
			errorMsg = "Could not replace environment variables in <workspace> \"path\" attribute";
			return;
		}

		//std::cout << putInQuotes(folder) << std::endl;
		//std::cout << putInQuotes(pathAttr->value()) << std::endl;
		//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
		//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
		//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
		data.workspaceFolder = FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(cleanVal)));
		data.workspaceRelativeFolder = FileSystem::CanonicalPath(pathAttr->value());
		data.workspaceRelativeManifest = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalManifest);
		data.workspaceRelativeProguard = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalProguard);
		data.workspaceRelativeJava = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalJava);
		data.workspaceRelativeAIDL = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalAIDL);
		data.workspaceRelativeRes = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalRes);
		data.workspaceRelativeAssets = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalAssets);
		data.workspaceRelativeLibs = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalLibs);
		data.workspaceRelativeJniLibs = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalJniLibs);
		
		rapidxml::xml_node<> *manifestNode = workspaceNode->first_node("manifest");
		if (manifestNode != nullptr)
		{
			data.useWorkspaceLocalManifest = true;
			rapidxml::xml_attribute<> *pathAttr = manifestNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<manifest> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <manifest> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalManifest = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeManifest = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalManifest);
		}

		rapidxml::xml_node<> *proguardNode = workspaceNode->first_node("proguard");
		if (proguardNode != nullptr)
		{
			data.useWorkspaceLocalProguard = true;
			rapidxml::xml_attribute<> *pathAttr = proguardNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<proguard> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <proguard> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalProguard = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeProguard = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalProguard);
		}

		rapidxml::xml_node<> *javaNode = workspaceNode->first_node("java");
		if (javaNode != nullptr)
		{
			data.useWorkspaceLocalJava = true;
			rapidxml::xml_attribute<> *pathAttr = javaNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<java> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <java> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalJava = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeJava = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalJava);
		}

		rapidxml::xml_node<> *aidlNode = workspaceNode->first_node("aidl");
		if (aidlNode != nullptr)
		{
			data.useWorkspaceLocalAIDL = true;
			rapidxml::xml_attribute<> *pathAttr = aidlNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<aidl> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <aidl> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalAIDL = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeAIDL = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalAIDL);
		}

		rapidxml::xml_node<> *resNode = workspaceNode->first_node("res");
		if (resNode != nullptr)
		{
			data.useWorkspaceLocalRes = true;
			rapidxml::xml_attribute<> *pathAttr = resNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<res> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <res> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalRes = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeRes = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalRes);
		}

		rapidxml::xml_node<> *assetsNode = workspaceNode->first_node("assets");
		if (assetsNode != nullptr)
		{
			data.useWorkspaceLocalAssets = true;
			rapidxml::xml_attribute<> *pathAttr = assetsNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<assets> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <assets> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalAssets = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeAssets = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalAssets);
		}

		rapidxml::xml_node<> *libsNode = workspaceNode->first_node("libs");
		if (libsNode != nullptr)
		{
			data.useWorkspaceLocalLibs = true;
			rapidxml::xml_attribute<> *pathAttr = libsNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<libs> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <libs> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalLibs = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeLibs = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalLibs);
		}

		rapidxml::xml_node<> *jniLibsNode = workspaceNode->first_node("jniLibs");
		if (jniLibsNode != nullptr)
		{
			data.useWorkspaceLocalJniLibs = true;
			rapidxml::xml_attribute<> *pathAttr = jniLibsNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<jniLibs> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <jniLibs> \"path\" attribute";
				return;
			}

			//std::cout << putInQuotes(folder) << std::endl;
			//std::cout << putInQuotes(pathAttr->value()) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(pathAttr->value())) << std::endl;
			//std::cout << putInQuotes(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value()))) << std::endl;
			//std::cout << putInQuotes(FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(pathAttr->value())))) << std::endl;
			data.workspaceLocalJniLibs = FileSystem::CanonicalPath(cleanVal);
			data.workspaceRelativeJniLibs = FileSystem::CombinePaths(data.workspaceRelativeFolder, data.workspaceLocalJniLibs);
		}
	}
	rapidxml::xml_node<> *flagsNode = node->first_node("flags");
	if (flagsNode != nullptr)
	{
		data.useFlagsPathList = true;
		rapidxml::xml_attribute<> *pathAttr = flagsNode->first_attribute("path");
		if (pathAttr == nullptr || pathAttr->value_size() == 0)
		{
			errorOccurred = true;
			errorMsg = "<flags> \"path\" attribute does not exist or is empty";
			return;
		}

		std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
		if (!success)
		{
			errorOccurred = true;
			errorMsg = "Could not replace environment variables in <flags> \"path\" attribute";
			return;
		}

		std::string flagsPath = FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(cleanVal)));
		std::ifstream in(flagsPath);
		if (!in.is_open())
		{
			errorOccurred = true;
			errorMsg = putInQuotes(flagsPath) + " does not exist";
			return;
		}
		bool found = false;
		for (std::vector<std::string>::iterator i = data.flagsPathList.begin(); i != data.flagsPathList.end(); i++)
		{
			if (*i == flagsPath)
			{
				//std::cout << "Warning: duplicate flags file - " << putInQuotes(flagsPath) << std::endl;
				warningFunction("duplicate flags file - " + putInQuotes(flagsPath));
				found = true;
				break;
			}
		}
		if (!found)
		{
			data.flagsPathList.push_back(flagsPath);
		}
	}
	rapidxml::xml_node<> *preprocessNode = node->first_node("preprocess");
	if (preprocessNode != nullptr)
	{
		for (rapidxml::xml_node<> *includeNode = preprocessNode->first_node("include"); includeNode; includeNode = includeNode->next_sibling("include"))
		{
			data.useIncludesList = true;
			rapidxml::xml_attribute<> *pathAttr = includeNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<include> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <include> \"path\" attribute";
				return;
			}

			std::string includePath = FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(cleanVal)));
			std::ifstream in(includePath);
			if (!in.is_open())
			{
				errorOccurred = true;
				errorMsg = putInQuotes(includePath) + " does not exist";
				return;
			}
			bool found = false;
			for (std::vector<std::string>::iterator i = data.includesList.begin(); i != data.includesList.end(); i++)
			{
				if (*i == includePath)
				{
					//std::cout << "Warning: duplicate include file - " << putInQuotes(includePath) << std::endl;
					warningFunction("duplicate include file - " + putInQuotes(includePath));
					found = true;
					break;
				}
			}
			if (!found)
			{
				data.includesList.push_back(includePath);
			}
		}

		for (rapidxml::xml_node<> *macrosNode = preprocessNode->first_node("macros"); macrosNode; macrosNode = macrosNode->next_sibling("macros"))
		{
			data.useMacrosList = true;
			rapidxml::xml_attribute<> *pathAttr = macrosNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<macros> \"path\" attribute does not exist or is empty";
				return;
			}

			std::string cleanVal = FileSystem::ReplaceEnvVarsAndCanonicalize(pathAttr->value(), success);
			if (!success)
			{
				errorOccurred = true;
				errorMsg = "Could not replace environment variables in <macros> \"path\" attribute";
				return;
			}

			std::string macrosPath = FileSystem::CanonicalPath(FileSystem::CombinePaths(folder, FileSystem::CanonicalPath(cleanVal)));
			std::ifstream in(macrosPath);
			if (!in.is_open())
			{
				errorOccurred = true;
				errorMsg = putInQuotes(macrosPath) + " does not exist";
				return;
			}
			bool found = false;
			for (std::vector<std::string>::iterator i = data.macrosList.begin(); i != data.macrosList.end(); i++)
			{
				if (*i == macrosPath)
				{
					//std::cout << "Warning: duplicate macros file - " << putInQuotes(macrosPath) << std::endl;
					warningFunction("duplicate macros file - " + putInQuotes(macrosPath));
					found = true;
					break;
				}
			}
			if (!found)
			{
				data.macrosList.push_back(macrosPath);
			}
		}
	}
}
void XMLParser::processJava2AndroidGropup(const rapidxml::xml_node<> *node, Java2Android &data, bool &errorOccurred, std::string &errorMsg)
{
	rapidxml::xml_attribute<> *ifAttr = node->first_attribute("if");
	bool flag = false;
	if (ifAttr == nullptr)
	{
		flag = true;
	}
	else
	{
		//flag = processBoolExpression(ifAttr->value(), flags);
		bool isWellFormed = true;
		flag = ExpressionHandler::EvaluateBoolExpression(toLower(ifAttr->value()), isWellFormed);
		if (!isWellFormed)
		{
			errorOccurred = true;
			errorMsg = "If expression \"" + std::string(ifAttr->value()) + "\" is not well formed";
			return;
		}
	}
	if (flag)
	{
		for (rapidxml::xml_node<> *j2aNode = node->first_node("j2a"); j2aNode; j2aNode = j2aNode->next_sibling("j2a"))
		{
			rapidxml::xml_attribute<> *j2aIfAttr = j2aNode->first_attribute("if");
			bool j2aflag = false;
			if (j2aIfAttr == nullptr)
			{
				j2aflag = true;
			}
			else
			{
				//j2aflag = processBoolExpression(j2aIfAttr->value(), flags);
				bool isWellFormed = true;
				j2aflag = ExpressionHandler::EvaluateBoolExpression(toLower(j2aIfAttr->value()), isWellFormed);
				if (!isWellFormed)
				{
					errorOccurred = true;
					errorMsg = "If expression \"" + std::string(j2aIfAttr->value()) + "\" is not well formed";
					return;
				}
			}
			if (j2aflag)
			{
				rapidxml::xml_attribute<> *j2aPathAttr = j2aNode->first_attribute("path");
				std::string path = "";
				if (j2aPathAttr != nullptr)
				{
					path = j2aPathAttr->value();
				}
				if (path != "")
				{
					data.j2aPathsList[data.j2aPathsList.size()-1].push_back(path);
				}
				else
				{
					errorOccurred = true;
					errorMsg = "<j2a> \"path\" attribute does not exist or is empty";
					return;
				}
			}
		}
		for (rapidxml::xml_node<> *groupNode = node->first_node("group"); groupNode; groupNode = groupNode->next_sibling("group"))
		{
			processJava2AndroidGropup(groupNode, data, errorOccurred, errorMsg);
		}
	}
}

void XMLParser::processJava2Android(const rapidxml::xml_node<> *node, const std::string &folder, Java2Android &data, bool &errorOccurred, std::string &errorMsg)
{
	rapidxml::xml_node<> *projectNode = node->first_node("project");
	if (projectNode != nullptr)
	{
		processJava2AndroidProject(projectNode, folder, data, errorOccurred, errorMsg);
		if (errorOccurred) return;
	}
	rapidxml::xml_node<> *libsNode = node->first_node("libs");
	if (libsNode != nullptr)
	{
		data.j2aPathsList.push_back(std::vector<std::string>());
		processJava2AndroidGropup(libsNode, data, errorOccurred, errorMsg);
		if (errorOccurred) return;
	}
}

void XMLParser::processJ2ADirs(const rapidxml::xml_node<> *node, J2AData::Directories::type dir, J2AData &data, bool &errorOccurred, std::string &errorMsg)
{
	for (rapidxml::xml_node<> *dirNode = node->first_node("dir"); dirNode; dirNode = dirNode->next_sibling("dir"))
	{
		data.dirs[dir].push_back(DirData());

		rapidxml::xml_attribute<> *pathAttr = dirNode->first_attribute("path");
		std::string tmpstr = "";
		if (pathAttr != nullptr)
		{
			tmpstr = pathAttr->value();
		}
		if (tmpstr != "")
		{
			data.dirs[dir].back().path = tmpstr;
		}
		else
		{
			errorOccurred = true;
			errorMsg = "<dir> \"path\" attribute does not exist or is empty";
			return;
		}
		
		for (rapidxml::xml_node<> *ignoreNode = dirNode->first_node("ignore"); ignoreNode; ignoreNode = ignoreNode->next_sibling("ignore"))
		{
			bool isFile = true;
			rapidxml::xml_attribute<> *typeAttr = ignoreNode->first_attribute("type");
			if (typeAttr != nullptr)
			{
				if (std::string(typeAttr->value()) == "folder")
				{
					isFile = false;
				}
				else if (std::string(typeAttr->value()) == "file")
				{
					//...
				}
				else
				{
					errorOccurred = true;
					errorMsg = "Invalid value for \"type\" attribute in <ignore> node. Must be \"file\" of \"folder\"";
					return;
				}
			}

			rapidxml::xml_attribute<> *nameAttr = ignoreNode->first_attribute("name");
			rapidxml::xml_attribute<> *pathAttr = ignoreNode->first_attribute("path");

			if (nameAttr == nullptr && pathAttr == nullptr)
			{
				errorOccurred = true;
				errorMsg = "<ignore> \"name\" or \"path\" attribute missing";
				return;
			}
			if (nameAttr != nullptr && pathAttr != nullptr)
			{
				errorOccurred = true;
				errorMsg = "<ignore> \"name\" and \"path\" attributes are self exclusive";
				return;
			}
			if (nameAttr != nullptr && nameAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<ignore> \"name\" attribute is empty";
				return;
			}
			if (pathAttr != nullptr && pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<ignore> \"path\" attribute is empty";
				return;
			}

			bool isNameAndNotPath = nameAttr != nullptr;

			if (isFile)
			{
				data.dirs[dir].back().fileIgnores.push_back(std::make_pair(isNameAndNotPath, isNameAndNotPath ? nameAttr->value() : FileSystem::CanonicalPath(pathAttr->value())));
			}
			else
			{
				data.dirs[dir].back().folderIgnores.push_back(std::make_pair(isNameAndNotPath, isNameAndNotPath ? nameAttr->value() : FileSystem::CanonicalPath(pathAttr->value())));
			}
		}
	}
}
void XMLParser::processJ2AjniType(const rapidxml::xml_node<> *node, std::string jnidefaulttype, J2AData &data, bool &errorOccurred, std::string &errorMsg)
{
	std::vector<DirData>::iterator dir = data.dirs[J2AData::Directories::JniLibs].begin();
	for (rapidxml::xml_node<> *dirNode = node->first_node("dir"); dirNode; dirNode = dirNode->next_sibling("dir"))
	{
		rapidxml::xml_attribute<> *typeAttr = dirNode->first_attribute("type");
		bool useDefault = false;
		if (typeAttr == nullptr)
		{
			useDefault = true;
		}
		else
		{
			std::string val = typeAttr->value();
			if (val.size() == 0)
			{
				useDefault = true;
			}
			else
			{
				bool found = false;
				for (size_t i = 0; i < Global::varJNITypesCount; i++)
				{
					if (val == Global::varJNITypes[i])
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					errorOccurred = true;
					errorMsg = "Invalid value for \"type\" attribute in <dir> node";
					return;
				}
			}
		}
		if (useDefault)
		{
			data.jniLibsType.push_back(jnidefaulttype);
		}
		else
		{
			data.jniLibsType.push_back(typeAttr->value());
		}

		dir++;
	}
}
void XMLParser::processJ2APreprocess(const rapidxml::xml_node<> *node, J2AData &data, bool &errorOccurred, std::string &errorMsg)
{
	for (rapidxml::xml_node<> *includeNode = node->first_node("include"); includeNode; includeNode = includeNode->next_sibling("include"))
	{
		rapidxml::xml_attribute<> *pathAttr = includeNode->first_attribute("path");
		if (pathAttr == nullptr || pathAttr->value_size() == 0)
		{
			errorOccurred = true;
			errorMsg = "<include> \"path\" attribute does not exist or is empty";
			return;
		}

		data.javaPreprocess.includes.push_back(pathAttr->value());
	}

	for (rapidxml::xml_node<> *macrosNode = node->first_node("macros"); macrosNode; macrosNode = macrosNode->next_sibling("macros"))
	{
		rapidxml::xml_attribute<> *pathAttr = macrosNode->first_attribute("path");
		if (pathAttr == nullptr || pathAttr->value_size() == 0)
		{
			errorOccurred = true;
			errorMsg = "<macros> \"path\" attribute does not exist or is empty";
			return;
		}

		data.javaPreprocess.macros.push_back(pathAttr->value());
	}
}
void XMLParser::processJ2A(const rapidxml::xml_node<> *node, std::string jnidefaulttype, J2AData &data, bool &errorOccurred, std::string &errorMsg)
{
	//j2a
	rapidxml::xml_attribute<> *nameAttr = node->first_attribute("name");
	if (nameAttr == nullptr || nameAttr->value_size() == 0)
	{
		errorOccurred = true;
		errorMsg = "<j2a> \"name\" attribute does not exist or is empty";
		return;
	}
	data.name = nameAttr->value();
																								//no native 'cause were doing java only... (get rid of it)
	//name                  - art - manifest, proguard, java, aidl, res, assets, libs, jniLibs, native
	//copy                  -     -       no,      may,  may,  may, may,    may,  may,     may, ??????
	//java library          - jar -       no,      may,  may,  may,  no,    may,  may,      no,     no
	//android library       - aar -      yes,      may,  may,  may, may,    may,  may,     may, ??????
	//android application   - apk -      yes,      may,  may,  may, may,    may,  may,     may, native

	//copy                  -     -         ,         , java, aidl, res, assets, libs, jniLibs, ??????
	//java library          - jar -         , proguard, java, aidl,    , assets, libs,        , 
	//android library       - aar - manifest, proguard, java, aidl, res, assets, libs, jniLibs, ??????
	//android application   - apk - manifest, proguard, java, aidl, res, assets, libs, jniLibs, native


	//copy                  -     -         ,         , java, aidl, res, assets, libs, jniLibs, ??????
	//java library          - jar -         , proguard, java, aidl,    , assets, libs,        , 
	//android library       - aar - manifest, proguard, java, aidl, res, assets, libs, jniLibs, ??????
	//android application   - apk - manifest, proguard, java, aidl, res, assets, libs, jniLibs, native

	//native folder ? ? ?

	//Copy - most
	//JavaLibrary - probably never used
	//AndroidLibrary - for externals with manifest and res(may or may not be debuggable)
	//AndroidApplication - probably never used

	//build
	rapidxml::xml_node<> *buildNode = node->first_node("build");
	if (buildNode == nullptr)
	{
		errorOccurred = true;
		errorMsg = "<build> node not found";
		return;
	}
	rapidxml::xml_attribute<> *buildTypeAttr = buildNode->first_attribute("type");
	if (buildTypeAttr == nullptr || buildTypeAttr->value_size() == 0)
	{
		errorOccurred = true;
		errorMsg = "<build> \"type\" attribute does not exist or is empty";
		return;
	}
	std::string tmpBuildType = buildTypeAttr->value();
	std::string buildType = toLower(tmpBuildType);

	bool needArtefact = false;
	if (buildType == "copy" || buildType == "cpy")
	{
		data.buildType = J2AData::BuildType::Copy;
		bool needArtefact = false;
	}
	else if (buildType == "javalibrary" || buildType == "jar")
	{
		data.buildType = J2AData::BuildType::JavaLibrary;
		bool needArtefact = true;
	}
	else if (buildType == "androidlibrary" || buildType == "aar")
	{
		data.buildType = J2AData::BuildType::AndroidLibrary;
		bool needArtefact = true;
	}
	else if (buildType == "androidapplication" || buildType == "apk")
	{
		data.buildType = J2AData::BuildType::AndroidApplication;
		bool needArtefact = true;
	}
	else
	{
		errorOccurred = true;
		errorMsg = "Invalid value for \"type\" attribute in <build> node";
		return;
	}

	if (!XMLParser::J2AData::IsSupported[data.buildType])
	{
		errorOccurred = true;
		errorMsg = "Invalid value for \"type\" attribute in <build> node";
		return;
	}

	if (data.buildType != J2AData::BuildType::Copy)
	{
		std::stringstream ss;

		rapidxml::xml_node<> *minSdkVersionNode = buildNode->first_node("minSdkVersion");
		if (minSdkVersionNode == nullptr)
		{
			data.useMinSdkVersion = false;
		}
		else
		{
			data.useMinSdkVersion = true;
			rapidxml::xml_attribute<> *minSdkVersionAttr = minSdkVersionNode->first_attribute("value");
			if (minSdkVersionAttr == nullptr || minSdkVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<minSdkVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			ss.str("");
			ss << minSdkVersionAttr->value();
			ss >> data.minSdkVersion;
			if (ss.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <minSdkVersion> node";
				return;
			}
			ss.clear();
		}

		rapidxml::xml_node<> *targetSdkVersionNode = buildNode->first_node("targetSdkVersion");
		if (targetSdkVersionNode == nullptr)
		{
			data.useTargetSdkVersion = false;
		}
		else
		{
			data.useTargetSdkVersion = true;
			rapidxml::xml_attribute<> *targetSdkVersionAttr = targetSdkVersionNode->first_attribute("value");
			if (targetSdkVersionAttr == nullptr || targetSdkVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<targetSdkVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			ss.str("");
			ss << targetSdkVersionAttr->value();
			ss >> data.targetSdkVersion;
			if (ss.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <targetSdkVersion> node";
				return;
			}
			ss.clear();
		}

		rapidxml::xml_node<> *compileSdkVersionNode = buildNode->first_node("compileSdkVersion");
		if (compileSdkVersionNode == nullptr)
		{
			errorOccurred = true;
			errorMsg = "<compileSdkVersion> node not found";
			return;
		}
		else
		{
			rapidxml::xml_attribute<> *compileSdkVersionAttr = compileSdkVersionNode->first_attribute("value");
			if (compileSdkVersionAttr == nullptr || compileSdkVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<compileSdkVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			ss.str("");
			ss << compileSdkVersionAttr->value();
			ss >> data.compileSdkVersion;
			if (ss.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <compileSdkVersion> node";
				return;
			}
			ss.clear();
		}

		rapidxml::xml_node<> *buildToolsVersionNode = buildNode->first_node("buildToolsVersion");
		if (buildToolsVersionNode == nullptr)
		{
			errorOccurred = true;
			errorMsg = "<buildToolsVersion> node not found";
			return;
		}
		else
		{
			rapidxml::xml_attribute<> *buildToolsVersionAttr = buildToolsVersionNode->first_attribute("value");
			if (buildToolsVersionAttr == nullptr || buildToolsVersionAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<buildToolsVersion> \"value\" attribute does not exist or is empty";
				return;
			}

			ss.str("");
			ss << buildToolsVersionAttr->value();
			std::string v0, v1, v2;
			std::getline(ss, v0, '.');
			std::getline(ss, v1, '.');
			std::getline(ss, v2, '.');
			if (v1 == "")
			{
				v1 = "0";
			}
			if (v2 == "")
			{
				v2 = "0";
			}
			std::stringstream ss1;
			ss1.str("");
			ss1 << v0;
			ss1 >> data.buildToolsVersion[0];
			if (ss1.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <buildToolsVersion> node";
				return;
			}
			ss1.clear();
			ss1.str("");
			ss1 << v1;
			ss1 >> data.buildToolsVersion[1];
			if (ss1.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <buildToolsVersion> node";
				return;
			}
			ss1.clear();
			ss1.str("");
			ss1 << v2;
			ss1 >> data.buildToolsVersion[2];
			if (ss1.fail())
			{
				errorOccurred = true;
				errorMsg = "Invalid value for \"value\" attribute in <buildToolsVersion> node";
				return;
			}
			ss1.clear();
		}

		if (data.useMinSdkVersion && data.minSdkVersion == 0)
		{
			///????????????????????????????????????
			errorOccurred = true;
			errorMsg = "\"minSdkVersion\" can not be 0";
			return;
		}
		if (data.useMinSdkVersion  && data.useTargetSdkVersion && data.useMinSdkVersion > data.targetSdkVersion)
		{
			errorOccurred = true;
			errorMsg = "\"minSdkVersion\" must be lower or equal to \"targetSdkVersion\"";
			return;
		}
		if (data.useTargetSdkVersion && data.targetSdkVersion > data.compileSdkVersion)
		{
			errorOccurred = true;
			errorMsg = "\"targetSdkVersion\" must be lower or equal to \"compileSdkVersion\"";
			return;
		}
		if (data.targetSdkVersion > data.buildToolsVersion[0])
		{
			errorOccurred = true;
			errorMsg = "\"targetSdkVersion\" must be lower or equal to \"buildToolsVersion\"";
			return;
		}
		if (!data.useMinSdkVersion && data.minSdkVersion > data.compileSdkVersion)
		{
			data.minSdkVersion = data.compileSdkVersion;
		}
		if (!data.useTargetSdkVersion && data.targetSdkVersion > data.compileSdkVersion)
		{
			data.targetSdkVersion = data.compileSdkVersion;
		}
	}

	rapidxml::xml_attribute<> *buildArtefactAttr = buildNode->first_attribute("artefact");
	if ((buildArtefactAttr == nullptr || buildArtefactAttr->value_size() == 0) && needArtefact)
	{
		errorOccurred = true;
		errorMsg = "<build> \"artefact\" attribute does not exist or is empty";
		return;
	}
	if (buildArtefactAttr != nullptr)
	{
		data.buildArtefact = buildArtefactAttr->value();
		data.useBuildArtefact = true;
	}

	//sources
	rapidxml::xml_node<> *sourcesNode = node->first_node("sources");
	if (sourcesNode == nullptr)
	{
		errorOccurred = true;
		errorMsg = "<sources> node not found";
		return;
	}

	//manifest
	rapidxml::xml_node<> *manifestNode = sourcesNode->first_node("manifest");
	if (manifestNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Manifest] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<manifest> node not found";
		return;
	}
	if (manifestNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Manifest] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<manifest> node is not needed";
			return;
		}
		else
		{
			data.useManifest = true;

			rapidxml::xml_attribute<> *pathAttr = manifestNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<manifest> \"path\" attribute does not exist or is empty";
				return;
			}

			data.manifestPath = pathAttr->value();
		}
	}

	//proguard
	rapidxml::xml_node<> *proguardNode = sourcesNode->first_node("proguard");
	if (proguardNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Proguard] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<proguard> node not found";
		return;
	}
	if (proguardNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Proguard] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<proguard> node is not needed";
			return;
		}
		else
		{
			data.useProguard = true;

			rapidxml::xml_attribute<> *pathAttr = proguardNode->first_attribute("path");
			if (pathAttr == nullptr || pathAttr->value_size() == 0)
			{
				errorOccurred = true;
				errorMsg = "<proguard> \"path\" attribute does not exist or is empty";
				return;
			}

			data.proguardPath = pathAttr->value();
		}
	}

	//java
	rapidxml::xml_node<> *javaNode = sourcesNode->first_node("java");
	if (javaNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Java] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<java> node not found";
		return;
	}
	if (javaNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Java] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<java> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::Java] = true;

			processJ2ADirs(javaNode, J2AData::Directories::Java, data, errorOccurred, errorMsg);

			rapidxml::xml_node<> *preprocessNode = javaNode->first_node("preprocess");
			if (preprocessNode != nullptr)
			{

				data.useJavaPreprocess = true;

				processJ2APreprocess(preprocessNode, data, errorOccurred, errorMsg);
			}
		}
	}

	//aidl
	rapidxml::xml_node<> *aidlNode = sourcesNode->first_node("aidl");
	if (aidlNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::AIDL] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<aidl> node not found";
		return;
	}
	if (aidlNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::AIDL] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<aidl> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::AIDL] = true;

			processJ2ADirs(aidlNode, J2AData::Directories::AIDL, data, errorOccurred, errorMsg);
		}
	}

	//res
	rapidxml::xml_node<> *resNode = sourcesNode->first_node("res");
	if (resNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Res] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<res> node not found";
		return;
	}
	if (resNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Res] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<res> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::Res] = true;

			processJ2ADirs(resNode, J2AData::Directories::Res, data, errorOccurred, errorMsg);
		}
	}

	//assets
	rapidxml::xml_node<> *assetsNode = sourcesNode->first_node("assets");
	if (assetsNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Assets] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<assets> node not found";
		return;
	}
	if (assetsNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Assets] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<assets> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::Assets] = true;

			processJ2ADirs(assetsNode, J2AData::Directories::Assets, data, errorOccurred, errorMsg);
		}
	}

	//libs
	rapidxml::xml_node<> *libsNode = sourcesNode->first_node("libs");
	if (libsNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Libs] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<libs> node not found";
		return;
	}
	if (libsNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Libs] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<libs> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::Libs] = true;

			processJ2ADirs(libsNode, J2AData::Directories::Libs, data, errorOccurred, errorMsg);
		}
	}

	//jnilibs
	rapidxml::xml_node<> *jnilibsNode = sourcesNode->first_node("jniLibs");
	if (jnilibsNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::JniLibs] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<jniLibs> node not found";
		return;
	}
	if (jnilibsNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::JniLibs] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<jniLibs> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::JniLibs] = true;

			processJ2ADirs(jnilibsNode, J2AData::Directories::JniLibs, data, errorOccurred, errorMsg);

			processJ2AjniType(jnilibsNode/*, j2aPath*/, jnidefaulttype, data, errorOccurred, errorMsg);
		}
	}

	//native
	rapidxml::xml_node<> *nativeNode = sourcesNode->first_node("native");
	if (nativeNode == nullptr && J2AData::UseTag[data.buildType][J2AData::Tag::Native] == J2AData::Use::Yes)
	{
		errorOccurred = true;
		errorMsg = "<native> node not found";
		return;
	}
	if (nativeNode != nullptr)
	{
		if (J2AData::UseTag[data.buildType][J2AData::Tag::Native] == J2AData::Use::No)
		{
			errorOccurred = true;
			errorMsg = "<native> node is not needed";
			return;
		}
		else
		{
			data.useDirs[J2AData::Directories::Native] = true;

			processJ2ADirs(nativeNode, J2AData::Directories::Native, data, errorOccurred, errorMsg);
		}
	}
}

//void XMLParser::ProcessJava2Android(const std::string &filename, std::vector<std::string> &j2apaths, bool &errorOccurred, std::string &errorMsg)
//{
//	errorOccurred = false;
//	std::string xmlContent;
//	rapidxml::xml_document<> doc;
//	toXMLDoc(filename, xmlContent, doc, errorOccurred, errorMsg);
//	rapidxml::xml_node<> *rootNode = doc.first_node("java2android");
//	//processJava2AndroidGropup(rootNode, j2apaths, errorOccurred, errorMsg);
//	processJava2Android(rootNode, j2apaths, errorOccurred, errorMsg);
//	if (!errorOccurred)
//	{
//		errorMsg = "";
//	}
//}
void XMLParser::ProcessJava2Android(const std::string &filename, Java2Android &data, bool &errorOccurred, std::string &errorMsg)
{
	errorOccurred = false;
	std::string xmlContent;
	rapidxml::xml_document<> doc;
	toXMLDoc(filename, xmlContent, doc, errorOccurred, errorMsg);
	if (errorOccurred)
	{
		return;
	}
	rapidxml::xml_node<> *rootNode = doc.first_node("java2android");
	bool success = false;
	std::string folderpath = FileSystem::FolderPath(filename, success);
	if (!success)
	{
		//??
	}
	processJava2Android(rootNode, folderpath, data, errorOccurred, errorMsg);
	if (!errorOccurred)
	{
		errorMsg = "";
	}
}
void XMLParser::ProcessJava2AndroidProject(const std::string &filename, Java2Android &data, bool &errorOccurred, std::string &errorMsg)
{
	errorOccurred = false;
	std::string xmlContent;
	rapidxml::xml_document<> doc;
	toXMLDoc(filename, xmlContent, doc, errorOccurred, errorMsg);
	if (errorOccurred)
	{
		return;
	}
	rapidxml::xml_node<> *rootNode = doc.first_node("java2android");

	rapidxml::xml_node<> *projectNode = rootNode->first_node("project");
	if (projectNode != nullptr)
	{
		bool success = false;
		std::string folderpath = FileSystem::FolderPath(filename, success);
		if (!success)
		{
			//??
		}
		processJava2AndroidProject(projectNode, folderpath, data, errorOccurred, errorMsg);
		if (errorOccurred) return;
	}

	if (!errorOccurred)
	{
		errorMsg = "";
	}
}
void XMLParser::ProcessJava2AndroidLibs(const std::string &filename, Java2Android &data, bool &errorOccurred, std::string &errorMsg)
{
	errorOccurred = false;
	std::string xmlContent;
	rapidxml::xml_document<> doc;
	toXMLDoc(filename, xmlContent, doc, errorOccurred, errorMsg);
	if (errorOccurred)
	{
		return;
	}
	rapidxml::xml_node<> *rootNode = doc.first_node("java2android");

	rapidxml::xml_node<> *libsNode = rootNode->first_node("libs");
	if (libsNode != nullptr)
	{
		data.j2aPathsList.push_back(std::vector<std::string>());
		processJava2AndroidGropup(libsNode, data, errorOccurred, errorMsg);
		if (errorOccurred) return;
	}

	if (!errorOccurred)
	{
		errorMsg = "";
	}
}
void XMLParser::ProcessJ2A(const std::string &filename, const std::string &jniDefaultType, J2AData &data, bool &errorOccurred, std::string &errorMsg)
{
	errorOccurred = false;
	std::string xmlContent;
	rapidxml::xml_document<> doc;
	toXMLDoc(filename, xmlContent, doc, errorOccurred, errorMsg);
	if (errorOccurred)
	{
		return;
	}
	rapidxml::xml_node<> *rootNode = doc.first_node("j2a");
	processJ2A(rootNode, jniDefaultType, data, errorOccurred, errorMsg);
	if (!errorOccurred)
	{
		errorMsg = "";
	}
}
