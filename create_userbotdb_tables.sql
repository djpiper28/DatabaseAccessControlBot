CREATE TABLE GuildSettings (
    GuildID bigint NOT NULL PRIMARY KEY,
    AllowedAccess boolean NOT NULL,
    DatabaseStatusCategoryID bigint,
    UserChangeLogChannelID bigint,
    ActiveUserChannelID bigint,
    AdministratorRoleID bigint
);

CREATE TABLE DiscordUsers (
    DiscordID bigint,
    NameCache varchar(30)
);

CREATE TABLE DatabaseUsers (
    DatabaseUserID uuid NOT NULL PRIMARY KEY,
    GuildID bigint NOT NULL references GuildSettings(GuildID),
    DiscordID bigint NOT NULL references DiscordUser(DiscordID),
    UserName varchar(30) NOT NULL UNIQUE,
    CreationTime timestamp NOT NULL,
    DeletionTime timestamp NOT NULL,
    Active boolean NOT NULL
);
