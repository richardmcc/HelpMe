package controllers;

import play.mvc.*;
import twitter4j.TwitterException;
import services.ConnectTwitter;
import views.html.*;
import filters.infoRepository;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import actors.userActor;
import actors.hashtagActor;
import actors.locationActor;
import actors.tweetsWordsActor;
import akka.actor.*;
import static akka.pattern.Patterns.ask;
import javax.inject.*;
import scala.compat.java8.FutureConverters;

/**
 * This controller contains actions to handle HTTP requests to the application's
 * home page using Actor System. The Result method renders the main page of the application. The getUserProfile renders the
 * userPage and display's user's profile info along with the ten latest tweets.
 * The getLocationBasedTweets renders the location page and display's the ten
 * latest tweets for that location The getHashtagBasedTweets renders the hashtag
 * page and display's the ten latest tweets for that hashtag.
 * 
 * @author Navdeep Kaur Brar
 */
public class HomeController extends Controller {

	/** The configuration in the
	 * routes file means that this method will be called when the
	 * application receives a GET request with a path of
	 * /.
	 * 
	 * @author Navdeep Kaur Brar
	 * @return returns the result ok and renders the main page
	 */

	public Result index() {
		return ok(views.html.mainPage.render());
		//return ok(mainPage.render());
	}

	//Actor References defined
	private final ActorRef user_actor;
	private final ActorRef location_actor;
	private final ActorRef hashtag_actor;
	private final ActorRef tweetWords_actor;
	

	//using dependency injection to initialize the actor references
	@Inject
	public HomeController(ActorSystem actorObj) {
		user_actor = actorObj.actorOf(userActor.getProps());
		location_actor = actorObj.actorOf(locationActor.getProps());
		hashtag_actor = actorObj.actorOf(hashtagActor.getProps());
		tweetWords_actor = actorObj.actorOf(tweetsWordsActor.getProps());
		
	}

	/**
	 * Renders the userPage HTML page with the profile information and the latest ten tweets
	 * @route GET request with a path of /Profile/:name
	 * @param name the username whose profile and tweets are displayed
	 * @return the profile information and ten latest tweets to the userPage.scala.html
	 * @throws TwitterException It throws a TwitterException
	 */
	public CompletionStage<Result> getUserProfile(String name) throws TwitterException, ExecutionException, InterruptedException {
		return FutureConverters.toJava(ask(user_actor, new userActor.User(name), 5000))
				.thenApplyAsync(userInfo -> ok(userPage.render((infoRepository) userInfo)));
	} 

	/**
	 * Renders the location HTML page with the latest ten tweets
	 * @route GET request with a path of  /Location/:name.
	 * @param name the location for which the tweets are displayed
	 * @return the location and ten latest tweets to the locationPage.scala.html
	 * @throws TwitterException It throws a TwitterException
	 */
	public CompletionStage<Result> getLocationBasedTweets(String name) throws TwitterException, ExecutionException, InterruptedException {
		return FutureConverters.toJava(ask(location_actor, new locationActor.Location(name), 5000))
				.thenApplyAsync(userInfo -> ok(locationPage.render((infoRepository) userInfo)));
	}

	/**
	 * Renders the Hashtag HTML page with the latest ten tweets
	 * @route GET request with a path of  /hashtag/:name.
	 * @param name the hashtag for which the tweets are displayed
	 * @return the hashtag and ten latest tweets to the hashtagPage.scala.html
	 * @throws TwitterException It throws a TwitterException
	 */
	public CompletionStage<Result> getHashtagBasedTweets(String name) throws TwitterException, ExecutionException, InterruptedException {
		return FutureConverters.toJava(ask(hashtag_actor, new hashtagActor.Hashtag(name), 5000))
				.thenApplyAsync(userInfo -> ok(hashtagPage.render((infoRepository) userInfo)));
	}

	/**
	 * An action that renders the searchterms HTML page with the distinct words and
	 * frequencies The configuration in the routes file means that this method will
	 * be called when the application receives a GET request with a
	 * path of /searchTerms/:name.
	 * @param name the search terms for which the distinct words are displayed
	 * @return the distinct words and frequencies to the searchtermsPage.scala.html
	 * @throws TwitterException It throws a TwitterException
	 */
	public CompletionStage<Result> getDistinctWords(String name) throws TwitterException, ExecutionException, InterruptedException {
		return FutureConverters.toJava(ask(tweetWords_actor, new tweetsWordsActor.SearchTerms(name), 5000))
				.thenApply(userInfo -> ok(searchtermsPage.render((infoRepository) userInfo)));
	}
	

}
