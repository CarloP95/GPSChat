FROM ubuntu:19.04

WORKDIR dht

COPY ./bin ./bin
COPY ./conf ./conf

# Install dependencies for Poco library
RUN apt-get update && apt-get install -y git openssl libssl-dev build-essential && \
    #Clone the git repository for Poco library
    git clone https://github.com/pocoproject/poco.git && \
    cd poco && \
    #Configure Poco
    ./configure --omit=Data/ODBC,Data/MySQL && \
    #Compile and install Poco library on container, then clean up
    make -j 2; make install && rm -rdf * ; rm -rdf .* ; \
    #Remove something to make the container light
    cd .. && apt-get purge -y build-essential && apt-get -y autoremove

CMD ["./bin/Executable"]