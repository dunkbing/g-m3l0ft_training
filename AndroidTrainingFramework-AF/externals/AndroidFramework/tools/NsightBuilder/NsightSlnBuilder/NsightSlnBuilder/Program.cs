using System;
using System.Collections.Generic;
using System.Text;


namespace NsightSlnBuilder
{

	public partial class MainClass
	{

		[STAThread]
		public static void Main(string[] args)
		{
			string output = "";
			try
			{
				Utils.PrintLine();

				if (args.Length < 4)
				{
					Status.ErrorMessage = "Argument set is invalid. Required paths are: core package, game specific, nsight proj, sln2gcc.";
					throw new Status.ControlledExitException();
				}

				string corePackagePath = args[0];
				string gameSpecificPath = args[1];
				string initialSlnPath = args[2];
				string sln2GccPath = args[3];


				Builder builder = new Builder();
				if (builder == null)
				{
					Status.ErrorMessage = "Could not create Nsight Solution Builder. Aborting.";
					throw new Status.ControlledExitException();
				}


				output = builder.Create(corePackagePath, gameSpecificPath, initialSlnPath, sln2GccPath);					
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
				if (Status.Success)
				{
					Utils.PrintLineSuccess("Nsight Debug Solution was successfully created.");
					Utils.PrintLineInfo("Check " + output);
					Utils.PrintLine();
				}
				else
				{
					Utils.PrintLineWarning("Could not create Nsight Debug Solution. Error description:");
					Utils.PrintLineWarning(Status.ErrorMessage);
					Utils.PrintLine();
				}
			}
		}

	}

}
