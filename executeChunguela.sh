#!/bin/bash

dir=./instances

sequences=(90 classical 200)
num_sequences=3

log=/results
config=./config/GRASPswapInsertRandom-first.csp
dateMark="$(date +%s)"


for j in $(seq 1 $num_sequences)
do
	customDir="${dir}/${sequences[$j-1]}"

	customLog="${customDir}${log}"
	
	echo ${customDir}
	
	echo ${customLog}

	/usr/java/jdk1.8.0_45/bin/java -jar javaCSP.jar ${customDir} ${customLog} ${config} >> "${customLog}/${dateMark}.log"

done;

echo "############################################"
echo DONE
echo "############################################"
