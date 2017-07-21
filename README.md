[bintrayImage]: https://api.bintray.com/packages/breadmoirai/maven/SBF/images/download.svg
[bintrayLink]: https://bintray.com/breadmoirai/maven/SBF/_latestVersion
[contributorsImage]: https://img.shields.io/github/contributors/BreadMoirai/Samurai7.svg
[contributorsLink]: https://github.com/BreadMoirai/Samurai7/graphs/contributors
[issuesImage]: https://img.shields.io/github/issues-raw/BreadMoirai/Samurai7.svg
[issuesLink]: https://github.com/BreadMoirai/Samurai7/issues
[discordWidget]: https://discordapp.com/api/guilds/284822192821108736/widget.png
[discordInvite]: https://discord.gg/yAMdGU9

[ ![bintrayImage][] ][bintrayLink] 
[ ![contributorsImage][] ][contributorsLink]
[ ![issuesImage][] ][issuesLink]
[ ![discordWidget][] ][discordInvite]

# SamuraiBotFramework (SBF)
This is a simple framework for bots that uses modules and reflection to make it easy.

This framework is built ontop of [JDA](https://github.com/DV8FromTheWorld/JDA)
## Download
You can check the releases tab. This is also distributed via [bintray][bintrayLink].

```groovy
repositories {
    maven { url 'https://dl.bintray.com/breadmoirai/maven'}
}

dependencies {
    compile 'net.breadmoirai:SamuraiBotFramework:0.3.3'
}
```

### Javadoc
Web access to the javadocs is provided [here](https://breadmoirai.github.io/SamuraiBotFramework/). Please note that this is a work in progress and it is likely something you would like documented is not documented. Please open an issue via Github [Issues](https://github.com/BreadMoirai/SamuraiBotFramework/issues) or contact me directly through my [Discord][discordInvite].

## Example
**Main**
```java
import net.breadmoirai.sbf.core.impl.SamuraiClientBuilder;

public class Main {
    public static void main(String[] args) {
        ...
        long myId = 123456789L;
        SamuraiClientBuilder scb = new SamuraiClientBuilder()
                .addDefaultPrefixModule("!")
                .addAdminModule(member -> {
                    //defines criteria for which members are allowed to use commands marked with @Admin
                    long myId = 0L;
                    if (member.getUser().getIdLong() == myId) return true;
                    else return member.canInteract(member.getGuild().getSelfMember()) && member.hasPermission(Permission.KICK_MEMBERS);
                })
                .registerCommand(ShutdownCommand.class)
                .addModule(new CatDogModule());
        
        EventManager eventManager = scb.buildInterfaced();
                
        try {
            new JDABuilder(AccountType.BOT)
                    .setToken("mytoken")
                    .setEventManager(eventManager)
                    //SBF also comes with a singleton implementation of jagrosh's EventWaiter.
                    //This eventwaiter is slightly different in that it only takes a predicate that returns true if it should stop receiving events and false if it has not found the right event.
                    .addEventListener(EventWaiter.get())
                    .buildAsync();
            } catch (LoginException | RateLimitedException e) {
                e.printStackTrace();
            }
    }
}
```
Please note, many of the default modules provided by me rely on a Database such as the PrefixModule which stores strings in a Database.
The database is a thread-safe singleton instance of [JDBI](http://jdbi.github.io/) which will work with any JDBC connection.

The database should be initialized first before the SamuraiClient as in the following example where a connection to an embedded apache derby database is provided.
The Database#create method will take a [Supplier\<Connection\>](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html).
```java
public class Main {
    public static void main(String[] args) {
        try {
            DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
            Database.create(() -> DriverManager.getConnection("jdbc:derby:MyDatabase;create=true"));
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        ...
    }
}
```


**Shutdown Command**
```java
@Key("shutdown")
public class ShutdownCommand extends Command {
    public Response execute(CommandEvent event) {
        long myId = 123456789L;
        if (event.getAuthorId() != myId) {
            return Responses.of("What you do! Go away.");
        } else {
            event.getJDA().shutdown();
            return null;
        }
    }
}
```
**CatDog Module**
```java
public class CatDogModule implements IModule {
    @Override
    public void init(CommandEngineBuilder ceb, SamuraiClient client) {
        ceb.registerCommand(CatDogCommand.class);
    }

    public String getRandomCatUrl() {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new URL("http://random.cat/meow").openStream(), StandardCharsets.UTF_8))) {
            return new JSONObject(rd.readLine()).getString("file");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRandomDogUrl() {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new URL("http://random.dog/woof").openStream(), StandardCharsets.UTF_8))) {
            return "http://random.dog/" + rd.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```
**CatDog Commands**
There are multiple different types of commands you can extend.

The command below is triggered by `!catdog ...` like `!catdog meow` or `!catdog`
```java
@Key("catdog")
public class CatDogCommand extends Command {
    @Override
    public Response execute(CommandEvent event) {
        CatDogModule catDogModule = event.getClient().getModule("CatDogModule");
        final List<String> args = event.getArgs();
        String url = null;
        if (args.size() == 1)
            switch (args.get(0).toLowerCase()) {
                case "woof":
                    url = catDogModule.getRandomDogUrl();
                    break;
                case "meow":
                    url = catDogModule.getRandomCatUrl();
                    break;
            }
        if (url == null)
            url = ThreadLocalRandom.current().nextBoolean() ? catDogModule.getRandomCatUrl() : catDogModule.getRandomDogUrl();

        return Responses.of(new EmbedBuilder().setImage(url).build());
    }
}
```
This command is the same as above.
The only difference here is that the module is passed through to the method via reflection.
```java
@Key("catdog")
public class CatDogCommand extends ModuleCommand<CatDogModule> {
    @Override
    public Response execute(CommandEvent event, CatDogModule catDogModule) {
        final List<String> args = event.getArgs();
        String url = null;
        if (args.size() == 1)
            switch (args.get(0).toLowerCase()) {
                case "woof":
                    url = catDogModule.getRandomDogUrl();
                    break;
                case "meow":
                    url = catDogModule.getRandomCatUrl();
                    break;
            }
        if (url == null)
            url = ThreadLocalRandom.current().nextBoolean() ? catDogModule.getRandomCatUrl() : catDogModule.getRandomDogUrl();

        return Responses.of(new EmbedBuilder().setImage(url).build());
    }
}
```
This command is triggered via `!cat` or `!meow` for the meow method, or `!dog` or `!woof` for the woof method.
```java
public class CatDogCommand extends ModuleMultiCommand<CatDogModule> {

    @Key({"cat", "meow"})
    public Response meow(CommandEvent event, CatDogModule module) {
        return Responses.of(new EmbedBuilder().setImage(module.getRandomCatUrl()).build());
    }

    @Key({"dog", "woof"})
    public Response woof(CommandEvent event, CatDogModule module) {
        return Responses.of(new EmbedBuilder().setImage(module.getRandomCatUrl()).build());
    }
}
```
This is a MultiSubCommand where there is a primary key affixed to the class and secondary keys affixed to the methods. As such, this command is triggered by `!whatgoes meow` or `!whatgoes woof`
```java
@Key("whatgoes")
public class CatDogCommand extends ModuleMultiSubCommand<CatDogModule> {

    @Key("meow")
    public Response meow(CommandEvent event, CatDogModule module) {
        return Responses.of(new EmbedBuilder().setImage(module.getRandomCatUrl()).build());
    }

    @Key("woof")
    public Response woof(CommandEvent event, CatDogModule module) {
        return Responses.of(new EmbedBuilder().setImage(module.getRandomCatUrl()).build());
    }
}
```


       


