import os, sys
import zlib
import shutil

#def crc(fileName, excludeLine="", includeLine=""):
def crc(fileName):
	try:
		fd = open(fileName, "rb")
	except IOError:
		print "Unable to open the file in readmode:", filename
		return
		
	eachLine = fd.readline()
	prev = 0
	while eachLine:
		# if excludeLine and eachLine.startswith(excludeLine):
		# continue   
		prev = zlib.crc32(eachLine, prev)
		eachLine = fd.readline()
	
	fd.close()    
	return format(prev & 0xFFFFFFFF, '08x') #returns 8 digits crc

if __name__ == "__main__":
	fileName = sys.argv[1]
	
	try:
		config = open(fileName,"rb")
		
		fileNameBak = fileName+".nodelete"
		fileLastBuild = fileName+".prev"
		try:
			configBak = open(fileNameBak,"rb")
			#print "Calculating crcs"
			
			if crc(fileName) == crc(fileNameBak):
				#print "Changing times"
				times = (os.stat(fileNameBak).st_atime, os.stat(fileNameBak).st_mtime)
				os.utime(fileName, times)
				
			else:
				#print "file was change"
				shutil.copy2(fileNameBak, fileLastBuild)
				shutil.copy2(fileName, fileNameBak)
			
		except IOError:
			#print "Creating bak file", fileNameBak
			shutil.copy2(fileName, fileNameBak)
			
	except IOError:
			print "Config file not found", fileName
			
	#print "End"
	