# User Access Control Bot
Access to the monarchdb database is limited and, this bot is to assert
that only authorised users are able to use the database.

For all appendices (marked with \*) see [#appendix](#appendix)

## Setup
### Required File and Databases
 - `user.txt` in the current directory
 - `bot.conf` in the current directory
 - `userbotdb` and `monarchdb` running on postgresql locally

#### user.txt format
```
username
password
```

#### bot.conf format
```yaml
token=<discord token>
database-admin-id=<database owner's discord id>
```

### Required Software
 - Java 1.8
 - Postgresql (for the databases)
 - Gradle (for building)
 
### Running the Bot
After setting up the softwares and configuration files, you need to compile the
code then execute it with java.

#### Compling the Bot
 - Use `gradle test` that you have downloaded is working as intended.
 - Use `gradle shadowJar` to create the executable jar file which is saved in
 `.build/libs/`.
 
#### Executing the Bot
`java -jar user-access-bot-1.0-SNAPSHOT-all.jar -Xmx1G &`

#### Databases
See `./create_userbotdb_tables.sql` and `../Database_SQL.sql` for 
table definitions. Both are to be running on the same postgresql instance 
and, have read/write permissions for the user in `user.txt`. 

## Specification
The bot must ensure that users can be created by admins, have their
password reset, deleted by admins and, that the active accounts can be 
seen.

### Data Storage
A database is to be setup that stores the user information.
**It is imperative that users cannot see this database**.
See `users-db-creation.sql` and others in this repo.

### User Change* Log
 - A channel is to be created with an embed showing all active users.
 - A channel is to be created with a log of user changes* including 
 who/what caused them.

### Commands
|command|permitted users|automatic invocation|description|
|---|---|---|---|
| create_user | admins | - | creates a database user |
| delete_user | admins | on user leave | deletes a database user |
| view_user_info | admins | - | shows info a user (discord owner, creation time and status) |
| reset_password | admins, account user | - | resets a user password |
| view_users | admins | user change\* | shows a list of all active users |
| reset_all_passwords | server/database owner | - | resets the password of all active users |
| Admin Commands |
| activate_guild | database owner | - | sets the guild to active to allow for database account creation |
| deactivate_guild | database owner | - | sets the guild to inactive to disallow for database account creation |
| set_status_category | admins | - | sets the category to change the title of to reflect the database status |
| set_user_change_log_channel | admins | - | sets the channel to have the user log in | 
| set_administrator_role | admins | - | sets the admin role |

### Security
 - Passwords are to be sent with a spoiler tag to the user via discord
 and; when they react to the message or, after 5 minutes to be deleted.
 - Passwords are to be 15 digits long of random alpha-numeric charcters
 (of both cases).
 - Passwords are never to be stored by the bot.
 - Users cannot access the user-info database.
 
## Appendix 
\* a user change is whenever a database user is created, has a password
changed or deleted.

### Copyright
System created by Danny Piper for use with `monarch3`. All licencing and,
distributive rights are hereby given to the group.