FROM golang:1.26-alpine AS builder

WORKDIR /app
COPY go.mod ./
COPY . .
RUN go mod tidy && CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o synapse .

FROM scratch
COPY --from=builder /app/synapse /synapse
ENTRYPOINT ["/synapse"]
