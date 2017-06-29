Kompilieren:
cd src
javac -cp .:../lib/\* apgas/*.java -d ../bin
javac -cp .:../lib/\* apgas/impl/*.java -d ../bin
javac -cp .:../lib/\* Start.java -d ../bin

Ausfuehren:
cd bin
java -cp .:../lib/* -Dapgas.places=$places -Dapgas.hostfile=/path/to/hostfile Start
java -cp .:../lib/* -Dapgas.verbose.launcher=true -Dapgas.places=6 Start 80 123

Wenn -Dapgas.hostfile wegelassen wird, werden alle places lokal gestartet.
Das hostfile entweder in bin anlegen oder konkreten Pfad angeben. 
Es enthält alle Hosts, einen pro Zeile, in der ersten Zeile steht der lokale Rechner fuer place 0. 
Wenn places>hosts, dann wird der letzte Host wiederholt.

Option um die Startroutine auszugeben:
-Dapgas.verbose.launcher=true
(ist nützlich um zu sehen ob die Places auf dem gewuenschten Knoten gestartet werden)



Shell auf bash umstellen:
Auf https://www.uni-kassel.de/go/userapp einloggen und im Profil die LoginShell auf /bin/bash umstellen und speichern. Es kann bis 15 Minuten dauern bis der Cluster es übernommen hat. Dies kann auf dem Cluster mit "echo $0" überprüft werden. Es muss "bash" zurückgeliefert werden.

Damit die ssh Verbindungen automatisch ohne Passworteingabe erfolgen können, bitte folgendes ausführen:
ssh-keygen -t rsa (alles einfach mit enter bestätigen)
ssh-copy-id -i ~/.ssh/id_rsa.pub uk000000@its-cs207.its.uni-kassel.de (richtige uk-nummer einfügen)

Zusätzlich kann die Überprüfung der Keys einfach deaktiviert werden, dafür folgendes in der Datei ~/.ssh/config ergänzen (falls nicht vorhanden, anlegen):

Host *
    StrictHostKeyChecking no
