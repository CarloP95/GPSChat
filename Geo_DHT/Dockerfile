FROM ubuntu:19.04

WORKDIR dht

COPY ./bin ./bin
COPY ./conf ./conf

RUN apt-get update && apt-get install -y git openssl libssl-dev build-essential && \
    git clone https://github.com/pocoproject/poco.git && \
    cd poco && \
    ./configure --omit=Data/ODBC,Data/MySQL && \
    make -j 2 && make install && rm -rdf * && rm -rdf .* && \
    cd .. && apt-get purge -y build-essential && apt-get -y autoremove

CMD ["./bin/Executable"]