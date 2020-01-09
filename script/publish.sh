#!/bin/bash

function showHelp() {
    echo "publishToMavenLocal: ./publish.sh l"
    echo "publish: ./publish.sh m"
    # publish to JCenter
    echo "publish: ./publish.sh j"
}

if [ -z $1 ];then
    showHelp
    exit -1
fi

function publishMaven(){
    ./gradlew clean :core:$1 :plugin:$1 --no-daemon --stacktrace
}

if [[ $1 == 'l' ]];then
    publishMaven publishToMavenLocal
elif [[ $1 == 'm' ]];then
    publishMaven publish
elif [[ $1 == 'j' ]];then
    publishMaven bintrayUpload
else
    showHelp
    exit -1
fi
