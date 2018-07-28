package com.project.business;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.project.domain.datatable.LadderTableEntry;
import com.project.domain.ladder.Entries;
import com.project.domain.ladder.Ladder;

public class DatasetService {

	private static CurrentLeagueService currentLeagueService = new CurrentLeagueService();
	private static List<String> leagues = new ArrayList<>();
	private static List<List<LadderTableEntry>> currentDataset = new ArrayList<>();
	private static List<List<LadderTableEntry>> latestDataset = new ArrayList<>();

	public DatasetService() throws InterruptedException {
		calculateDataSet();
	}

	public static List<List<LadderTableEntry>> getLatestDataSet() throws InterruptedException {
		leagues = currentLeagueService.getLeagues();
		latestDataset.clear();

		for (int i = 0; i < leagues.size(); i++) {
			List<LadderTableEntry> tableEntries = new ArrayList<>();
			String url = "http://api.pathofexile.com/ladders/" + leagues.get(i) + "?limit=200";
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<Ladder> response = restTemplate.getForEntity(url, Ladder.class);

			for (Entries anEntry : response.getBody().getEntries()) {
				LadderTableEntry entry = new LadderTableEntry();
				entry.setRank(anEntry.getRank());
				entry.setOnline(anEntry.getOnline());
				entry.setCharacter(anEntry.getCharacter().getName());
				entry.setDead(anEntry.getDead());
				entry.setAccount(anEntry.getAccount().getName());
				entry.setLevel(anEntry.getCharacter().getLevel());
				entry.setTheClass(anEntry.getCharacter().getTheClass());
				entry.setChallenges(anEntry.getAccount().getChallenges().getTotal());
				entry.setExperience(anEntry.getCharacter().getExperience());
				if (anEntry.getAccount().getTwitch() != null) {
					entry.setTwitch(anEntry.getAccount().getTwitch().getName());
				} else {
					entry.setTwitch("");
				}
				tableEntries.add(entry);
			}
			latestDataset.add(tableEntries);
			Thread.sleep(1000);
		}
		if (currentDataset.size() == 0) {
			currentDataset = latestDataset;
		}
		return latestDataset;
	}

	public static void calculateDataSet() throws InterruptedException {
		// copy latest to current dataset
		currentDataset = latestDataset;
		// get the latest dataset
		latestDataset = DatasetService.getLatestDataSet();
		// iterate
		for (int i = 0; i < latestDataset.size(); i++) {
			for (int j = 0; j < latestDataset.get(i).size(); j++) {
				for (int k = 0; k < currentDataset.get(i).size(); k++) {
					if (latestDataset.get(i).get(j).getCharacter().equals(currentDataset.get(i).get(k).getCharacter())) {
						// character match then calculate xph
						// System.out.println("match found for : " +
						// currentDataset.get(i).get(k).getCharacter());
						Long newXPPH, oldXPPH;
						String latest = latestDataset.get(i).get(j).getExperience();
						String current = currentDataset.get(i).get(k).getExperience();
						if (latest.equals("")) {
							newXPPH = new Long(0);
						} else {
							newXPPH = Long.parseLong(latest);
						}

						if (current.equals("")) {
							oldXPPH = new Long(0);
						} else {
							oldXPPH = Long.parseLong(current);
						}
						String difference = String.valueOf(oldXPPH - newXPPH);
						String xpPerHour = String.valueOf((oldXPPH - newXPPH) * 12);
						latestDataset.get(i).get(j).setXph(xpPerHour);
						latestDataset.get(i).get(j).setXphDifference(difference);
						// set polling timestamp for current time
						String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
						latestDataset.get(i).get(j).setTimeStamp(timeStamp);
					}
				}
			}
		}
	}

	public static List<LadderTableEntry> getCalculatedDataset(String selectedLeague) {
		switch (selectedLeague) {
			case "Incursion": {
				System.out.println("getCalculatedDataset() : Incursion");
				return latestDataset.get(0);
			}
			case "Hardcore Incursion": {
				System.out.println("getCalculatedDataset() : Hardcore Incursion");
				return latestDataset.get(1);
			}
			case "SSF Incursion": {
				System.out.println("getCalculatedDataset() : SSF Incursion");
				return latestDataset.get(2);
			}
			case "SSF Incursion HC": {
				System.out.println("getCalculatedDataset() : SSF Incursion HC");
				return latestDataset.get(3);
			}
			default: {
				return latestDataset.get(0);
			}
		}
	}

}