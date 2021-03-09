using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace NsightSlnBuilder
{
	class Status
	{
		static Status()
		{
			success = true;
		}

		private static bool success;
		private static string errorMessage;

		internal static bool Success 
		{
			get {  return success; }

			set 
			{
				success = value;
				if (success)
					errorMessage = "";
				else
				{
					if (errorMessage.Length == 0)
						errorMessage = "Generic error.";
				}
			}
		}

		internal static string ErrorMessage
		{
			get { return errorMessage; }

			set 
			{
				errorMessage = value;
				if (errorMessage.Length > 0)
					success = false;
				else
					success = true;
			}
		}

		internal class ControlledExitException : Exception
		{
			override public string ToString()
			{
				return "Controlled Exit Exception";
			}
		}
	}
}
