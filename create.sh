#!/bin/bash -e

function rep(){
    sed -i "s/$1/$2/g" `grep $1 -rl ./$3`
}

function createProject(){
package=$2
packPath=${package//./\/}
echo begin create project $1 ...
mkdir -p $1 $1/$1-api $1/$1-provider
cp ./base-project/pom.xml $1/pom.xml
cp ./base-project/base-api/pom.xml $1/$1-api/pom.xml
cp ./base-project/base-provider/pom.xml $1/$1-provider/pom.xml
mkdir -p $1/$1-api/src/main/java/${packPath}
mkdir -p $1/$1-provider/src/main/java/${packPath}
mkdir -p $1/$1-provider/src/main/resources
mkdir -p $1/$1-provider/src/test/java/${packPath}
cp -r ./base-project/base-provider/src/main/java/com/you/boot/cloud/* $1/$1-provider/src/main/java/${packPath}
cp -r ./base-project/base-provider/src/main/resources/* $1/$1-provider/src/main/resources
cp -r ./base-project/base-provider/src/test/java/com/you/boot/cloud/* $1/$1-provider/src/test/java/${packPath}
rep com.you.boot.cloud $2 $1
rep base-project $1 $1
rep base-api $1-api $1
rep base-provider $1-provider $1


echo create project $1 success!
}
cd `dirname "$0"`
cd ../
read -p "Please input your project name :
> " newPrjName

read -p "Please input your package name :
> " newPacName

createProject $newPrjName $newPacName

