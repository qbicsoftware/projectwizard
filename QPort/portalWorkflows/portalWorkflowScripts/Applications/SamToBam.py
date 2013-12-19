import sys,shlex,subprocess, os
from Utils.workflow_config import workflow_config
from Wrapper.Bwa  import Bwa
from Factory.IFactory import IFactory
class SamToBam(Bwa):
	def __init__(self,program,tool):
		Bwa.__init__(self,program,tool)
		self.input = ''	
		self.additionalParameters = ''
		self.outputFileName = ''	
	def createOutputFileName(self):
		print "inside createOutputFileName"
		print self.input
		if(len(self.input) != 0):
			path,file_name = os.path.split(self.input)
			output_file_name = file_name.rstrip() + "_sorted.bam"
			self.createOutputFilePath(path,output_file_name)
	def setInput(self):
		try:
			print 'Reading input files'
			with open(workflow_config.input_file, 'r') as f:
 			 	self.input = f.readline()
		except IOError as e:
			sys.stderr.write('No input file found\n')
		print self.input
		return self.input

	def buildCommand(self):
		print self.outputFileName 
		tmp1 = "samtools view %s %s %s" % ( self.parameters, self.additionalParameters,self.input.rstrip())			
		tmp2 = "samtools sort - %s" % (os.path.splitext(self.outputFileName)[0])
		return [shlex.split(tmp1),shlex.split(tmp2)]
	def execute(self):
		cmd_line = self.buildCommand()
		print cmd_line
		p1 = subprocess.Popen(cmd_line[0], stdout=subprocess.PIPE)
		p2 = subprocess.Popen(cmd_line[1], stdin=p1.stdout)#,stdout=subprocess.PIPE)
		p1.wait()
		p2.wait()
		if(p1.returncode != 0):
			print "sammtools view exited with with error status: %d " % p1.returncode
		#	sys.exit(p1.returncode)
		if(p2.returncode != 0):
			print "sammtools sort exited with with error status: %d " % p2.returncode
		#	sys.exit(p2.returncode)
		#check if file exists
		if(os.path.exists(self.outputFileName)):
			print("output file written")
		else:
			print("ERROR: output file NOT written")
		#p1.stdout.close()  # Allow p1 to receive a SIGPIPE if p2 exits.
		#print p2.communicate() 
				
	
	class Factory(IFactory):
		def create(self): return SamToBam(self.program,self.tool)
