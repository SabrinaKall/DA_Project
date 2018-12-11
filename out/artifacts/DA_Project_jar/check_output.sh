#!/bin/bash
#
# Checks the output of processes.
#

echo -e "\nTESTING PROCESS OUTPUT.\n"

filepart1="da_proc_"
filepart2=".out"
memberfile=membership_causal

echo "Checking files."



./check_causal.py $memberfile $filepart1 $filepart2
if [ $? -ne 0 ]; then
    echo "Error in file ${filename}."
    echo -e "\nTEST FAILED.\n"
    exit 1
fi

echo -e "\nTEST SUCCEEDED.\n"
