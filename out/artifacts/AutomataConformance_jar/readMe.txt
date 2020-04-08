------------------------
AutomataConformance v1.2
------------------------

REQUIREMENTS: 
Java 8 or above

WHAT IT DOES:
	AutomataConformance v1.2 is a business process conformance checking tool for identifying differences between a process model and an event log in a scalable way.
	The differences are captured as alignments when synchronously traversing automata stuctures representing the process model and event log. As a result, the tool produces various alignment 		statistics such as fitness and raw fitness costs. Optionally, the tool can output the alignments found. 

HOW:
	AutomataConformance v1.0 implements the conformance checking technique described in the paper 
      	"Scalable Conformance Checking of Business Processes" by D. Reißner, R. Conforti, M. Dumas, M. La Rosa 
	and A. Armas-Cervantes.

	AutomataConformance v1.1 implements a divide-and-conquer extension based on S-Component decomposition described in the paper
	"Scalable Alignment of Process Models and Event Logs: An Approach Based on Automata and S-Components" by D. Reißner, A. Armas-Cervantes, 
	R. Conforti, M. Dumas, D. Fahland and M. La Rosa

	AutomataConformance v1.2 implements an additional extension based on tandem repeat reductions described in the paper
	"Efficient Conformance Checking using Alignment Computation with Tandem Repeats" by D. Reißner, A. Armas-Cervantes and M. La Rosa 

USAGE:
	java -jar AutomataConformance.jar [folder] [modelfile] [logfile] [extension] [numberThreads]

	Input : A process model in bpmn or pnml format and an event log in either xes or xes.gz format.
	Output: various alignment statistics such as fitness and raw fitness costs. Optionally, the tool can output the alignments found. 

PARAMETERS:

[folder]      		: Folder where all input files are located and where the output file will be created. Please note, that the last delimiter is expected after the folder name, i.e. "/path/dataset/".
[modelFile]    		: Process model including extension (.pnml or bpmn).
[logFile]		: Event log including extension (either .xes or .xes.gz).
[extension]		: (optional) Specify which of the following extensions should be applied to calculate conformance:
				-> "Automata-Conformance" is the standard technique from v1.0
				-> "S-Components" applies the first extension from v1.1
				-> "TR-SComp" applies the second extension from v1.2
				-> "Hybrid approach" determines which extension should be applied based on characteristics of the input event log and process model. 
				   This is the default parameter, if the extension is not specified.
[numberThreads]		: (optional) The approach can be run multi-threaded by specifying the number of threads. Please not that this parameter can only use the maximal number of threads of your computer. 			  If this parameter is not specified, the application will run single-threaded.

Parameters [folder], [logfile] and [modelfile] and [extension] are compulsory.
Parameters [extension] and [numberThreads] are optional, but expected in this order.
The application will ask you, if you want to output alignments after the computation is finished. Press key y to output alignments into an excel sheet.

Using the tool without any parameters will provide release information of the tool.

OUTPUT:
	The tool will output the specified extension, the model name, the time used to calculate alignments, the average raw fitness cost per case and the trace fitness.
	If the user pressed y, the tool will output an excel file with name "alignments.xlsx" in the specified [folder] with the following characteristics:
		-> Sheet "Alignment results per Case type" contains the alignments and statistics for each case type (a.k.a. unique trace) and the number of represented cases.
		-> Sheet "Alignment results per Case" contains the alignments of each case.
		
EXAMPLE:
	java -jar AutomataConformance.jar ./DemonstrationExample/ BPIC12IM.pnml BPIC12.xes.gz

	The dataset used for the evaluation of the papers can be found here:
	https://melbourne.figshare.com/articles/Public_benchmark_data-set_for_Conformance_Checking_in_Process_Mining/8081426

ASSUMPTIONS:
	The input model should be formatted according to the pnml (http://www.pnml.org/) format.
      	To check that the input model is correctly formatted, you can open the model using WoPeD.

	The tool accepts models formatted according to the bpmn (http://http://www.bpmn.org) format.
		The tool will internally convert the bpmn diagram to its equivalent Petri net representation.
	
	The input log should be formatted according to the mxml or xes (OpenXES, http://www.xes-standard.org/) format.
      	To check that the input log is correctly formatted, you can open the log using the ProM toolkit.

RELEASE NOTES:

	1.0: initial version
	1.1: Added the implementation of the S-Components extension.
	1.2. Added the implementation of the Tandem repeats extenstion.