package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.service.RecommendationsService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsService service;

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Request for interactions weight: eventIds {}", request.getEventIdList());
            service.getInteractionsCount(request.getEventIdList()).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Request for recommendation for user {}", request.getUserId());
            service.getRecommendationForUser(request.getUserId(), request.getMaxResults());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request, StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Request for similar events: userId {}, eventId {}", request.getUserId(), request.getEventId());
            service.getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }
}
