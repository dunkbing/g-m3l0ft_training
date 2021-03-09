using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using EnvDTE;
using EnvDTE80;

namespace NsightSlnBuilder
{
	partial class SlnManager
	{
		static DTE2 dte2010;
		static bool inited;


		static SlnManager()
		{
			dte2010 = null;
			inited = false;
		}

		internal static void Init()
		{
			if (inited)
			{
				return;
			}

			try
			{
				// Get Visual Studio 2010
				Type type = System.Type.GetTypeFromProgID("VisualStudio.DTE.10.0");

				if (type == null)
				{
					Status.ErrorMessage = "Could not find Visual Studio 2010 on the machine.";
					throw new Status.ControlledExitException();
				}

				Utils.PrintLineInfo("Starting a Visual Studio instance");

				dte2010 = (DTE2)System.Activator.CreateInstance(type, true);

				if (dte2010 == null)
				{
					Status.ErrorMessage = "Could not start a Visual Studio 2010 instance.";
					throw new Status.ControlledExitException();
				}

				dte2010.SuppressUI = true;

				MessageFilter.Register();

				Utils.PrintLineSuccess("Done");
				Utils.PrintLine();
				Utils.PrintLineWarning("If you get Visual Studio pop-ups, please click and dismiss them.");
				Utils.PrintLineWarning("Otherwise, this script will remain hung when trying to read / write a solution.");

				inited = true;
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
		}



		internal static void Cleanup()
		{
			if (!inited)
			{
				return;
			}

			if (dte2010 != null)
			{
				dte2010.Quit();
			}

			try
			{
				MessageFilter.Revoke();
			}
			catch (Exception)
			{
 				// not sure what message filter does... let alone revoke.
			}

			inited = false;
		}



		internal static List<string> GetProjectsFromSln(string androidSlnPath, List<string> projectNames)
		{
			List<string> projectPaths = null;

			try
			{
				Init();

				if (!Status.Success)
					throw new Status.ControlledExitException();


				Utils.PrintLineInfo("Reading solution " + androidSlnPath);
				Utils.PrintLineInfo("This will be very slow. Seriously, go get a cup of coffee or something.");


				Object tempSln = dte2010.Solution;
				if (tempSln == null)
				{
					Status.ErrorMessage = "Could not load solution handler.";
					throw new Status.ControlledExitException();
				}

				Solution2 inputSln = (Solution2)tempSln;

				try
				{
					// Slow as fuck.
					inputSln.Open(androidSlnPath);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Visual Studio solution is not valid: " + androidSlnPath;
					throw new Status.ControlledExitException();
				}

				if (!inputSln.IsOpen)
				{
					Status.ErrorMessage = "Could not properly open Visual Studio solution: " + androidSlnPath;
					throw new Status.ControlledExitException();
				}


				var projects = inputSln.Projects;

				if (projects.Count == 0)
				{
					Utils.PrintLineWarning("The solution doesn't appear to have any projects.");
				}

				projectPaths = new List<string>();
				foreach (var obj in projects)
				{
					Project project = (Project)obj;
					foreach (var name in projectNames)
					{
						if (project.Name.Contains(name))
						{
							projectPaths.Add(project.FullName);
							break;
						}
					}
				}

				Utils.PrintLineSuccess("Done");

				inputSln.Close(false);
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



		internal static void AddProjectsToSln(string finalSlnName, List<string> projectNames)
		{
			try
			{
				Init();

				if (!Status.Success)
					throw new Status.ControlledExitException();


				Utils.PrintLineInfo("Writing solution " + finalSlnName);


				Object tempSln = dte2010.Solution;
				if (tempSln == null)
				{
					Status.ErrorMessage = "Could not load solution handler.";
					throw new Status.ControlledExitException();
				}

				Solution2 outputSln = (Solution2)tempSln;

				try
				{
					outputSln.Open(finalSlnName);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Could not properly open Visual Studio solution: " + finalSlnName;
					throw new Status.ControlledExitException();
				}

				if (!outputSln.IsOpen)
				{
					Status.ErrorMessage = "Could not properly open Visual Studio solution: " + finalSlnName;
					throw new Status.ControlledExitException();
				}

				Utils.PrintLine();

				int maxLength = 0;
				foreach (string projectName in projectNames)
					maxLength = Math.Max(maxLength, projectName.Length);


				int iProj = 0;
				// Again, slow as fuck.
				foreach (string projectName in projectNames)
				{
					iProj++;

					if (iProj < 10)
						Utils.PrintSpace();
					if (iProj < 100)
						Utils.PrintSpace();


					Utils.Print(ConsoleColor.DarkCyan, " " + iProj + " / " + projectNames.Count + ":   ");
					Utils.Print(ConsoleColor.Gray, projectName);
					for (int i = 0; i <= maxLength - projectName.Length; i++)
						Utils.PrintSpace();

					try
					{
						outputSln.AddFromFile(projectName, false);
						Utils.PrintLineSuccess("Done");
					}
					catch (Exception)
					{
						Utils.PrintLineWarning("Could not add to sln");
						continue;
					}
				}

				try
				{
					outputSln.SaveAs(finalSlnName);
				}
				catch (Exception)
				{
					Status.ErrorMessage = "Could not save Visual Studio solution: " + finalSlnName;
					throw new Status.ControlledExitException();
				}


				Utils.PrintLine();
				Utils.PrintLineSuccess("All Done");
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
		}
	}
}
