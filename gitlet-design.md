# Gitlet Design Document
**Name**: Vikram Cherukuri
## Classes and Data Structures
###Commit - Constructor for commit object, will commit the current staging area 
*Field1 -> Master (Arraylist): Holds commits in order

*Field2 -> Head (Commit): everytime add is called on a blob, add the blob to staging area

*Field3 -> Message (String): commit message

*Field4 -> timestamp (String): records the time at which the commit was made (standardized when calling init)

*Field5 -> parent (Commit): The parent of the commit, will be null if first commit

###Blob - Constructor for the blob object, will add blobs to staging area when the add command is called
*Field1 -> Name (String): file name

*Field2 -> Contents (string): holds the contents of a file

###Repository - Call/write necessary functions for each command
*Field1 -> Staging area (Arraylist): everytime add is called on a blob, add the blob to staging area
Use ArrayList of commits to add and keep track of files that are added, files will be removed from stagingArea once they have been committed

*Field2 -> commits (Arraylist): list of all commits in order
Each commit object in the list will have 2 or more instance variables, metadata (timestamp, parent), message, possibly more
###Main - Decides which command to run based on the input
Will use cases or a series of if statements like in game.java in proj2 to execute various commands that have been defined in the repository class

*Fields -> Unsure as of now
##Algorithms

*add: adds a given blob to the staging area arraylist

*init: initializes the first commit with a set timestamp and contents

*rm: unstages the file if it is in the staging area and if it is a part of the current commit, remove it from the commit and the working directory

*log: express list of current commits in a readable format with sha1 id

*global-log: express list of all current commits in a readable format with sha1 id

*find: prints out sha ids of all commits with the given commit message 

*status: displays existing branches, staged files, removed files, files that were modified but not staged for commit, and untracked files

*checkout: follows one of the three guidelines for the checkout command

*branch: creates a new branch with the given name, and points it at the current head node

*rm-branch: deletes branch with the given input name (delete pointer not commits)

*reset: checks out all the files tracked by the given commit

*commit: stores a list of blobs as a commit object and adds the commit object into the arrayList

*merge: joins two commit branches to form a singular commit

Need to check if files change the same lines to avoid merge conflicts

*getName: returns name of a given blob

*getTimeStamp: returns timeStamp of a commit

Assign the first time as some 1970 thing (original computer time), rest will be normal real time?

## Persistence
*Every time we will add a blob, it will go to the staging area. From there, the
entire staging area will be placed into a commit object, and the staging area
arraylist will be cleared

*By creating files and adding them to the working directory, we can edit the files and save their current state so as to not lose their states at the current time including metadata such as file contents, timestamp, name, etc

*We will implement serializable to track the state of current and previous files
to ensure that there is no loss of content and that the correct files are being
added/committed.
