import twitter4j.*;
import twitter4j.conf.*;
import java.sql.*;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

import java.util.List;
import java.util.ArrayList;

public class TwitterCrawlerV2 extends Crawler implements Database {

	private static ArrayList<String> dtg = new ArrayList<String>();
	private static ArrayList<String> user = new ArrayList<String>();
	private static ArrayList<String> text = new ArrayList<String>();
	int totalTweets = 5;

	public TwitterCrawlerV2() {

	}

	public void crawl() {

		long lastID = Long.MAX_VALUE;

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("SwEnOs3wjXJZ3C7hIVuXG2xuR")
				.setOAuthConsumerSecret("GE7S0xHjBu0V9hBw63IhmDia2g71mZpUXjg851ipMYgTzdyuWN")
				.setOAuthAccessToken("1364017893147439106-ieVSnBE3qKtGwnQxk8npxf9SC2Eloj")
				.setOAuthAccessTokenSecret("T8D7vCZtPrLiYGR5DaVh7jfjEASG2V4rZDefMdyIpFUfI");

		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		Query query = new Query("GME" + "-filter:retweets -filter:links -filter:images");
		query.setLocale("en");
		query.setLang("en");

		while (text.size() < totalTweets) {
			if (totalTweets - text.size() > 100)
				query.setCount(100);
			else
				query.setCount(totalTweets - text.size());

			try {
				QueryResult result = twitter.search(query);

				for (Status tweet : result.getTweets()) {
					dtg.add(tweet.getCreatedAt().toString());
					user.add(tweet.getUser().getScreenName().toString());
					text.add(tweet.getText().toString());

					if (tweet.getId() < lastID)
						lastID = tweet.getId();

				}
			} catch (TwitterException te) {
				te.printStackTrace();
				System.out.println("Failed to search tweets: " + te.getMessage());
			}
			query.setMaxId(lastID - 1);
		}

		System.out.println("Time: " + dtg);
		System.out.println("@ " + user);
		System.out.println(" - " + text);
	}

	public void store() {
		CoreDocument coreDocument = new CoreDocument(text.toString());
		// setting up JDBC connection
		Connection conn = null;
		Statement stmt = null;

		String jdbcurl = "jdbc:mysql://localhost:3306/stockdb";
		String username = "root";
		String password = "qwe123";

		try {
			// Opening a connection
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(jdbcurl, username, password);
			System.out.println(text);

			// EDIT THIS
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();
			while (totalTweets > 0) {
				// writes query into SQL database
				String sql = " INSERT INTO stock" + "(Date_created,Tweet,Screen_name,Sentiments)" + " VALUES" + "('"
						+ dtg + "','" + text + "', '" + user + "', '" + coreDocument.sentences() + "')";

				// String sql = "insert into stockdb(Date_created,Tweet,Sentiment,)" + " VALUES
				// ('"+status.getCreatedAt() +
				// "," +status.getText() ")";

				// updates query into MySQL database
				System.out.println(sql);
				stmt.executeUpdate(sql);
			}
			System.exit(0);
			System.out.println("Records inserted into SQL Database");

			// close write sequence into SQL database
			System.exit(0);
			stmt.close();
			conn.close();
		}

		catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // exeption errors if resources cannot be closed
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		}
	}

	// parses tweet.getText() into NLP pipeline
	public void analyse() {
		StanfordCoreNLP stanfordCoreNLP = Pipeline.getPipeline();
		CoreDocument coreDocument = new CoreDocument(text.toString());
		stanfordCoreNLP.annotate(coreDocument);
		List<CoreSentence> sentences = coreDocument.sentences();
		for (CoreSentence sentence : sentences) {
			String sentiment = sentence.sentiment();
			System.out.println(sentiment);

		}
	}

}
