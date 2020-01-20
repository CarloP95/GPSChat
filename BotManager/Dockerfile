FROM golang as BUILDER

ARG exposedPort=9450

WORKDIR /go/src/github.com/GPSChat/BotManager

COPY . .

RUN go get -d ./ && \
    rm -rdf /go/src/github.com/docker/docker/vendor/github.com/docker/go-connections/ && \
    go get github.com/pkg/errors && \
    env GOOS=linux CGO_ENABLED=0 go build -o BotManager

FROM alpine:latest

WORKDIR app

COPY --from=BUILDER /go/src/github.com/GPSChat/BotManager/BotManager .

EXPOSE $exposedPort

CMD ["./BotManager"]


