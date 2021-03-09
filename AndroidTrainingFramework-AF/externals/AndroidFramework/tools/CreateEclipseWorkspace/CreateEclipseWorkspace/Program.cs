using System;
using System.Collections.Generic;
using System.Text;
using System.IO;


namespace CreateEclipseWorkspace
{
    class Program
    {
        public static String Version = "0.0.4";
        public static String PathToPackage = "";
        public static String PathToDestination = "";
        public static String SkipedFiles = "";
        public static List<String> listOfFiles = new List<String>();

        public static Boolean omitFile = false;

        public static int FUNCTION_COPY = 0;
        public static int FUNCTION_COPY_AND_CREATE_STRUCTURE = 1;

        static void Main(string[] args)
        {
            Console.WriteLine("CreateEclipseWorkspace Ver" + Version);
            Console.WriteLine("(C) 2012 Gameloft BUC");

            if (args.Length <= 0)
            {
                Console.WriteLine("Usage: CreateEclipseWorkspace <Path to Package> <Path to destionation folder> <SkipFiles.txt list with skipped files>");
                return;
            }

            PathToPackage = args[0];
            PathToDestination = args[1];
            SkipedFiles = args[2];

            Console.WriteLine("PathToPackage=" + PathToPackage);
            Console.WriteLine("PathToDestination=" + PathToDestination);
            Console.WriteLine("SkippedFiles=" + SkipedFiles);

            if (!System.IO.Directory.Exists(PathToPackage))
            {
                Console.WriteLine("The PathToPackage=\"" + PathToPackage + "\" does not exist!");
                return;
            }

            if (!System.IO.Directory.Exists(PathToDestination))
            {
                System.IO.Directory.CreateDirectory(PathToDestination);
                Console.WriteLine("Creating: " + PathToDestination);
            }

            if (System.IO.File.Exists(SkipedFiles))
            {
                StreamReader sr = new StreamReader(SkipedFiles);
                String line;
                while ((line = sr.ReadLine()) != null)
                    listOfFiles.Add(line);
                
            }
            
            String src = PathToPackage + "\\libs";
            String dst = PathToDestination + "\\libs";
            if (System.IO.Directory.Exists(dst)) System.IO.Directory.Delete(dst, true);
            ForEachFile(FUNCTION_COPY, src, dst, true);

            
            src = PathToPackage + "\\_work\\assets";
            dst = PathToDestination + "assets";
            if (System.IO.Directory.Exists(dst)) System.IO.Directory.Delete(dst, true);
            ForEachFile(FUNCTION_COPY, src, dst, true);

            
            src = PathToPackage + "\\_work\\res";
            dst = PathToDestination + "\\res";
            if (System.IO.Directory.Exists(dst)) System.IO.Directory.Delete(dst, true);
            ForEachFile(FUNCTION_COPY, src, dst, true);


            src = PathToPackage + "\\_work";
            dst = PathToDestination + "\\";
            ForEachFile(FUNCTION_COPY, src, dst, false);//only files


            src = PathToPackage + "\\_work\\src";
            dst = PathToDestination + "\\src";
            if (System.IO.Directory.Exists(dst)) System.IO.Directory.Delete(dst, true);
            ForEachFile(FUNCTION_COPY_AND_CREATE_STRUCTURE, src, dst, true);

            Console.WriteLine("DONE!");
        }

        static void ForEachFile(int function, String src_folder, String dst_folder, bool recursive)
        {
            //Console.WriteLine("ForEachFile src_folder=" + src_folder + ", dst_folder=" + dst_folder);

            if (!System.IO.Directory.Exists(src_folder)) return;

        
            if (recursive)
            {
                string[] folders = System.IO.Directory.GetDirectories(src_folder);

                foreach (string folder in folders)
                {
                 
                    DirectoryInfo dirinfo = new DirectoryInfo(folder);
                    //Console.WriteLine("Attributes: " + dirinfo.Attributes.ToString());

                    if ((dirinfo.Attributes & FileAttributes.Hidden) == FileAttributes.Hidden) continue;

                    if (function == FUNCTION_COPY)
                    {
                        String new_dst_folder = dst_folder + "\\" + folder.Substring(src_folder.Length);

                        if (!System.IO.Directory.Exists(new_dst_folder))
                        {
                            System.IO.Directory.CreateDirectory(new_dst_folder);
                        }

                        ForEachFile(function, folder, new_dst_folder, recursive);
                    }
                    else 
                    if( function == FUNCTION_COPY_AND_CREATE_STRUCTURE)
                    {
                        ForEachFile(function, folder, dst_folder, recursive);
                    }

                    
                }
            }

            string[] files = System.IO.Directory.GetFiles(src_folder);

            foreach (string file in files)
            {
                omitFile = false;
                foreach (string skipfile in listOfFiles)
                {
                    string[] words = file.Split('\\');
                    int numberOfWords = words.Length;
                    if (words[numberOfWords - 1] == skipfile)
                    {
                        omitFile = true;
                    }
                }

                if (!omitFile)
                {
                    String fileName = System.IO.Path.GetFileName(file);
                     
                    if (function == FUNCTION_COPY)
                    {
                        if (!System.IO.Directory.Exists(dst_folder))
                        {
                            System.IO.Directory.CreateDirectory(dst_folder);
                        }

                        String destFile = System.IO.Path.Combine(dst_folder, fileName);

                        System.IO.File.Copy(file, destFile, true);

                        //Console.WriteLine("destFile=" + destFile);
                    }
                    else
                        if (function == FUNCTION_COPY_AND_CREATE_STRUCTURE)
                        {
                            try
                            {
                                //Console.WriteLine("file=" + file);
                                // Create an instance of StreamReader to read from a file.
                                // The using statement also closes the StreamReader.
                                using (StreamReader sr = new StreamReader(file))
                                {
                                    bool isAComment = false;
                                    String line;
                                    // Read and display lines from the file until the end of
                                    // the file is reached.
                                    while ((line = sr.ReadLine()) != null)
                                    {
                                        line.Trim();

                                        if (line.StartsWith("//")) continue;

                                        if (line.Contains("/*")) isAComment = true;
                                        if (line.Contains("*/")) isAComment = false;

                                        if (isAComment) continue;

                                        if (line.Contains("package"))
                                        {
                                            //Console.WriteLine(line);

                                            String new_dst_folder = dst_folder + "\\";

                                            if (fileName == "R.java")
                                            {
                                                new_dst_folder = new_dst_folder.Replace("\\src\\", "\\gen\\");
                                            }


                                            String pathwithdots = line.Substring("package".Length).Replace(';', ' ');
                                            string[] words = pathwithdots.Split('.');
                                            foreach (string word in words)
                                            {
                                                new_dst_folder += word.TrimStart().TrimEnd() + "\\";
                                            }





                                            if (!System.IO.Directory.Exists(new_dst_folder))
                                            {
                                                System.IO.Directory.CreateDirectory(new_dst_folder);
                                            }

                                            //Console.WriteLine(new_dst_folder + fileName);

                                            System.IO.File.Copy(file, new_dst_folder + fileName, true);

                                            break;
                                        }
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                // Let the user know what went wrong.
                                Console.WriteLine("The file could not be read:");
                                Console.WriteLine(e.Message);
                            }



                        }
                }

                
            }
        }
    }
}
