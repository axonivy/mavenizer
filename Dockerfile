FROM maven:3.6.3-jdk-11

RUN addgroup --gid 1000 build && adduser --uid 1000 --gid 1000 --disabled-password --gecos "" build && \
    apt-get update && \
    apt-get install rsync -y && \
    echo "Host *\n   StrictHostKeyChecking no" > /etc/ssh/ssh_config
