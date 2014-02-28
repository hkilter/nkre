UPDATE LOG: 
- 26 Feb 2014 by Ziquan
	Add constraint, exploration strategy into output file name
- 24 Feb 2014 by Ziquan
	Add exhaustive exploration and resource constraint
- 18 Feb 2014 by Ziquan
	In random exploration, replace direct fitness value comparison by using averaging fitness value
- 1 Feb 2014 by Ziquan
	Remove &lt;refactoring&gt; tag, so that the function of refactoring will be expressed explicitly 
- 17 Apr 2013 by Ziquan
	Add .trim() to ConfigReader class
	Use EclipseMetrics plug-in to generate a metrics folder

================================================
XML CONFIG FILE
================================================

- the root element is &lt;simulation&gt;.
-- &lt;simulation&gt; contains 1 or more &lt;case&gt;
---- &lt;case&gt; contains 1 &lt;runs&gt;, 1 &lt;inf&gt;, 1 &lt;bias&gt;, 1 &lt;delta&gt;, 0 or more &lt;tau&gt; and 0 or more &lt;agent&gt;
-------- &lt;runs&gt; contains an integer, which indicates the number of runs of the case.
-------- &lt;inf&gt; contains a string, which MUST BE an ABSOLUTE path to the influence matrix txt file OR a RELATIVE path to the jar files.
-------- &lt;bias&gt; and &lt;delta&gt; contain values between 0 and 1, which indicate the amount of initial uncertainty and the among of changes after shocks respectively.
-------- &lt;tau&gt; contains an integer, which indicates the time to have a shock. Multiple shocks need to have multiple &lt;tau&gt; elements.
-------- &lt;agent&gt; has a type to be defined. E.g., &lt;agent type="foo"&gt;. &lt;agent&gt; must has a UNIQUE type within one &lt;case&gt;, however, &lt;agent&gt;s in different &lt;case&gt;s can have the same type.
-------- &lt;agent&gt; contains 1 &lt;num&gt;, 1 &lt;power&gt; and 1 &lt;plan&gt;.
---------------- &lt;num&gt; contains an integer, which indicates the total number of that type of agents with within that case
---------------- &lt;power&gt; contains an integer, which indicates the processing power of that type of agent
---------------- &lt;plan&gt; contains the agent's implementation plan, E.g., (0,1)(2,3) when N=4, (0,1,3)(2,5,4) when N=6
---------------- &lt;constraint&gt; contains values in (0, 1], which indicates the amount of constraint resource. E.g., 1 for no resource constraint and 0.5 for half resource constraint.
---------------- &lt;exhaustive&gt; contains ‘true’ or ‘false’, which indicates the agent's exploration strategy. ’true’ for exhaustive greedy exploration; ‘false’ for random exploration.
---------------- &lt;refactoring&gt; is removed (did contain 'true' or 'false', which indicates the scope of refactoring process. 'true' for refactoring all implemented elements including the elements in the current iteration; 'false' for refactoring all implemented elements excluding the elements in the current iteration.)

================================================
NK_run
================================================
NK_run takes only one argument which is the path (absolute or relative path) to an xml config file.

E.g.,
&gt;&gt; java -jar NK_run.jar ./config/conf1.xml

- The output files of NK_run are stored in the same directory as the jar file.
- The output files are txt files. One txt file is for one agent type under one case. In other words, each output file corresponds to one &lt;agent&gt; element node in the xml config file.
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
&gt;&gt; java -jar NK_landscape.jar ./config/conf1.xml 0 0

- NK_landscape prints the landscape fitness values (for locations from 000…00, 000…01 to 111…11) line by line
- NK_landscape only inspects the influence matrix (with its delta, bias and tau) in the FIRST &lt;case&gt;
 from the xml config file
- Landscape is generated from the randomly generated fitness contribution values. The seed of the random generator is taken from the &lt;runs&gt; element, so you may want to change &lt;runs&gt; to get different landscapes.

