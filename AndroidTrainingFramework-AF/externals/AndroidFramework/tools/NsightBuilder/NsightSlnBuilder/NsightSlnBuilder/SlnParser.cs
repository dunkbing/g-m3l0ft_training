using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using System.IO;

namespace NsightSlnBuilder
{
	partial class SlnParser
	{

		internal static string GetSlnFromSln2Gcc(string sln2GccPath)
		{
			string slnFullPath = "";

			try
			{
				if (!File.Exists(sln2GccPath))
				{
					Status.ErrorMessage = "Sln2Gcc file does not exist: " + sln2GccPath;
					throw new Status.ControlledExitException();
				}

				Utils.PrintLineInfo("Reading " + sln2GccPath);

				XDocument sln2GccDoc = null;

				try
				{
					sln2GccDoc = XDocument.Load(sln2GccPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Sln2Gcc file is not a valid xml file: " + sln2GccPath;
					throw new Status.ControlledExitException();
				}

				XElement makefileElem = null;
				XElement slnElem = null;
				string slnLocalPath = "";

				try
				{
					makefileElem = sln2GccDoc.Elements().FirstOrDefault(x => x.Name.LocalName.Equals("Makefile"));
					slnElem = makefileElem.Elements().FirstOrDefault(x => x.Name.LocalName.Equals("Solution"));
					slnLocalPath = slnElem.Attributes().First(x => x.Name.LocalName.Equals("Path")).Value;
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Sln2Gcc format is not valid: " + sln2GccPath;
					throw new Status.ControlledExitException();
				}

				try
				{
					slnFullPath = Directory.GetParent(sln2GccPath).FullName + "\\" + slnLocalPath;
					slnFullPath = Path.GetFullPath(slnFullPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Solution path retrieved from sln2gcc is not valid: " + slnFullPath;
					throw new Status.ControlledExitException();
				}

				Utils.PrintLineSuccess("Done");
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
				Utils.PrintLine(); 
			}

			return slnFullPath;
		}


		internal static string GetSlnVersion(string slnFullPath)
		{
			string version = "";
			try
			{
				if (!File.Exists(slnFullPath))
				{
					Status.ErrorMessage = "Solution file does not exist: " + slnFullPath;
					throw new Status.ControlledExitException();
				}

				string slnText = "";
				try
				{
					slnText = File.ReadAllText(slnFullPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Could not read solution file: " + slnFullPath;
					throw new Status.ControlledExitException();
				}

				try
				{
					string substr = "Microsoft Visual Studio Solution File, Format Version";
					// awhaaa..?
					version = slnText.Substring(slnText.IndexOf(substr) + substr.Length);
					version = version.Substring(0, version.IndexOf("\n"));
					version = version.Trim();
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Could not read solution version: " + slnFullPath;
					throw new Status.ControlledExitException();
				}

				if (!(version.Contains("10.00") || 
						version.Contains("11.00") || 
						version.Contains("12.00") || 
						version.Contains("13.00")))
				{
					Status.ErrorMessage = "Invalid solution version in file: " + slnFullPath;
					throw new Status.ControlledExitException();
				}
			}
			catch (Status.ControlledExitException)
			{
				// nothing to handle, it's fine
			}
			catch (Exception ex)
			{
				Status.ErrorMessage = ex.Message + "\n" + ex.StackTrace;
			}	
			
			return version;
		}



		internal static List<string> GetProjectNames(string sln2GccPath)
		{
			var sln2GccDoc = XDocument.Load(sln2GccPath);
			var makefileElem = sln2GccDoc.Elements().FirstOrDefault(x => x.Name.LocalName.Equals("Makefile"));

			var localNameEnumerator = from elem in makefileElem.Elements()
									  where elem.Name.LocalName.Equals("Project")
									  select elem.Attributes().FirstOrDefault(x => x.Name.LocalName.Equals("Name")).Value;
			var localNameList = localNameEnumerator.ToList();

			return localNameList; 
		}



		internal static List<string> GetConvertedProjects(string slnFullPath, List<string> projectNames)
		{
			List<string> projectPaths = null;

			try
			{
				if (!File.Exists(slnFullPath))
				{
					Status.ErrorMessage = "Solution file does not exist: " + slnFullPath;
					throw new Status.ControlledExitException();
				}

				string slnText = "";
				try
				{
					slnText = File.ReadAllText(slnFullPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Could not read solution file: " + slnFullPath;
					throw new Status.ControlledExitException();
				}

				string slnBasePath = Directory.GetParent(slnFullPath).FullName;


				Utils.PrintLineInfo("Converting 2008 projects");
				Utils.PrintLine();


				int maxLength = 0;
				foreach (string localName in projectNames)
					maxLength = Math.Max(maxLength, localName.Length);


				int iProj = 0;

				projectPaths = new List<string>();
				foreach (var localName in projectNames)
				{
					iProj++;

					if (iProj < 10)
						Utils.PrintSpace();
					if (iProj < 100)
						Utils.PrintSpace();


					Utils.Print(ConsoleColor.DarkCyan, " " + iProj + " / " + projectNames.Count + ":   ");
					Utils.Print(ConsoleColor.Gray, localName);
					for (int i = 0; i <= maxLength - localName.Length; i++)
						Utils.PrintSpace();


					string strToMatch = "\"" + localName + "\"";
					int index = slnText.IndexOf(strToMatch);

					if (index < 0)
					{
						Utils.PrintLineWarning("Could not find in solution");
						continue;
					}


					string localPath = "";

					try
					{
						localPath = slnText.Substring(index + strToMatch.Length + 1);
						localPath = localPath.Substring(localPath.IndexOf('\"') + 1);
						localPath = localPath.Substring(0, localPath.IndexOf('\"'));
					}
					catch (Exception)
					{
						Utils.PrintLineWarning("Could not find in solution");
						continue;
					}

					string projectFullPath = "";
					try
					{
						projectFullPath = Path.GetFullPath(Path.Combine(slnBasePath, localPath));
					}
					catch (Exception)
					{
						Utils.PrintLineWarning("Project path is malformed");
						continue;
					}


					string projectNewPath = "";
					string projectConvertedPath = "";

					int extensionIndex = projectFullPath.IndexOf(".vcproj");

					if (extensionIndex > 0)
					{
						projectNewPath = projectFullPath.Substring(0, extensionIndex);
						projectConvertedPath = projectNewPath + "_nsight.vcxproj";
						projectNewPath += "_nsight.vcproj";
					}
					else
					{
						projectNewPath = projectFullPath;
						projectConvertedPath = projectNewPath + "_nsight.vcxproj";
					}

					bool convertedSuccess = true;

					if (!File.Exists(projectConvertedPath))
					{
						try
						{
							File.Copy(projectFullPath, projectNewPath);
						}
						catch (Exception)
						{
							Utils.PrintLineWarning("Could not copy");
							continue;
						}

						System.Diagnostics.Process process = System.Diagnostics.Process.Start("devenv.exe", "/Upgrade " + projectNewPath);
						convertedSuccess = process.WaitForExit(10 * 1000);
						// is 10 seconds enough?

						if (!convertedSuccess)
						{
							// cleanup 

							try
							{
								process.Kill();
							}
							catch (Exception)
							{ }

							try
							{
								if (File.Exists(projectConvertedPath))
									File.Delete(projectConvertedPath);
							}
							catch (Exception)
							{ }
						}
					}
					else
					{
						convertedSuccess = true;
					}


					projectPaths.Add(projectConvertedPath);

					if (convertedSuccess)
					{
						Utils.PrintLineSuccess("Done");
					}
					else
					{
						Utils.PrintLineWarning("Could not upgrade");
					}

				}
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
				Utils.PrintLine();
			}

			return projectPaths;
		}

	}
}
