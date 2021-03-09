using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;


namespace NsightSlnBuilder
{

	partial class Utils
	{

		internal static bool CopyDirectory(string source, string destination)
		{
			// Get the subdirectories for the specified directory.
			DirectoryInfo dir = new DirectoryInfo(source);
			DirectoryInfo[] dirs = dir.GetDirectories();

			if (!dir.Exists)
			{
				Status.ErrorMessage = "Source directory does not exist or could not be found: " + source;
				return false;
			}

			// If the destination directory doesn't exist, create it. 
			if (!Directory.Exists(destination))
			{
				Directory.CreateDirectory(destination);
			}


			if (!Directory.Exists(destination))
			{
				Status.ErrorMessage = "Could not create destination directory: " + destination;
				return false;
			}


			// Get the files in the directory and copy them to the new location.
			FileInfo[] files = dir.GetFiles();


			if (files.Length == 0 && dirs.Length == 0)
			{
				//PrintLineWarning("Source directory is empty: " + source);
			}
			
			foreach (FileInfo file in files)
			{
				string temppath = Path.Combine(destination, file.Name);
				file.CopyTo(temppath, true);
			}

			bool success = true;

			foreach (DirectoryInfo subdir in dirs)
			{
				string tempPath = Path.Combine(destination, subdir.Name);
				success = success && CopyDirectory(subdir.FullName, tempPath);
			}

			return success;
		}


		// ------------------------------------------------------------------------------


		internal static void Print(ConsoleColor color, string message)
		{
			System.Console.ForegroundColor = color;
			System.Console.Write(message);
			System.Console.ForegroundColor = ConsoleColor.Gray;
		}
		internal static void PrintLine(ConsoleColor color, string message)
		{
			Print(color, message + "\n");
		}

		internal static void PrintLine()
		{
			System.Console.WriteLine();
		}
		internal static void PrintSpace()
		{
			System.Console.Write(" ");
		}


		internal static void PrintLineInfo(string message)
		{
			PrintLine(ConsoleColor.Gray, message);
		}


		internal static void PrintLineSuccess(string message)
		{
			PrintLine(ConsoleColor.DarkGreen, message);
		}
		internal static void PrintLineWarning(string message)
		{
			PrintLine(ConsoleColor.Yellow, message);
		}
		internal static void PrintLineError(string message)
		{
			PrintLine(ConsoleColor.Red, message);
		}
		
	}

}
