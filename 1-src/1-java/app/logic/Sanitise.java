package logic;

import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.Tweet;
import play.libs.Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Sanitise {

    /*
    private static final Set<String[]> stopWords = new HashSet<>();

    static {
        try {
            String wordsAsStr = FileUtils.readFile("conf/stopwords.txt");
            String[] stopwordsArray = wordsAsStr.split("\\s+");
            Collections.addAll(stopWords, stopwordsArray);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

     */

    public static void clean(Tweet tweet){
        // Remove URLs, mentions, hashtags and whitespace
        tweet.setText(tweet.getText().trim()
                .replaceAll("http.*?[\\S]+", "")
                .replaceAll("@[\\S]+", "")
                .replaceAll("#", "")
                .replaceAll("[\\s]+", " ")
                //replace text between {},[],() including them
                .replaceAll("\\{.*?}", "")
                .replaceAll("\\[.*?]", "")
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("[^A-Za-z0-9(),!?@'`\"_\n]", " ")
                .replaceAll("[/]"," ")
                .replaceAll(";"," "));

        Pattern charsPunctuationPattern = Pattern.compile("[\\d:,\"'`_|?!\n\r@;]+");
        String input_text = charsPunctuationPattern.matcher(tweet.getText().trim().toLowerCase()).replaceAll("");

        //Collect all tokens into labels collection.
        Collection<String> labels = Arrays.asList(input_text.split(" ")).parallelStream().filter(label->label.length()>0).collect(Collectors.toList());
        //get from standard text files available for Stopwords. e.g https://algs4.cs.princeton.edu/35applications/stopwords.txt
        //labels = labels.parallelStream().filter(label ->  !StopWords.getStopWords().contains(label.trim())).collect(Collectors.toList());


    }



    /**
    public static boolean isStopWord(String word) {
        return stopWords.contains(word) || word.trim().length() < 2;
    }


    public static String[] removeStopWords(String words) {
        List<String> filteredWords = new ArrayList<String>();
        for (String w : split(words)) {
            if (!isStopWord(w)) {
                String cleanupWord = w.replaceAll("[^A-z0-9#]", "");
                if (cleanupWord.length() > 1) {
                    filteredWords.add(cleanupWord);
                }

            }
        }
        //System.out.println("\nCleaned & Tokenised Text:\n" + Arrays.toString(filteredWords.toArray(new String[0])));
        return filteredWords.toArray(new String[0]);
    }



    public static JavaRDD<String> loadTwitterData(JavaSparkContext sc, String file) {
        JavaRDD<String> logData = sc.textFile(file).cache();

        J//avaRDD<String> tweetText = logData.map(new Function<String, String>() {
           // @Override
            public String call(String s) {
                String[] tokens = s.split(" ");
                String t = new String();
                for (String token:
                        tokens) {
                    t += token.replaceAll("[\"')(]", "").toLowerCase();
                    t = t.replaceAll("http[^\\s,]*", "");
                    t = t.replaceAll("https[^\\s,]*", "");
                    t = t.replaceAll("#", "");
                    t = t.replaceAll("@[A-z0-9]+", "");
                    t = t.replaceAll("[:!,.<]", "");
                    t = t.replaceAll("[0-9]*", "");
                    t = t.replaceAll("&amp;", "");
                    t = t.replaceAll("…", "");
                    t = t.replaceAll("\\n", " ");
                    t = t.replaceAll("rt ", "");
                    t += " ";
                }
                //System.out.println(t);
                return t;
            }
        });

        tweetText = tweetText.filter(new Function<String, Boolean>() {
            public Boolean call(String s) {
                return s != null;
            }
        });
        return tweetText;
    }

    public static float format(double value) {
        return Math.round(1000 * value) / 1000f;
    }
    **/









}
