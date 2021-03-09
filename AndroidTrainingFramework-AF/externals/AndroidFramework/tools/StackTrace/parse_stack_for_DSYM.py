import sys
import re
import os

sohead = re.compile('(.+\.so):')
funchead = re.compile('([0-9a-f]{8}) <(.+)>:')
funcline = re.compile('^[ ]+([0-9a-f]+):.+')

LogfilePath = os.getenv("LOG_TXT")
DSYMFile = os.getenv("SO_LIBRARY")

if(len(sys.argv) == 2):
    DSYMFile = sys.argv[1]

if(len(sys.argv) == 3):
    DSYMFile = sys.argv[1]
    LogfilePath = sys.argv[2]

print "LogfilePath=", LogfilePath;
print "DSYMFile=", DSYMFile;

So_LibraryFile = os.path.basename(os.path.splitext(DSYMFile)[0])
ArmAebi = os.getenv("ARMABIADRR")
ArmCppFilt = os.getenv("ARMABICPPFILT")

def parsestack( lines ):
    crashline = re.compile('.+pc.([0-9a-f]{8}).+gameloft')
    ret = []
    for l in lines:
	#print "line=", l;
	startFname = l.find('(')+1;
	endFname   = l.find(')');
	fname = ""
	if(startFname != -1 and endFname != -1 and startFname < endFname):
	    fname = l[startFname:endFname];
        m = crashline.match(l)
        if m:
	    addr =  m.groups()[0]
	    #arm-eabi-addr2line.exe -f -C -e -s -i libnova.so 0x002a5e5e
	    #ret.append(int(addr,16))
	    #os.system("%s -f -e %s %s | %s -p -i" % (ArmAebi , DSYMFile , addr, ArmCppFilt) )
	    command = "%s -e %s %s " % (ArmAebi , DSYMFile , addr)
	    #print "command=", command;
	    os.system(command )
	    if(fname != ""):
		command = "%s %s" % (ArmCppFilt, fname)
		#print "command=", command;
		os.system(command )	    
	    #os.system("%s -e %s %s | %s -p -i" % (ArmAebi , DSYMFile , addr, ArmCppFilt) )
	    #print "ADREESS --> "
	    #print addr
	    #print So_LibraryFile
    return ret

def parseasm( lines ):
    ret = []
    current = None
    for l in lines:
        m = funchead.match(l)
        if m:
            if current:
                ret.append(current)
            startaddr, funcname =  m.groups()
            current = [ funcname, int(startaddr,16), int(startaddr,16) ]
        m = funcline.match(l)
        if m:
            addr =  m.groups()[0]
            if current != None:
                current[2] = int(addr,16)
        m = sohead.match(l)
        if m:
            so =  m.groups()[0]
            so = os.path.split(so)[1]
    return so, ret

if __name__=="__main__":
    #asm, stack = sys.argv[1],sys.argv[2]

    #libname, asm = parseasm( file(asm).read().split('\n') )
	
    stack = parsestack( file(LogfilePath).read().split('\n'))

    #for addr in stack:
    #    for func, a1, a2 in asm:
    #        if addr >= a1 and addr <= a2:
    #            print "0x%08x:%32s + 0x%04x" % ( addr, func, addr-a1 )
