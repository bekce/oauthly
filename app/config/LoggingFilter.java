package config;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.inject.Inject;
import akka.stream.Materializer;
import play.Logger;
import play.libs.typedmap.TypedKey;
import play.mvc.*;
public class LoggingFilter extends Filter {

    public static final TypedKey<Object> KEY = TypedKey.create();

    @Inject
    public LoggingFilter(Materializer mat) {
        super(mat);
    }

    @Override
    public CompletionStage<Result> apply(
            Function<Http.RequestHeader, CompletionStage<Result>> nextFilter,
            Http.RequestHeader requestHeader) {
        long startTime = System.currentTimeMillis();
        Http.RequestHeader requestHeader1 = requestHeader.addAttr(KEY, requestHeader.path()+"|"+requestHeader.uri());
        return nextFilter.apply(requestHeader1).thenApply(result -> {
            long endTime = System.currentTimeMillis();
            long requestTime = endTime - startTime;

            Logger.info("{} {} took {}ms and returned {}",
                requestHeader.method(), requestHeader.uri(), requestTime, result.status());

            return result.withHeader("Request-Time", "" + requestTime);
        });
    }
}
