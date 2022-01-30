# AutomataConformance
This repository contains the open source code for my PhD project "Quality measures in process mining \\ Tackling Scalability with concurrent and repetitive patterns". The technique involves comparing automata structures representing the event log and the process model to compute trace alignments. Additionally, to improve scalability several improvements have been impemented: 

(1) Parallelism in process models is broken down in a divide-and-conquer approach based on S-Components 
Full article can be found here:https://www.sciencedirect.com/science/article/pii/S0306437920300545;

(2) Repeated sequences in the event log are reduced via tandem repeats to further speed up computation and later extended to full alignments.
Full article can be found here:
https://arxiv.org/abs/2004.01781

The main class for computing trace alignments and fitness values for a model and a log is 
CommandLineTool.CommandLineMain.

Additionally, the project contains a new Generalization measure for a process model and an event log based on concurrent and repetitive patterns: A concurrency oracle is used in tandem with partial orders to identify concurrent patterns in the log that are tested against parallel blocks in the process model. Tandem repeats are used with various trace reduction and extensions to define repetitive patterns in the log that are tested against loops in the process model. Each pattern is assigned a partial fulfilment. The generalization is then the average of pattern fulfilments weighted by the trace counts for which the patterns have been observed. 

The main class for computing the pattern generalization for a model and a log is 
main.PatternGeneralization.

