package move;

import services.TweetStore;
import Utils.APIUtils;
import services.TweetService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.Tweet;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Controller;

import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class TweetController extends Controller {


    //private HttpExecutionContext ec;
    private final TweetStore tweetStore;

    @Inject
    public TweetController(TweetStore tweetStore) {
        this.tweetStore = tweetStore;
        //this.ec = ec;
    }

    public CompletionStage<Result> update(Http.Request request) {
        JsonNode json = request.body().asJson();
        return supplyAsync(() -> {
            if (json == null) {
                return badRequest(APIUtils.createResponse("Expecting Json data", false));
            }
            Optional<Tweet> tweetOptional = tweetStore.updateTweet(Json.fromJson(json, Tweet.class));
            return tweetOptional.map(student -> {
                if (tweetStore == null) {
                    return notFound(APIUtils.createResponse("Student not found", false));
                }
                JsonNode jsonObject = Json.toJson(tweetStore);
                return ok(APIUtils.createResponse(jsonObject, true));
            }).orElse(internalServerError(APIUtils.createResponse("Could not create data.", false)));
        });//, ec.current());
    }

    public CompletionStage<Result> retrieve(int id) {
        return supplyAsync(() -> {
            final Optional<Tweet> tweetOptional = tweetStore.getTweet(id);
            return tweetOptional.map(student -> {
                JsonNode jsonObjects = Json.toJson(tweetStore);
                return ok(APIUtils.createResponse(jsonObjects, true));
            }).orElse(notFound(APIUtils.createResponse("Tweet with id:" + id + " not found", false)));
        }); //,ec.current());
    }

    public Result list() {
        Vector<Double> count = TweetService.getFeatureVector();
        ObjectNode result = Json.newObject();
        result.put("FeatureVector", String.valueOf(count));


        return ok(result);
    }


}