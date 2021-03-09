using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace GetRelativePath
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {


                if (args.Length < 2)
                {
                    Console.WriteLine("error: missing arguments! use it like this: GetRelativePath.exe <fromPath> <toPath> [separator]");
                }
                else
                {
                    String relPath = MakeRelativePath(args[0], args[1]);
                    if (relPath != null)
                    {
                        String separator = "/";
                        if (args.Length == 3)
                            separator = args[2];

                        relPath = relPath.Replace("" + Path.DirectorySeparatorChar, separator);
                        Console.WriteLine(relPath);
                    }
                    else
                    {
                        Console.WriteLine("error_GetRelativePath_relative_path_is_null");
                    }
                }
            }
            catch(Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }


        /// <summary>
        /// Creates a relative path from one file or folder to another.
        /// </summary>
        /// <param name="fromPath">Contains the directory that defines the start of the relative path.</param>
        /// <param name="toPath">Contains the path that defines the endpoint of the relative path.</param>
        /// <param name="dontEscape">Boolean indicating whether to add uri safe escapes to the relative path</param>
        /// <returns>The relative path from the start directory to the end path.</returns>
        /// <exception cref="ArgumentNullException"></exception>
        public static String MakeRelativePath(String fromPath, String toPath)
        {
            if (String.IsNullOrEmpty(fromPath)) throw new ArgumentNullException("fromPath");
            if (String.IsNullOrEmpty(toPath)) throw new ArgumentNullException("toPath");

            if (!Path.IsPathRooted(fromPath) || !Path.IsPathRooted(toPath))
            {
                return "error_GetRelativePath_path_is_already_relative";
            }

            string doubleSeparator = "" + Path.DirectorySeparatorChar + Path.DirectorySeparatorChar;

            fromPath = fromPath.Replace('/', Path.DirectorySeparatorChar);
            while (fromPath.IndexOf(doubleSeparator) >= 0)
            {
                fromPath = fromPath.Replace(doubleSeparator, "" + Path.DirectorySeparatorChar);
            }
            if (fromPath[fromPath.Length - 1] != Path.DirectorySeparatorChar)
                fromPath += Path.DirectorySeparatorChar;

            toPath = toPath.Replace('/', Path.DirectorySeparatorChar);
            while (toPath.IndexOf(doubleSeparator) >= 0)
            {
                toPath = toPath.Replace(doubleSeparator, "" + Path.DirectorySeparatorChar);
            }
            if (toPath[toPath.Length - 1] != Path.DirectorySeparatorChar)
                toPath += Path.DirectorySeparatorChar;



            Uri fromUri = new Uri(fromPath);
            Uri toUri = new Uri(toPath);

            Uri relativeUri = fromUri.MakeRelativeUri(toUri);
            fromUri = null;
            toUri = null;
            String relativePath = Uri.UnescapeDataString(relativeUri.ToString());

            return relativePath.Replace('/', Path.DirectorySeparatorChar);
        }
    }
}
