#!/bin/bash

dir=./instances

sequences=(60 65 70 75 80 85 90 classical)
num_sequences=8

log=/results
config=./config/GRASPswap-first.csp
dateMark="$(date +%s)"


for j in $(seq 1 $num_sequences)
do
	customDir="${dir}/${sequences[$j-1]}"

	customLog="${customDir}${log}"

	java -jar javaCSP.jar ${customDir} ${customLog} ${config} >> "${customLog}/${dateMark}.log"
done;

echo "############################################"
echo DONE
echo "############################################"
