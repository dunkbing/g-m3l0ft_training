//#if USE_DOWNLOAD_MANAGER
package APP_PACKAGE.installer.utils;

public interface Defs
{
	static final int BUFFER_SIZE = 128 * 1024;
	static final int MAX_SECTIONS = 8;
	static final int SECTION_TIMEOUT = 15000;
	
	static final String SECTION_FILE_NAME = "section.";
	static final String JOINED_FILE_NAME = "joinedFile.zip";
}

//#endif