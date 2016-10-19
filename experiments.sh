#!/bin/bash

ulimit -t 1800

for experiment in examples/*
do
    if [[ -d $experiment ]]
    then
        echo "Doing $experiment"
        ( time ./css-analyser --mode NODOM --in-folder $experiment --dom-free-deps ) > $experiment/results.txt 2>&1
    fi
done
