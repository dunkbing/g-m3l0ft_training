package APP_PACKAGE.installer.utils;
public class PackFile {
    private String folder;
    private String name;
    private String zipname;
    private long length;
    private long checksum;
    private int ziplength;
    private int offset;
	private int ID;
	private String URL;
	private long unsplittedLength;
	private String md5;

	public PackFile()
	{
		this.folder = "";
		this.name = "";
		this.zipname = "";
		this.length = 0;
		this.checksum = 0;
		this.ziplength = 0;
		this.offset = 0;
		this.ID = 0;
		this.URL = "";
		this.md5 = "";
	}
	public PackFile(String folder, String name, String zipname, long length, long checksum, int ziplength, int offset, int ID)
	{
		this.folder = folder;
		this.name = name;
		this.zipname = zipname;
		this.length = length;
		this.checksum = checksum;
		this.ziplength = ziplength;
		this.offset = offset;
		this.ID = ID;
		this.URL = "";
	}
	public PackFile(String folder, String name, String zipname, long length, long checksum, int ziplength, int offset, int ID, String URL)
	{
		this.folder = folder;
		this.name = name;
		this.zipname = zipname;
		this.length = length;
		this.checksum = checksum;
		this.ziplength = ziplength;
		this.offset = offset;
		this.ID = ID;
		this.URL = URL;
	}
    /**
     * @return the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

     /**
     * @return the zipname
     */
    public String getZipName() {
        return zipname;
    }

    /**
     * @param name the zipname to set
     */
    public void setZipName(String name) {
        this.zipname = name;
    }
    /**
     * @return the lenght
     */
    public long getLength() {
        return length;
    }

    /**
     * @param lenght the lenght to set
     */
    public void setLength(long length) {
        this.length = length;
    }

    /**
     * @return the Checksum
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * @param value the Checksum to set
     */
    public void setChecksum(long value) {
        this.checksum = value;
    }

    /**
     * @return the zip lenght
     */
    public int getZipLength() {
        return ziplength;
    }

    /**
     * @param lenght the zip lenght to set
     */
    public void setZipLength(int length) {
        this.ziplength = length;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }
	
	/**
     * @param md5 to set
     */
	public void setMD5(String value)
	{
		md5 = value;
	}
	
	public String getMD5()
	{
		return md5;
	}

    /**
     * @param offset the ID to set
     */
    public void setID(int id) {
        this.ID = id;
    }
	public String getURL() {
        return this.URL;
    }
	public void setURL(String url) {
        this.URL = url;
    }
	 public long getUnsplittedLength() {
        return unsplittedLength;
    }
    public void setUnsplittedLength(long unsplittedLength) {
        this.unsplittedLength = unsplittedLength;
    }
}
