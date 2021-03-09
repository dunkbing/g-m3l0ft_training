import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.util.zip.DeflaterOutputStream;

public class Compress
{
	public static void main(String args[])
	{
		final String MODE_STORED = "STORED";
		final String MODE_DEFLATED = "DEFLATED";
		byte buf[];
		
		try
		{
			int argsSize = args.length;
			if(argsSize < 4)
			{
				System.out.println("Error: Arguments are missing. "+argsSize);
				System.out.println("Usage: Compress <filesDir> <mode: STORED or DEFLATED> <OutDir> <OutName>");
				System.out.println("Or:    Compress <filesDir1,filesDir2> <mode: STORED or DEFLATED> <OutDir> <OutName>");
				return;
			}
			String dirPath[] = args[0].split(",");
			String mode = args[1];
			String outPath = args[2];
			String OUT_NAME = args[3];
			
			if(dirPath[0].length() == 0)
			{
				System.out.println("Error: No Files Directory Has Been Specified.");
				return;
			}
			if(mode.length() == 0 || (mode.compareToIgnoreCase(MODE_STORED) != 0 && mode.compareToIgnoreCase(MODE_DEFLATED) != 0))
			{
				System.out.println("Error: Mode Invalid. Current Valid Modes Are: " + MODE_STORED + " And " + MODE_DEFLATED);
				return;
			}
			if(outPath.length() == 0)
			{
				System.out.println("Error: No Output Directory Has Been Specified.");
				return;
			}
			
			File srcDir[] = new File[dirPath.length];
			
			for (int i = 0; i < dirPath.length; i++)
				srcDir[i] = new File(dirPath[i]);
				
			File currentFile = null;
			File outDir = new File(outPath);
			String mFiles[];
			int cfiles = 0;
			for (int i = 0; i < dirPath.length; i++)
			{
				mFiles = srcDir[i].list();
				if(!srcDir[i].isDirectory())
				{
					System.out.println("Error: " + dirPath[i] + ", Is Not A Directory.");
					return;
				}
			
				if(mFiles.length == 0)
				{
					cfiles ++;
					//System.out.println("Error: No Source Files Found In " + dirPath);
					//return;
				}
			}
			if(!outDir.isDirectory())
			{
				System.out.println("Error: " + outPath + ", Is Not A Directory.");
				return;
			}
			DataOutputStream mDOStream = new DataOutputStream(new DeflaterOutputStream(new FileOutputStream(outPath + "\\" + OUT_NAME)));
			for (int c = 0; c < dirPath.length; c++)
			{
				System.out.println("Files dir: " + dirPath);
				System.out.println("Compression mode: " + mode);
				System.out.println("Files dir: " + outDir+"\n");
				mFiles = srcDir[c].list();
				for (int i=0; i<mFiles.length; i++)
				{ 
				if(mFiles[i].compareToIgnoreCase(OUT_NAME) == 0)
						continue;
					if(mFiles[i].compareToIgnoreCase("sounds") == 0)
						continue;
					if(mFiles[i].compareToIgnoreCase(".svn") == 0)
						continue;
					currentFile = new File(dirPath[c] + "\\" + mFiles[i]);
					FileInputStream in = new FileInputStream(currentFile); 
					int fSize = (int)currentFile.length();
					buf = new byte[fSize];
					int len = in.read(buf);
					System.out.println("Adding " + mFiles[i] + ", len: " + len);
					mDOStream.write(buf, 0, len);
					in.close(); 
				} 
			}
			mDOStream.close(); 
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}