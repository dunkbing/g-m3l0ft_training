using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.IO;


namespace NsightSlnBuilder
{
	partial class Builder
	{
		string corePackagePath;
		string gameSpecificPath;
		string sln2GccPath;
		string initialSlnPath;

		string finalSlnPath;
		string finalSlnName;

		string androidSlnPath;
		string androidSlnVersion;



		internal string Create(string _corePackagePath, string _gameSpecificPath, string _initialSlnPath, string _sln2GccPath)
		{
			string output = "";

			try
			{
				Init(_corePackagePath, _initialSlnPath, _gameSpecificPath, _sln2GccPath);

				if (!Status.Success)
					throw new Status.ControlledExitException();


				// Copy nsight sln to gamespecific
				Utils.CopyDirectory(initialSlnPath, finalSlnPath);

				if (!Status.Success)
					throw new Status.ControlledExitException();


				// Get project names
				List<string> projectNames = SlnParser.GetProjectNames(sln2GccPath);

				if (!Status.Success)
					throw new Status.ControlledExitException();


				// Get project paths
				var projectPaths = GetFinalProjects(projectNames);

				if (!Status.Success)
					throw new Status.ControlledExitException();

				// Add projects to new sln
				SlnManager.AddProjectsToSln(finalSlnName, projectPaths);

				CopyAdditionalFiles();

				if (!Status.Success)
					throw new Status.ControlledExitException();

				output = finalSlnPath;
			}
			catch (Status.ControlledExitException)
			{
				// nothing to handle, it's fine
			}
			catch (Exception ex)
			{
				Status.ErrorMessage = ex.Message + "\n" + ex.StackTrace;
			}
			finally
			{
				Cleanup();
			}

			return output;	
		}


				
		void Init(string _corePackagePath, string _initialSlnPath, string _gameSpecificPath, string _sln2GccPath)
		{
			try
			{
				try
				{
					corePackagePath = Path.GetFullPath(_corePackagePath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Invalid path: corePackagePath = " + _corePackagePath;
					throw new Status.ControlledExitException();
				}


				try
				{
					gameSpecificPath = Path.GetFullPath(_gameSpecificPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Invalid path: gameSpecificPath = " + _gameSpecificPath;
					throw new Status.ControlledExitException();
				}

				
				try
				{
					sln2GccPath = Path.GetFullPath(_sln2GccPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Invalid path: sln2GccPath = " + _sln2GccPath;
					throw new Status.ControlledExitException();
				}


				try
				{
					initialSlnPath = Path.GetFullPath(_initialSlnPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Invalid path: initialSlnPath = " + _initialSlnPath;
					throw new Status.ControlledExitException();
				}


				// Paths have been validated

				finalSlnPath = Path.GetFullPath(Path.Combine(_gameSpecificPath, "NsightSolution"));
				finalSlnName = Path.GetFullPath(Path.Combine(finalSlnPath, "NsightDebug.sln"));


				androidSlnPath = SlnParser.GetSlnFromSln2Gcc(sln2GccPath);

				if (!Status.Success)
					throw new Status.ControlledExitException();

				androidSlnVersion = SlnParser.GetSlnVersion(androidSlnPath);

				if (!Status.Success)
					throw new Status.ControlledExitException();

				SlnManager.Init();
			}
			catch (Status.ControlledExitException)
			{
				// nothing to handle, it's fine
			}
			catch (Exception ex)
			{
				Status.ErrorMessage = ex.Message + "\n" + ex.StackTrace;
			}	
		}



		List<string> GetFinalProjects(List<string> projectNames)
		{
			List<string> projects = null;
			
			if (androidSlnVersion.Contains("10.00"))
			{
				projects = SlnParser.GetConvertedProjects(androidSlnPath, projectNames);
			}
			else
			{
				projects = SlnManager.GetProjectsFromSln(androidSlnPath, projectNames);
			}			

			return projects;
		}



		void CopyAdditionalFiles()
		{
			string nsightDebugPath = Path.Combine(gameSpecificPath, "NsightSolution", "NSightDebug");
			nsightDebugPath = Path.GetFullPath(nsightDebugPath);


			// xcopy /Y   	%ANDROID_PACKAGE_DIR%\bin\*_DEBUG.apk   				%ANDROID_FRAMEWORK_CONFIG%\NsightSolution\NSightDebug\project.apk	 

			try
			{
				string binPath = Path.Combine(corePackagePath, "bin");
				string[] binFiles = Directory.GetFiles(binPath);

				string newestApk = "";
				DateTime newestTime = DateTime.MinValue;

				foreach (var fileName in binFiles)
				{
					if (!fileName.EndsWith("apk"))
						continue;

					DateTime fileTime = File.GetLastWriteTime(fileName);
					int dif = DateTime.Compare(newestTime, fileTime);

					if (dif < 0)
					{
						newestApk = fileName;
						newestTime = fileTime;
					}
				}

				string srcApkPath = Path.Combine(corePackagePath, "bin", newestApk);
				srcApkPath = Path.GetFullPath(srcApkPath);

				string dstApkPath = Path.Combine(nsightDebugPath, "project.apk");
				dstApkPath = Path.GetFullPath(dstApkPath);

				Utils.PrintLineInfo("Copying " + srcApkPath);
				Utils.PrintLineInfo("to      " + dstApkPath);

				File.Copy(srcApkPath, dstApkPath, true);

				Utils.PrintLineSuccess("Done");
				Utils.PrintLine();
			}
			catch (Exception)
			{
				Utils.PrintLineWarning("Could not copy debug apk to solution folder. You need to do it manually.");
				Utils.PrintLine();
			}


			// xcopy /Y   	%ANDROID_PACKAGE_DIR%\libs\armeabi-v7a\libGame.so   	%ANDROID_FRAMEWORK_CONFIG%\NsightSolution\NSightDebug\libs\libGame.so 

			try
			{
				string srcSoPath = Path.Combine(corePackagePath, "libs", "armeabi-v7a", "libGame.so");
				srcSoPath = Path.GetFullPath(srcSoPath);

				string dstSoPath = Path.Combine(nsightDebugPath, "libs", "libGame.so");
				dstSoPath = Path.GetFullPath(dstSoPath);

				Utils.PrintLineInfo("Copying " + srcSoPath);
				Utils.PrintLineInfo("to      " + dstSoPath);

				File.Copy(srcSoPath, dstSoPath, true);

				Utils.PrintLineSuccess("Done");
				Utils.PrintLine();
			}
			catch (Exception)
			{
				Utils.PrintLineWarning("Could not copy libGame.so to solution folder. You need to do it manually.");
				Utils.PrintLine();
			}


			// xcopy /Y 	%ANDROID_PACKAGE_DIR%\_work\AndroidManifest.xml 		%ANDROID_FRAMEWORK_CONFIG%\NsightSolution\NSightDebug\AndroidManifest.xml	 

			try
			{
				string srcManifestPath = Path.Combine(corePackagePath, "_work", "AndroidManifest.xml");
				srcManifestPath = Path.GetFullPath(srcManifestPath);

				string dstManifestPath = Path.Combine(nsightDebugPath, "AndroidManifest.xml");
				dstManifestPath = Path.GetFullPath(dstManifestPath);

				Utils.PrintLineInfo("Copying " + srcManifestPath);
				Utils.PrintLineInfo("to      " + dstManifestPath);

				File.Copy(srcManifestPath, dstManifestPath, true);

				Utils.PrintLineSuccess("Done");
				Utils.PrintLine();
			}
			catch (Exception)
			{
				Utils.PrintLineWarning("Could not copy manifest to solution folder. You need to do it manually.");
				Utils.PrintLine();
			}


			// xcopy /Y 	%ANDROID_FRAMEWORK_CONFIG%\build.xml			 					%ANDROID_FRAMEWORK_CONFIG%\NsightSolution\NSightDebug\build.xml	

			try
			{
				string srcBuildPath = Path.Combine(gameSpecificPath, "build.xml");
				srcBuildPath = Path.GetFullPath(srcBuildPath);

				string dstBuildPath = Path.Combine(nsightDebugPath, "build.xml");
				dstBuildPath = Path.GetFullPath(dstBuildPath);

				Utils.PrintLineInfo("Copying " + srcBuildPath);
				Utils.PrintLineInfo("to      " + dstBuildPath);

				File.Copy(srcBuildPath, dstBuildPath, true);

				Utils.PrintLineSuccess("Done");
				Utils.PrintLine();
			}
			catch (Exception)
			{
				Utils.PrintLineWarning("Could not copy build.xml to solution folder. You need to do it manually.");
				Utils.PrintLine();
			}

			
		}



		void Cleanup()
		{
			SlnManager.Cleanup();
			System.Console.ForegroundColor = ConsoleColor.White;
		}

		
	}
}
