FROM ubuntu:latest
LABEL authors="psh"

ENTRYPOINT ["top", "-b"]