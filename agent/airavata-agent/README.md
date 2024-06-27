## The Agent for orchestrating Airavata job workloads

## Running the agent
```
go mod tidy
go install
go run agent.go echo $JAVA_HOME
```

## Building proto files

```
go install google.golang.org/protobuf/cmd/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
export PATH=$PATH:$HOME/go/bin
protoc --go_out=. --go-grpc_out=. agent-communication.proto
```

## Sample Requests


```
POST http://localhost:9001/api/v1/application/agent/execute

{
    "processId": "process1",
    "workingDir": "",
    "arguments": ["docker", "ps", "-a"]
} 
```
```
POST http://localhost:9001/api/v1/application/agent/tunnel

{
    "destinationHost": "32.241.33.22",
    "destinationPort": "9999",
    "sshUserName": "sshuser",
    "sourcePort": "9001",
    "sshKeyPath": "/Users/dwannipu/.ssh/id_rsa_unencrypted",
    "processId": "process1"
}
```
