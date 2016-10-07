#!/usr/bin/env bash

#This is the folder where the component looks for config files
CONFDIR="/opt/${artifactId}/conf"
#Delete it, so we can overrule it
rm -rf $CONFDIR

#The /conf folder is a shorthand so people can easily mount another conf folder
# If we have something in the /conf folder, use that
if [ -f /conf/config.properties ]; then
    ln -s /conf $CONFDIR
else # Else, parse the conf.orig folder with the environment
    if [ ! -f /conf/config.properties ]; then
        mkdir -p $CONFDIR
        for file in /opt/${artifactId}/conf.orig/*; do
            if [[ "$file" == *.properties ]]; then #Only replace in .properties files
                outputFile="$CONFDIR/$(basename $file)"
                cat $file | while read line; do
                    #This oneliner converts lines like
                    #    autonomous.lockserver.url={zookeeper.host}:{zookeper.port}
                    #To
                    #    autonomous.lockserver.url=${zookeeper_host}:${zookeper_port}
                    parsedLine=$(echo $line | sed 's/\(=[^{]*\){/\1${/g' | sed 's/\(}[^{]*\){/\1${/g' | sed 's/\({[^.}]*\)\./\1_/g' | sed 's/\({[^.}]*\)\./\1_/g')
                    # Use eval to substitute the env variables into the config line
                    eval "echo \"$parsedLine\" >> ${outputFile}"
                done
            else
                cp $file $CONFDIR
            fi
        done
    fi
fi

#When the config is done, just start whatever command is given
exec bash -c "$@"

#The default command from the dockerfile is
#while :; do sleep ${interval}; /opt/${artifactId}/bin/pollAndWork.sh; done