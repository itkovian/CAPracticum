#!/bin/bash

aantal_iteraties[0]=10000000
aantal_iteraties[1]=10000000
aantal_iteraties[2]=10000000
aantal_iteraties[3]=10000000
aantal_iteraties[4]=1000000
aantal_iteraties[5]=1000000
aantal_iteraties[6]=1000000
aantal_iteraties[7]=1000000
aantal_iteraties[8]=1000000
aantal_iteraties[9]=100000
aantal_iteraties[10]=100000
aantal_iteraties[11]=100000
aantal_iteraties[12]=100000
aantal_iteraties[13]=100000
aantal_iteraties[14]=100000
aantal_iteraties[15]=10000
aantal_iteraties[16]=10000
aantal_iteraties[17]=10000
aantal_iteraties[18]=10000
aantal_iteraties[19]=10000
aantal_iteraties[20]=1000
aantal_iteraties[21]=1000
aantal_iteraties[22]=1000

for i in `seq 1 1 22`; do 
	echo "${aantal_iteraties[ $i ]} `./pell $i ${aantal_iteraties[ $i ]} | grep Pell | awk '{print $5}'`"
done

