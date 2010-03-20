#!/bin/bash

# gather all command line arguments for passing on
for arg in "$@" ; do
	bob_args="$bob_args \"$arg\""
done

if [ "`uname`" = "Darwin" ] && [ -z "$JAVA_HOME" ]; then
	JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
fi

if [ -z "$JAVACMD" ] ; then
	if [ -n "$JAVA_HOME" ] ; then
		# IBM's JDK on AIX uses strange locations for the executables
		if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
			JAVACMD="$JAVA_HOME/jre/sh/java"
		elif [ -x "$JAVA_HOME/jre/bin/java" ] ; then
			JAVACMD="$JAVA_HOME/jre/bin/java"
		else
			JAVACMD="$JAVA_HOME/bin/java"
		fi
	else
		JAVACMD=`which java 2> /dev/null `
		if [ -z "$JAVACMD" ] ; then
			JAVACMD=java
		fi
	fi
fi

if [ ! -x "$JAVACMD" ] ; then
	echo "Error: JAVA_HOME is not defined correctly."
	echo "  We cannot execute $JAVACMD"
	exit 1
fi

bob_command="exec \"$JAVACMD\""

eval $bob_command -jar bob.jar "$bob_args"