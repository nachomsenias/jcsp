#!/bin/bash

dir=./instances

sequences=(classical 200 90)
num_sequences=3

log=/results
#config=./config/GRASPswapInsertRandom-first.csp
config=./config/$1
dateMark="$(date +%s)"
alpha=0.15


for j in $(seq 1 $num_sequences)
do
	customDir="${dir}/${sequences[$j-1]}"

	customLog="${customDir}${log}"
	
	echo ${customDir}
	
	echo ${customLog}

	/usr/java/jdk1.8.0_45/bin/java -jar javaCSP.jar ${customDir} ${customLog} ${config} ${alpha} >> "${customLog}/${dateMark}.log"

done;

echo "############################################"
echo DONE
echo "############################################"
