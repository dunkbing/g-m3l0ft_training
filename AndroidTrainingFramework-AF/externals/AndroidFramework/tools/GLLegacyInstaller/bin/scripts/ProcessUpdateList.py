'''
Created on Mar 11, 2013

@author: cosmin.nastasoiu
'''
import sys


def splitTextInWordsList( text ):
    wordsList = []
    for textLine in text: 
        textLineList = textLine.split(" ")
        textLineList = filter(bool, textLineList)
        c = 0
        for word in textLineList:
            word = word.translate(None, " ,_-")
            textLineList[c] = word
            c += 1
        wordsList.append(textLineList)
    return wordsList

def getID_API_ifThisIsAboutSDK( textLine ):
    occ = 0
    apiWordPos = -1
    c = 0
    for word in textLine:
        if word.lower() == 'sdk':
            occ += 1
        if word.lower() == 'platform':
            occ += 1
        if word.lower() == 'android':
            occ += 1
        if word.lower() == 'api':
            apiWordPos = c + 1
        c += 1
    if occ == 3 and apiWordPos != -1:
        return {'isAbout': True, 'api': int(textLine[apiWordPos]), 'id': textLine[0]}
    else:
        return {'isAbout': False, 'api': -1, 'id': '-1'}
    
def getID_API_ifThisIsAboutGoogleAPI( textLine ):
    occ = 0
    apiWordPos = -1
    c = 0
    for word in textLine:
        if word.lower() == 'google':
            occ += 1
        if word.lower() == 'apis':
            occ += 1
        if word.lower() == 'android':
            occ += 1
        if word.lower() == 'api':
            apiWordPos = c + 1
        c += 1
    if occ == 3 and apiWordPos != -1:
        return {'isAbout': True, 'api': int(textLine[apiWordPos]), 'id': textLine[0]}
    else:
        return {'isAbout': False, 'api': -1, 'id': '-1'}    
    
updateFile = open(sys.argv[1], "r")
updateText = []


for line in updateFile:
    updateText.append(line)
updateFile.close()    

wordsList = splitTextInWordsList(updateText)

largestSdkApis = [-1, -1, -1]
largestGoogleApis = [-1, -1, -1]
sdkApisID = [-1, -1, -1]
googleApisID = [-1, -1, -1]

for line in wordsList:

    sdkTest = getID_API_ifThisIsAboutSDK( line )
    gapiTest = getID_API_ifThisIsAboutGoogleAPI( line )
    if sdkTest['isAbout'] == True:  
        if sdkTest['api'] > largestSdkApis[0]:
            largestSdkApis[0] = sdkTest['api']
            sdkApisID[0]      = sdkTest['id']
        elif sdkTest['api'] > largestSdkApis[1]:
            largestSdkApis[1] = sdkTest['api']
            sdkApisID[1]      = sdkTest['id']
        elif sdkTest['api'] > largestSdkApis[2]:
            largestSdkApis[2] = sdkTest['api']
            sdkApisID[2]      = sdkTest['id']           
    
    if gapiTest['isAbout'] == True:
        if gapiTest['api'] > largestGoogleApis[0]:
            largestGoogleApis[0]    = gapiTest['api']
            googleApisID[0]         = gapiTest['id']
        elif gapiTest['api'] > largestGoogleApis[1]:
            largestGoogleApis[1]  = gapiTest['api']
            googleApisID[1]       = gapiTest['id']       
        elif gapiTest['api'] > largestGoogleApis[2]:
            largestGoogleApis[2]  = gapiTest['api']
            googleApisID[2]       = gapiTest['id']               
    


sys.stdout.write('LargestSDK1=' + sdkApisID[0] + '\n')
sys.stdout.write('LargestSDK2=' + sdkApisID[1] + '\n')
sys.stdout.write('LargestSDK3=' + sdkApisID[2] + '\n')
sys.stdout.write('LargestGAPI1=' + googleApisID[0] + '\n')
sys.stdout.write('LargestGAPI2=' + googleApisID[1] + '\n')
sys.stdout.write('LargestGAPI3=' + googleApisID[2] + '\n')

sys.exit()
