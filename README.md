[bintrayImage]: https://api.bintray.com/packages/breadmoirai/maven/BreadBotFramework/images/download.svg
[bintrayLink]: https://bintray.com/breadmoirai/maven/BreadBotFramework/_latestVersion
[contributorsImage]: https://img.shields.io/github/contributors/BreadMoirai/BreadBotFramework.svg
[contributorsLink]: https://github.com/BreadMoirai/BreadBotFramework/graphs/contributors
[issuesImage]: https://img.shields.io/github/issues-raw/BreadMoirai/BreadBotFramework.svg
[issuesLink]: https://github.com/BreadMoirai/BreadBotFramework/issues
[discordWidget]: https://discordapp.com/api/guilds/284822192821108736/widget.png
[discordInvite]: https://discord.gg/yAMdGU9
[wikiImage]: https://img.shields.io/badge/wiki-10%-orange.svg
[wikiLink]: https://github.com/BreadMoirai/BreadBotFramework/wiki
[docsImage]: https://img.shields.io/badge/docs-30%-yellow.svg
[docsLink]: https://breadmoirai.github.io/BreadBotFramework/

[ ![bintrayImage][] ][bintrayLink] 
[ ![contributorsImage][] ][contributorsLink]
[ ![issuesImage][] ][issuesLink]
[ ![discordWidget][] ][discordInvite]
[ ![wikiImage][]][wikiLink]
[ ![docsImage][]][docsLink]

# BreadBotFramework
This is a framework for Discord Bots

### Features
 - CommandModules
 - 
##### Planned Features
 - Being able to do things with CommandModules instead of just having access to them.
 - Being able to link sent messages with Response objects
 - logs

This framework is built ontop of [JDA](https://github.com/DV8FromTheWorld/JDA)
## Download
You can check the [releases](https://github.com/BreadMoirai/BreadBotFramework/releases) tab for jars. This is also distributed via [bintray][bintrayLink].

### Adding as Dependency
[![bintrayImage][]][bintrayLink] 

When using the snippets below replace the **VERSION** key with the version shown above.

#### Gradle
```groovy
repositories {
  jcenter()
}

dependencies {
  compile 'com.github.breadmoirai:BreadBotFramework:VERSION'
}
```

#### Maven
```xml
<repository>
  <id>jcenter</id>
  <name>jcenter</name>
  <url>http://jcenter.bintray.com/</url>
</repository>

<dependency>
  <groupId>com.github.breadmoirai</groupId>
  <artifactId>BreadBotFramework</artifactId>
  <version>VERSION</version>
  <type>pom</type>
</dependency>
```

### Javadoc
Web access to the javadocs is provided [here][docsLink]. Please note that this is a work in progress and it is likely something you would like documented is not documented. Please open an issue via Github [Issues](https://github.com/BreadMoirai/BreadBotFramework/issues) or contact me directly through my [Discord][discordInvite].

## Example
Please refer [here](https://github.com/BreadMoirai/BreadBotFramework/wiki/Getting-Started)
```
## CatDog Module
Here is an example module.
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
## CatDog Commands
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
Note that with any of the *Multi* Commands, The return type should be either void or castable to Response.

This is a MultiSubCommand where there is a primary keys affixed to the class and secondary keys affixed to the methods. As such, this command is triggered by `!whatgoes meow` or `!whatgoes woof`
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

## Depedencies

<p>+--- org.jdbi:jdbi3:3.0.0-beta1  
|    +--- org.antlr:antlr-runtime:3.4  
|    |    +--- org.antlr:stringtemplate:3.2.1 
|    |    |    \--- antlr:antlr:2.7.7  
|    |    \--- antlr:antlr:2.7.7  
|    +--- org.slf4j:slf4j-api:1.7.21  
|    +--- com.google.code.findbugs:annotations:3.0.1  
|    |    +--- net.jcip:jcip-annotations:1.0  
|    |    \--- com.google.code.findbugs:jsr305:3.0.1  
|    +--- com.google.guava:guava:21.0  
|    \--- net.jodah:expiringmap:0.5.6  
+--- org.jdbi:jdbi3-parent:3.0.0-beta1  
+--- org.jdbi:jdbi3-sqlobject:3.0.0-beta1  
|    +--- org.jdbi:jdbi3:3.0.0-beta1 (*)  
|    +--- com.google.code.findbugs:annotations:3.0.1 (*)  
|    \--- org.assertj:assertj-core:3.5.2  
+--- org.reflections:reflections:0.9.10  
|    +--- com.google.guava:guava:18.0 -> 21.0  
|    +--- org.javassist:javassist:3.18.2-GA  
|    \--- com.google.code.findbugs:annotations:2.0.1 -> 3.0.1 (*)  
\--- org.jetbrains:annotations:13.0  
       


