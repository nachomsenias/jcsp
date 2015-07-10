#!/bin/bash

set INSTANCES=instances

set SEQUENCES=(60 65 70 75 80 85 90 classical)

set LOG=/results
set CONFIG=config/GRASPinvertion-best.csp
set DATE_MARK=%DATE:/=-%_%TIME::=-%


for %%j in %SEQUENCES% do java -jar javaCSP.jar "%INSTANCES%/%%j" "%INSTANCES%/%%j%LOG%" "%CONFIG%" >> "%INSTANCES%/%%j%LOG%/%DATE_MARK%.log"

echo "############################################"
echo DONE
echo "############################################"
pause
