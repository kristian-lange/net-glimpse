# EtherVisuWeb

sudo setcap cap_net_raw,cap_net_admin=eip /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

sbt -DNIF=wlp2s0 -Dhttp.address=172.23.1.81 -Dhttp.port=9000 run
