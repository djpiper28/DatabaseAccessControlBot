insert into databaseusers values (? ? ? ? ? ? false);
create user ? with password pwd;
grant select on players, tournaments, tournamentplayers, commanders, matches, decks, tournamentdecks, deckcards, matchplayers, cards to ?;
