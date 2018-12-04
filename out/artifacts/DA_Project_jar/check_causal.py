#!/usr/bin/python3 -u

import sys
from collections import defaultdict

print("Starting analysis")

with open(sys.argv[1]) as mf:
    line = mf.readline()
    tokens = line.split()
    numFiles = int(tokens[0])

    for i in range(numFiles):
        mf.readline()
    
    dependencies = {}

    for i in range(numFiles):
        line = mf.readline()
        tokens = line.split()
        orig = int(tokens[0])
        dependencies[orig] = tokens[1:]

    filepart1 = sys.argv[2]
    filepart2 = sys.argv[3]

    multipleFilesInfo = []
    for i in range(numFiles):
        filenum = i+1
        filename = filepart1 + str(filenum) + filepart2
        

        with open(filename) as f:
            msgsCausalPast = {}
            causalPast = set()
            deliveredBefore = {}
            delivered = set()

            for line in f:
                tokens = line.split()

                # Check broadcast
                if tokens[0] == 'b':
                    msg = int(tokens[1]) #msgNum
                    msgsCausalPast[msg] = set(causalPast)
                    causalPast.add((filenum, msg))

                # Check delivery
                if tokens[0] == 'd':
                    msg = (int(tokens[1]),int(tokens[2])) #[processNum, msgNum]
                    deliveredBefore[msg] = set(delivered)
                    delivered.add(msg)
                    if tokens[1] in dependencies[filenum]:
                        causalPast.add(msg)
        
            multipleFilesInfo += [[msgsCausalPast, deliveredBefore]]

    i = 1
    for fileInfo in multipleFilesInfo:
        deliveredBefore = fileInfo[1]
        for delMsg in deliveredBefore:
            processIndex = delMsg[0]-1
            msgNum = delMsg[1]
            causalPast = multipleFilesInfo[processIndex][0][msgNum]
            if not causalPast.issubset(deliveredBefore[delMsg]):
                print("Checking file: " + str(i))
                print(delMsg)
                print(causalPast)
                print(deliveredBefore[delMsg])
                print("Output INCORRECT")
                exit(1)
                
        i += 1

    print("Output CORRECT")
    exit(0)
