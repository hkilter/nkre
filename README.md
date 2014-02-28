UPDATE LOG: 
- 26 Feb 2014 by Ziquan
	Add constraint, exploration strategy into output file name
- 24 Feb 2014 by Ziquan
	Add exhaustive exploration and resource constraint
- 18 Feb 2014 by Ziquan
	In random exploration, replace direct fitness value comparison by using averaging fitness value
- 1 Feb 2014 by Ziquan
	Remove <refactoring> tag, so that the function of refactoring will be expressed explicitly 
- 17 Apr 2013 by Ziquan
	Add .trim() to ConfigReader class
	Use EclipseMetrics plug-in to generate a metrics folder

================================================
XML CONFIG FILE
================================================

- the root element is <simulation>.
-- <simulation> contains 1 or more <case>
---- <case> contains 1 <runs>, 1 <inf>, 1 <bias>, 1 <delta>, 0 or more <tau> and 0 or more <agent>
-------- <runs> contains an integer, which indicates the number of runs of the case.
-------- <inf> contains a string, which MUST BE an ABSOLUTE path to the influence matrix txt file OR a RELATIVE path to the jar files.
-------- <bias> and <delta> contain values between 0 and 1, which indicate the amount of initial uncertainty and the among of changes after shocks respectively.
-------- <tau> contains an integer, which indicates the time to have a shock. Multiple shocks need to have multiple <tau> elements.
-------- <agent> has a type to be defined. E.g., <agent type="foo">. <agent> must has a UNIQUE type within one <case>, however, <agent>s in different <case>s can have the same type.
-------- <agent> contains 1 <num>, 1 <power> and 1 <plan>.
---------------- <num> contains an integer, which indicates the total number of that type of agents with within that case
---------------- <power> contains an integer, which indicates the processing power of that type of agent
---------------- <plan> contains the agent's implementation plan, E.g., (0,1)(2,3) when N=4, (0,1,3)(2,5,4) when N=6
---------------- <constraint> contains values in (0, 1], which indicates the amount of constraint resource. E.g., 1 for no resource constraint and 0.5 for half resource constraint.
---------------- <exhaustive> contains ‘true’ or ‘false’, which indicates the agent's exploration strategy. ’true’ for exhaustive greedy exploration; ‘false’ for random exploration.
---------------- <refactoring> is removed (did contain 'true' or 'false', which indicates the scope of refactoring process. 'true' for refactoring all implemented elements including the elements in the current iteration; 'false' for refactoring all implemented elements excluding the elements in the current iteration.)

================================================
NK_run
================================================
NK_run takes only one argument which is the path (absolute or relative path) to an xml config file.

E.g.,
>> java -jar NK_run.jar ./config/conf1.xml

- The output files of NK_run are stored in the same directory as the jar file.
- The output files are txt files. One txt file is for one agent type under one case. In other words, each output file corresponds to one <agent> element node in the xml config file.
- The output txt file name is formatted as:
"o_n" + the number N + "k" + the number K + "_b" + bias + "d" + delta + “c” + constraint + ”_” + exploration strategy + “_”+ the agent type + ".txt"
- The format in the output txt file is as follows:
SEED, AGENT, TIMESTAMP, SHOCK, ITERATION, PERFORMANCE, MAX, MIN	

- IMPORTANT NOTE 1
There are more than one influence matrices with the same N and K. Maybe you will differentiate them with different file names, e.g., "n4k2_1.txt", "n4k2_2.txt". However, the output file name could be the same, because of the format above. Therefore, please differentiate them using different agent types.

- IMPORTANT NOTE 2
New generated output file will not rewrite the original file (if existed) with the same name. Instead, the contents will be appended to the original file (if existed).


================================================
NK_landscape
================================================
NK_landscape takes 3 arguments, xml config file, shockNum and stepNum.

E.g.,
>> java -jar NK_landscape.jar ./config/conf1.xml 0 0

- NK_landscape prints the landscape fitness values (for locations from 000…00, 000…01 to 111…11) line by line
- NK_landscape only inspects the influence matrix (with its delta, bias and tau) in the FIRST <case>
 from the xml config file
- Landscape is generated from the randomly generated fitness contribution values. The seed of the random generator is taken from the <runs> element, so you may want to change <runs> to get different landscapes.

