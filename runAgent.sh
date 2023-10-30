#!/bin/sh
# $1 = ip1 $2 = ip2

java -cp Mobile.jar Mobile.Inject localhost 28180 MyAgent localhost localhost
