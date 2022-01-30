revoke all on players, matchplayers, matches, tournamentplayers, commanders, tournaments, tournamentdecks, decks, deckcards, cards from username;
drop user username;
update DatabaseUsers set active=false where DatabaseUserID = ?;
