package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.*;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class RecommendationsClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getRatings(List<Long> ids) {
        log.info("RecommendationClient: ratings request");

        try {
            InteractionsCountRequestProto interactionsCountRequestProto = InteractionsCountRequestProto.newBuilder()
                    .addAllEventId(ids)
                    .build();

            Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(interactionsCountRequestProto);
            return asStream(iterator);
        } catch (Exception e) {
            log.error("RecommendationClient ratings request FAILED with message {}", e.getMessage());
            throw new RuntimeException("RecommendationClient ratings request FAILED with message " + e.getMessage());
        }
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(Long userId, long maxResults) {
        log.info("RecommendationClient: for user {} recommendation request", userId);

        try {
            UserPredictionsRequestProto requestProto = UserPredictionsRequestProto.newBuilder()
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(requestProto);
            return asStream(iterator);
        } catch (Exception e) {
            log.error("RecommendationClient user recommendation request FAILED with message {}", e.getMessage());
            throw new RuntimeException("RecommendationClient user recommendation request FAILED with message " + e);
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, long maxResults) {
        log.info("RecommendationClient: for event {}, user {}", eventId, userId);

        try {
            SimilarEventsRequestProto requestProto = SimilarEventsRequestProto.newBuilder()
                    .setEventId(eventId)
                    .setUserId(userId)
                    .setMaxResults(maxResults)
                    .build();

            Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(requestProto);
            return asStream(iterator);
        } catch (Exception e) {
            log.error("RecommendationClient similar recommendation request FAILED with message {}", e.getMessage());
            throw new RuntimeException("RecommendationClient similar recommendation request FAILED with message "
                    + e);
        }
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
