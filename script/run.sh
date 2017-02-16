#!/bin/sh
#/*--------------------------------------------------------------------------
# *  Copyright 2012 Taro L. Saito
# *
# *  Licensed under the Apache License, Version 2.0 (the "License");
# *  you may not use this file except in compliance with the License.
# *  You may obtain a copy of the License at
# *
# *     http://www.apache.org/licenses/LICENSE-2.0
# *
# *  Unless required by applicable law or agreed to in writing, software
# *  distributed under the License is distributed on an "AS IS" BASIS,
# *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# *  See the License for the specific language governing permissions and
# *  limitations under the License.
# *--------------------------------------------------------------------------*/

action=$1

if [[ -z $action ]]; then
  echo "Usage: `basename $0` (start | stop | status)"
  exit 1
fi

if [[ $action != start && $action != stop && $action != status ]]; then
  echo "Unknown action: $action"
  exit 1
fi

JAVA_HOME=/apps/java/jre
PROG_HOME=/apps/file-size-reporter-0.0.1-SNAPSHOT
logfilepath=/apps/logs/
logfilename=file-size-reporter.log
pidfile=/apps/logs/file-size-reporter.pid
CONFIG_FILE_PATH=/apps/config
YOUTUBE_KEY_PATH=/apps/config/pkcs8_key
PAGE_LOAD_WORKER_COUNT=2
PAGE_PROCESS_WORKER_COUNT=10

if [ -z "$PROG_HOME" ] ; then
  ## resolve links - $0 may be a link to PROG_HOME
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done

  saveddir=`pwd`

  PROG_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  PROG_HOME=`cd "$PROG_HOME" && pwd`

  cd "$saveddir"
fi

cygwin=false
mingw=false
darwin=false
case "`uname`" in
  CYGWIN*) cygwin=true
          ;;
  MINGW*) mingw=true
          ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
            echo "Using Java version: $JAVA_VERSION" 1>&2
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           JAVA_OPTS="$JAVA_OPTS -Xdock:name=\"${PROG_NAME}\" -Xdock:icon=\"$PROG_HOME/icon-mac.png\" -Dapple.laf.useScreenMenuBar=true"
           JAVACMD="`which java`"
           ;;
esac

# Resolve JAVA_HOME from javac command path
if [ -z "$JAVA_HOME" ]; then
  javaExecutable="`which javac`"
  if [ -n "$javaExecutable" -a -f "$javaExecutable" -a ! "`expr \"$javaExecutable\" : '\([^ ]*\)'`" = "no" ]; then
    # readlink(1) is not available as standard on Solaris 10.
    readLink=`which readlink`
    if [ ! `expr "$readLink" : '\([^ ]*\)'` = "no" ]; then
      javaExecutable="`readlink -f \"$javaExecutable\"`"
      javaHome="`dirname \"$javaExecutable\"`"
      javaHome=`expr "$javaHome" : '\(.*\)/bin'`
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi


if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="`which java`"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

CLASSPATH_SUFFIX=""
# Path separator used in EXTRA_CLASSPATH
PSEP=":"

# For Cygwin, switch paths to Windows-mixed format before running java
if $cygwin; then
  [ -n "$PROG_HOME" ] &&
    PROG_HOME=`cygpath -am "$PROG_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath -am "$JAVA_HOME"`
  CLASSPATH_SUFFIX=";"
  PSEP=";"
fi

# For Migwn, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$PROG_HOME" ] &&
    PROG_HOME="`(cd "$PROG_HOME"; pwd)`"
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
  CLASSPATH_SUFFIX=";"
  PSEP=";"
fi

PROG_NAME=file-size-reporter
PROG_VERSION=1.0

cd $PROG_HOME

if [[ $action == start ]]; then
  if [[ -e $pidfile ]]; then
    echo "sftpdelivery already running ($pidfile exists)"
    exit 1
  fi

  JAVA_CMD=""
  JAVA_CMD="$JAVA_CMD nohup $JAVACMD"
  JAVA_CMD="$JAVA_CMD -Dlogfilepath=${logfilepath}"
  JAVA_CMD="$JAVA_CMD -Dlogback.configurationFile=${PROG_HOME}/config/logback.xml"
  JAVA_CMD="$JAVA_CMD -Dyoutube.key.path=${YOUTUBE_KEY_PATH}"
  JAVA_CMD="$JAVA_CMD -Dpage.load.worker.count=${PAGE_LOAD_WORKER_COUNT}"
  JAVA_CMD="$JAVA_CMD -Dpage.process.worker.count=${PAGE_PROCESS_WORKER_COUNT}"
  JAVA_CMD="$JAVA_CMD -Dpage.process.poll.interval=5000"
  JAVA_CMD="$JAVA_CMD ${JAVA_OPTS}"
  JAVA_CMD="$JAVA_CMD -cp '${CONFIG_FILE_PATH}/*${PSEP}${PROG_HOME}/config/*${PSEP}${PROG_HOME}/lib/*${CLASSPATH_SUFFIX}'"
  JAVA_CMD="$JAVA_CMD -Dprog.home='${PROG_HOME}'"
  JAVA_CMD="$JAVA_CMD -Dprog.version=${PROG_VERSION}"
  JAVA_CMD="$JAVA_CMD -Dcom.sun.management.jmxremote.port=45001"
  JAVA_CMD="$JAVA_CMD -Dcom.sun.management.jmxremote.ssl=false"
  JAVA_CMD="$JAVA_CMD -Dcom.sun.management.jmxremote.authenticate=false"
  JAVA_CMD="$JAVA_CMD -Dspring.config.location=file:${CONFIG_FILE_PATH}/application.properties"
  JAVA_CMD="$JAVA_CMD com.sonymusic.ycm.main.Application"

  sh -c 'echo $$ > '"$pidfile; exec $JAVA_CMD 2>&1" >> $logfilepath$logfilename &

  if [[ $? == 0 ]]; then
    echo "file-size-reporter started"
  fi
elif [[ $action == status ]]; then

  if [[ ! -e $pidfile ]]; then
    echo "file-size-reporter not running ($pidfile doesn't exist)"
    exit 1
  fi

  pid=`cat $pidfile`

  if [[ ! -n $pid ]]; then
    echo "file-size-reporter not running ($pidfile is empty)"
    exit 1
  fi

  echo "file-size-reporter running (pid=$pid)"

elif [[ $action == stop ]]; then

  if [[ ! -e $pidfile ]]; then
    echo "file-size-reporter not running ($pidfile doesn't exist)"
    exit 1
  fi

  pid=`cat $pidfile`

  if [[ ! -n $pid ]]; then
    echo "file-size-reporter not running ($pidfile is empty)"
    exit 1
  fi

  for signal in "term" "int" "hup" "kill"; do
    kill -$signal $pid
    if [[ $? == 0 ]]; then
      sleep 2
      ps -ef | grep java | grep $pid 2>&1 > /dev/null
      if [[ $? == 0 ]]; then sleep 1; else break; fi
    fi
  done

  if [[ $? == 0 ]]; then
    echo "file-size-reporter stopped"
  fi

  rm -f $pidfile

fi
