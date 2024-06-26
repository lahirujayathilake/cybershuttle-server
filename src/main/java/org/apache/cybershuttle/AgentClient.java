package org.apache.cybershuttle;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.agent.AgentCommunicationServiceGrpc;
import org.apache.airavata.agent.AgentMessage;
import org.apache.airavata.agent.ServerMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AgentClient {
    private final AgentCommunicationServiceGrpc.AgentCommunicationServiceStub asyncStub;

    public AgentClient(ManagedChannel channel) {
        asyncStub = AgentCommunicationServiceGrpc.newStub(channel);
    }

    private void startSync() throws InterruptedException {

        CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<AgentMessage> requestObserver = asyncStub.createMessageBus(new StreamObserver<ServerMessage>() {
            @Override
            public void onNext(ServerMessage serverMessage) {
                //System.out.println("Received from server: " + serverMessage.get());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server completed");
                finishLatch.countDown();
            }
        });

        try {
            for (int i = 1; i <= 5; i++) {
                //AgentMessage message = AgentMessage.newBuilder().setMessage("Message " + i).build();
                //requestObserver.onNext(message);
                Thread.sleep(1000);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("chat can not finish within 1 minutes");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        try {
            AgentClient client = new AgentClient(channel);
            client.startSync();
        } finally {
            channel.shutdown();
        }
    }
}
