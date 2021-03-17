public class MainProgram {
	
	public void crawl(Crawler crawler) {
		crawler.crawl();
	}
	
	public void store(Crawler crawler) {
		crawler.store();
	}
	
	public static void main(String args[]) {
		MainProgram program = new MainProgram();
		Crawler twitter = new TwitterCrawlerV2();
		//RedditCrawler reddit = new RedditCrawler();
		program.crawl(twitter);
		//program.crawl(reddit);
		program.store(twitter);
		//program.store(reddit);
	}
	
}
