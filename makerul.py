import sys
import glob
from mako.template import Template

inputDir   = sys.argv[1]
outputFile = sys.argv[2]

rqFileNames = glob.glob(inputDir+"/*.rq")
rqFileContents = []
for rqFileName in rqFileNames:
    f = open( rqFileName, "r")
    rqFileContents.append( f.read() )

mytemplate = Template(filename='rultemplate.txt')
output = open( outputFile, "w" )
output.write( mytemplate.render(values=rqFileContents) )
