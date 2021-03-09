export TMPDIR=$GLLEGACY_DISTCC_SERVER_TEMP

if [ $GLLEGACY_DISTCC_SERVER_JOBS -eq 0 ]
then
  distccd --daemon --allow=$GLLEGACY_DISTCC_SERVER_NETADDR --log-stderr --verbose
else
  distccd --daemon --jobs=$GLLEGACY_DISTCC_SERVER_JOBS --allow=$GLLEGACY_DISTCC_SERVER_NETADDR --log-stderr --verbose
fi
