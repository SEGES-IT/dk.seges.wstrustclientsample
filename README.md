# dk.seges.wstrustclientsample
Run with Ubuntu or WSL:

```
sudo apt-get install maven git openjdk-8-jdk
git clone https://github.com/SEGES-IT/dk.seges.wstrustclientsample.git
cd dk.seges.wstrustclientsample/
mvn compile
mvn exec:java -Dexec.mainClass=dk.seges.App -Dexec.args="myusername mypassword"
```
Output is the raw token xml, and the DeflatedSaml encoded token xml
