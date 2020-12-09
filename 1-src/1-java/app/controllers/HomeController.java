package controllers;

import actors.featureActor;
import actors.semanticActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import logic.*;
import models.Tweet;
import play.mvc.Controller;
import play.mvc.Result;
import services.CounterActor;
import services.CounterActor.Command;
import services.CounterActor.GetValue;
import services.CounterActor.Increment;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import tweetfeatures.NumericTweetFeatures;

import static tweetfeatures.NumericTweetFeatures.makeFeatureVector;
import actors.jsonReader;

/**
 * This controller contains an action to handle HTTP requests
 *
 * I've used an Akka-play seed,
 * the Akka cluster functionality is ignored/bypassed for now
 *
 *
 * !! To recompile, navigate to browser and refresh localhost:9000
 *
 */

public class HomeController extends Controller {
    private final File path = new File("/Users/mark/Documents/GitHub/HelpMe/1-src/1-java/conf/10.jsonl");

    private final ActorRef<Command> counterActor; // , TweetActor

    private final Scheduler scheduler;

    private final Duration askTimeout = Duration.ofSeconds(3L);


    List<Tweet> tweetList = new ArrayList<>();
    List<Vector> featureVectorList = new ArrayList<>();
    private Object[] objArray;

    @Inject
    public HomeController(ActorRef<CounterActor.Command> counterActor, Scheduler scheduler) {
        //TweetActor = system.actorOf(tweetActor.props());
        this.counterActor = counterActor;
        this.scheduler = scheduler;
    }


    public CompletionStage<Result> index() {
        // https://www.playframework.com/documentation/2.8.x/AkkaTyped#Using-the-AskPattern-&-Typed-Scheduler
        return AskPattern.ask(
                counterActor,
                GetValue::new,
                askTimeout,
                scheduler)
                .thenApply(this::renderIndex);
    }

    public CompletionStage<Result> increment() {
        // https://www.playframework.com/documentation/2.8.x/AkkaTyped#Using-the-AskPattern-&-Typed-Scheduler
        return AskPattern.ask(
                counterActor,
                Increment::new,
                askTimeout,
                scheduler)
                .thenApply(this::renderIndex);
    }


    private Result renderIndex(Integer hitCounter)  {
        long startTime = System.currentTimeMillis();


        //  sbt -java-home /Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home

        /** Parse into Tweet.class **/
        jsonReader reader = new jsonReader();
        tweetList = reader.parseOne();
        System.out.println("\ntweetList.size():\n" + tweetList.size());


        /** Keywords (logic.TermFrequency) **/
        featureActor featureActor = new featureActor();
        featureActor.getKeywords(tweetList);

        /** 51774 Tweets **/
        System.out.println("\ntweetList.size():\n" + tweetList.size());

        /** Feature Vector (NumericTweetFeatures.makeFeatures) **/
        for(Tweet tweet : tweetList) {
            semanticActor semanticActor = new semanticActor(tweet);
            semanticActor.analyse(tweet.getText());

            // Create the feature vector
            FeatureVec(tweet);

        }
        PrintWriter out = null;

        /** Export **/
        try {
            out = new PrintWriter(new FileWriter("../../0-data/processed/all_new.txt"));
            System.out.print("Exported");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Tweet tweet : tweetList) {
            if(tweet.getFeatureVector() != null){
                out.println(tweet.getFeatureVector());

            }

        }
        out.flush();
        out.close();
        System.out.println(featureVectorList);
        System.out.println("Finished...");


        //Outputs to browser
        long elapsedTime = System.currentTimeMillis() - startTime;
        long elapsedSeconds = elapsedTime / 1000;
        long secondsDisplay = elapsedSeconds % 60;
        long elapsedMinutes = elapsedSeconds / 60;
        System.out.println("minutes:" + elapsedMinutes);
        System.out.println("seconds:" + elapsedSeconds);
        return ok(Sanitise.toPrettyFormat(path));
    }

    private List<Path> collectFiles() {
        List<Path> bList = null;
        try {
            bList = Files.find(Paths.get("../../0-data/raw/data/2020/2020-A/tweets/"),
                    999,
                    (p, bfa) -> bfa.isRegularFile()
                    && p.getFileName().toString().matches(".*\\.jsonl"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(bList);
        return bList;
    }


    public void FeatureVec(Tweet tweet){
        Map<String, Double> stringDoubleMap = NumericTweetFeatures.makeFeatures(tweet);
        //System.out.println(stringDoubleMap);
        tweet.setFeatures(stringDoubleMap);
        tweet.setFeatureVector(makeFeatureVector(stringDoubleMap));
        //System.out.println(tweet.getFeatureVector());

        if(tweet.getFeatureVector() != null){
            featureVectorList.add(tweet.getFeatureVector());

        }

    }






}

