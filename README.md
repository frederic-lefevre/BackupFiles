# BackupFiles

Java application with swing GUI.

It allows to define directories backup configurations and to do these backups.

It allows a 2 phases backup : 
- first phase in a "buffer backup", typically from source directories to directories 
on the same fast accessible storage (ideally a SSD).
- second phase on the real target directories (maybe located on a mounted device such as a USB drive with a slower access)

The aim of the 2 phases is to be able to do the first backup during a "stable state" (no running applications that would possibly
modify the files during backup). This is especially true if portable applications are backed up. So this first phase must be as fast
as possible. After this first phase, you may open whatever application in parallel with the second backup phase.

This has also 2 other advantages : the possibility to have a local fast access backup and to have several target backup done from
a single buffer backup (2 backup on 2 USB keys for instance).

It has acceptable performances (something like 400K files scanning in 20 seconds in the buffer phase). The scanning is multi-threaded.

It has a content comparison option (which slows down the process naturally).

The property file can be passed in argument : 
> java -jar backupFiles.jar -props=myProperties.properties