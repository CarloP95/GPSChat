FROM golang:latest as BUILDER

WORKDIR /go/src/github.com/GPSChat/Bot

COPY . .

RUN go get -d ./ && \
	env GOOS=linux CGO_ENABLED=0 go build -o Bot

FROM alpine:latest

WORKDIR app

COPY --from=BUILDER /go/src/github.com/GPSChat/Bot/Bot .

CMD ["Bot", "-id", "Example42", "-name", "TestBot"]
