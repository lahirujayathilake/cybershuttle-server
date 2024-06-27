package org.apache.cybershuttle.handler;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.airavata.agent.*;
import org.apache.cybershuttle.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class AgentCommunicationHandler extends AgentCommunicationServiceGrpc.AgentCommunicationServiceImplBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(AgentCommunicationHandler.class);

    private final Map<String, StreamObserver<ServerMessage>> ACTIVE_STREAMS = new ConcurrentHashMap<>();
    private final Map<String, String> PROCESS_STREAM_MAPPING = new ConcurrentHashMap<>();

    public AgentInfoResponse isAgentUp(String processId) {

        if (PROCESS_STREAM_MAPPING.containsKey(processId) &&
                ACTIVE_STREAMS.containsKey(PROCESS_STREAM_MAPPING.get(processId))) {
            return new AgentInfoResponse(processId, true);
        } else {
            return new AgentInfoResponse(processId, false);
        }
    }

    public AgentTunnelAck runTunnelOnAgent(AgentTunnelCreationRequest tunnelRequest) {
        AgentTunnelAck ack = new AgentTunnelAck();

        if (PROCESS_STREAM_MAPPING.containsKey(tunnelRequest.getProcessId()) &&
                ACTIVE_STREAMS.containsKey(PROCESS_STREAM_MAPPING.get(tunnelRequest.getProcessId()))) {
            String agentId = PROCESS_STREAM_MAPPING.get(tunnelRequest.getProcessId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(agentId);

            try {
                streamObserver.onNext(ServerMessage.newBuilder().setTunnelCreationRequest(TunnelCreationRequest.newBuilder()
                        .setDestinationHost(tunnelRequest.getDestinationHost())
                        .setDestinationPort(tunnelRequest.getDestinationPort())
                        .setSourcePort(tunnelRequest.getSourcePort())
                        .setSshUserName(tunnelRequest.getSshUserName())
                        .setPassword(Optional.ofNullable(tunnelRequest.getPassword()).orElse(""))
                        .setSshKeyPath(Optional.ofNullable(tunnelRequest.getSshKeyPath()).orElse(""))
                        .build()).build());
            } catch (Exception e) {
                LOGGER.error("Failed to submit tunnel creation request to process {} on agent {}", tunnelRequest.getProcessId(), agentId, e);
                ack.setError(e.getMessage());
            }

        } else {
            LOGGER.warn("No agent found to run the tunnel on process {}", tunnelRequest.getProcessId());
            ack.setError("No agent found to run the tunnel on process " + tunnelRequest.getProcessId());
        }

        return ack;
    }
    public AgentCommandAck runCommandOnAgent(AgentCommandRequest commandRequest) {

        String executionId = UUID.randomUUID().toString();
        AgentCommandAck ack = new AgentCommandAck();
        ack.setExecutionId(executionId);

        if (PROCESS_STREAM_MAPPING.containsKey(commandRequest.getProcessId()) &&
                ACTIVE_STREAMS.containsKey(PROCESS_STREAM_MAPPING.get(commandRequest.getProcessId()))) {
            String agentId = PROCESS_STREAM_MAPPING.get(commandRequest.getProcessId());
            StreamObserver<ServerMessage> streamObserver = ACTIVE_STREAMS.get(agentId);

            try {
                LOGGER.info("Running a command for process {} on agent {}", commandRequest.getProcessId(), agentId);
                streamObserver.onNext(ServerMessage.newBuilder().setCommandExecutionRequest(
                        CommandExecutionRequest.newBuilder()
                                .setExecutionId(executionId)
                                .setWorkingDir(commandRequest.getWorkingDir())
                                .addAllArguments(commandRequest.getArguments()).build()).build());

            } catch (Exception e) {
                LOGGER.error("Failed to submit command execution request {} to process {} on agent {}",
                        executionId, commandRequest.getProcessId(), agentId, e);
                ack.setError(e.getMessage());
            }
        } else {
            LOGGER.warn("No agent found to run the command on process {}", commandRequest.getProcessId());
            ack.setError("No agent found to run the command on process " + commandRequest.getProcessId());
        }

        return ack;
    }

    private void handleAgentPing(AgentPing agentPing, String streamId) {
        LOGGER.info("Received agent ping for process id {} with agent id {}", agentPing.getProcessId(), agentPing.getAgentId());
        PROCESS_STREAM_MAPPING.put(agentPing.getProcessId(), streamId);
    }

    private void handleCommandExecutionResponse (CommandExecutionResponse commandExecutionResponse) {

    }

    private void handleContainerExecutionResponse (ContainerExecutionResponse containerExecutionResponse) {

    }

    private void handleAgentTerminationTesponse (TerminateExecutionResponse terminateExecutionResponse) {

    }

    private String generateStreamId() {
        // Generate a unique ID for each stream
        return java.util.UUID.randomUUID().toString();
    }
    @Override
    public StreamObserver<AgentMessage> createMessageBus(StreamObserver<ServerMessage> responseObserver) {

        String streamId = generateStreamId();
        ACTIVE_STREAMS.put(streamId, responseObserver);

        return new StreamObserver<AgentMessage>() {
            @Override
            public void onNext(AgentMessage request) {

                switch (request.getMessageCase()) {
                    case AGENTPING -> {
                        handleAgentPing(request.getAgentPing(), streamId);
                    }
                    case COMMANDEXECUTIONRESPONSE -> {
                        handleCommandExecutionResponse(request.getCommandExecutionResponse());
                    }
                    case CONTAINEREXECUTIONRESPONSE -> {
                        handleContainerExecutionResponse(request.getContainerExecutionResponse());
                    }
                    case TERMINATEEXECUTIONRESPONSE -> {
                        handleAgentTerminationTesponse(request.getTerminateExecutionResponse());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                LOGGER.error("Error in processing stream {}", streamId, t);
                ACTIVE_STREAMS.remove(streamId);
            }

            @Override
            public void onCompleted() {
                LOGGER.info("Stream {} is completed", streamId);
                responseObserver.onCompleted();
                ACTIVE_STREAMS.remove(streamId);
            }
        };
    }
}
