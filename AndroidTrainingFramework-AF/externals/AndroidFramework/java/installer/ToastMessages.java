package APP_PACKAGE.installer;

public final class ToastMessages 
{
	public static final class GameInstallerPiracyError
	{
		public static final int HEPInvalidDevice = 110;
		public static final int VerizonLicenceError = 111;
		public static final int GloftLicenceError = 112;
		public static final int TMobileLicenceError = 113;
	}
	
	public static final class MarkerInstallerChecks
	{
		public static final int ResourceValuesError = 180;
		public static final int CheckSpace = 181;
		public static final int UnsupportedScreen = 182;
		public static final int VerifyingCheckSum = 183;
		public static final int LatestVersionError = 184;
	}
	
	public static final class GameInstallerChecks
	{
		public static final int FilesNotValid = 201;
		public static final int PackInfoNull = 202;
		public static final int ResourceValuesError = 203;
		public static final int DataStreamError = 204;
	}
	
	public static final class GameInstaller_getDataStream
	{
		public static final int StatusFileNotFound = 220;
		public static final int SocketTimeoutException = 221;
		public static final int FileNotFoundException = 222;
		public static final int Exception = 223; 
	}
	
	public static final class GameInstallerNoWifi
	{
		public static final int GLoftLogoNoWifi = 240;
		public static final int GLoftLogoPackFileNoWifi = 241;
		public static final int HEPUpdateFinishNoWifi = 242;
		public static final int HEPUpdateFinishPackFileNoWifi = 243;
		public static final int DownloadFilesErrorYes = 244;
		public static final int DownloadFilesQuestionYes = 245;
		public static final int DownloadManagerNoWifi = 246;
	}
	
	public static final class GameInstallerCantReachServer
	{
		public static final int GLoftLogoCantReachServer = 260;
		public static final int GLoftLogoPackFileCantReachServer = 261;
		public static final int HEPUpdateFinishCantReachServer = 262;
		public static final int HEPUpdateFinishPackFileCantReachServer = 263;
	}
	
	public static final class GameInstallerSingleFileDownload
	{
		public static final int Failed = 500;
		public static final int DownloadFileError = 501;
		public static final int ExtractionError = 502;
		public static final int SocketException = 503;
		public static final int SocketTimeoutException = 504;
		public static final int FileNotFoundException = 505;
		public static final int IOException = 506;
		public static final int NullPointerException = 507;
		public static final int Exception = 508;
		public static final int DeleteErrorFileException = 509;
		public static final int SocketExceptionCreateDLStream = 510;
		public static final int SocketTimeoutExceptionCreateDLStream = 511;
		public static final int FileNotFoundExceptionCreateDLStream = 512;
		public static final int IOExceptionCreateDLStream = 513;
		public static final int ExceptionCreateDLStream = 514;
		public static final int createDLStreamFileNotFoundError = 515;
		public static final int closeDLStreamException = 516;
		public static final int WifiEnabledWhenUsing3G = 517;
	}

//#if USE_DOWNLOAD_MANAGER
	public static final class Downloader
	{
		public static final int Failed = 550;
		public static final int InitializeError = 551;
		public static final int InitializeRetryError = 552;
		public static final int StartThreadError = 553;
		public static final int StopError = 554;
	}

	public static final class Section
	{
		public static final int InitializeError = 560;
		public static final int InitializeSocketError = 561;
		public static final int RunSocketTimeoutError = 562;
		public static final int RunException = 563;
		public static final int getOutputStreamError = 564;
		public static final int RunZipEntryNull = 570;
		public static final int RunLzmaFileTooShort = 580;
		public static final int RunLzmaCantReadSize = 581;
		public static final int RunLzmaIncorrectProperties = 582;
		public static final int RunLzmaDecodeError = 583;
	}
//#endif

}